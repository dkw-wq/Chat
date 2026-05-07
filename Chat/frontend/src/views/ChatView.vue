<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import MiniProfileCard from "@/components/MiniProfileCard.vue";
import { getCurrentUser, logoutUser } from "@/api/auth";
import { listChatMessages, sendChatMessage } from "@/api/chat";
import { listFriends } from "@/api/friends";

const props = defineProps({
  friendId: {
    type: String,
    required: true
  }
});

const router = useRouter();
const currentUser = ref(null);
const friend = ref(null);
const friends = ref([]);
const messages = ref([]);
const draft = ref("");
const feedback = ref("");
const loading = ref(true);
const sending = ref(false);
const socketStatus = ref("disconnected");
const messageList = ref(null);
let socket = null;
let reconnectTimer = null;
let pollingTimer = null;
let shouldReconnect = true;

const friendIdNumber = computed(() => Number(props.friendId));

function isMine(message) {
  return currentUser.value && message.senderId === currentUser.value.id;
}

function isSocketReady() {
  return socket && socket.readyState === WebSocket.OPEN;
}

function websocketUrl() {
  const protocol = window.location.protocol === "https:" ? "wss" : "ws";
  return `${protocol}//${window.location.hostname}:8080/ws/chat`;
}

function belongsToCurrentConversation(message) {
  if (!currentUser.value || !friend.value) {
    return false;
  }

  return (
    (message.senderId === currentUser.value.id && message.receiverId === friend.value.id)
    || (message.senderId === friend.value.id && message.receiverId === currentUser.value.id)
  );
}

function appendMessage(message) {
  if (messages.value.some((item) => item.id === message.id)) {
    return;
  }
  if (!belongsToCurrentConversation(message)) {
    return;
  }
  messages.value.push(message);
  scrollToBottom();
}

function formatTime(value) {
  if (!value) {
    return "";
  }
  return new Date(value).toLocaleString("zh-CN", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
}

async function scrollToBottom() {
  await nextTick();
  if (messageList.value) {
    messageList.value.scrollTop = messageList.value.scrollHeight;
  }
}

async function loadPage() {
  loading.value = true;
  feedback.value = "";
  stopMessagePolling();

  try {
    currentUser.value = await getCurrentUser();
    friends.value = await listFriends();
    friend.value = friends.value.find((item) => item.id === friendIdNumber.value) || null;

    if (!friend.value) {
      feedback.value = "只能打开好友的聊天窗口";
      return;
    }

    messages.value = await listChatMessages(friend.value.id, 100);
    await scrollToBottom();
    connectWebSocket();
    startMessagePolling();
  } catch {
    router.replace("/login");
  } finally {
    loading.value = false;
  }
}

async function submitMessage() {
  const content = draft.value.trim();
  if (!content || !friend.value) {
    return;
  }

  sending.value = true;
  feedback.value = "";
  try {
    if (isSocketReady()) {
      socket.send(JSON.stringify({
        type: "SEND_MESSAGE",
        receiverId: friend.value.id,
        content
      }));
    } else {
      const message = await sendChatMessage(friend.value.id, content);
      appendMessage(message);
    }
    draft.value = "";
  } catch (error) {
    feedback.value = error.message || "发送失败";
  } finally {
    sending.value = false;
  }
}

async function refreshMessages() {
  if (!friend.value) {
    return;
  }
  messages.value = await listChatMessages(friend.value.id, 100);
  await scrollToBottom();
}

async function quietlyRefreshMessages() {
  if (!friend.value || document.hidden) {
    return;
  }
  try {
    messages.value = await listChatMessages(friend.value.id, 100);
    await scrollToBottom();
  } catch {
    stopMessagePolling();
  }
}

function startMessagePolling() {
  stopMessagePolling();
  pollingTimer = window.setInterval(quietlyRefreshMessages, 2000);
}

function stopMessagePolling() {
  if (pollingTimer) {
    window.clearInterval(pollingTimer);
    pollingTimer = null;
  }
}

function connectWebSocket() {
  if (!currentUser.value) {
    return;
  }
  if (isSocketReady()) {
    return;
  }

  closeWebSocket(false);
  shouldReconnect = true;
  socketStatus.value = "connecting";
  socket = new WebSocket(websocketUrl());

  socket.addEventListener("open", () => {
    socketStatus.value = "connected";
    feedback.value = "";
  });

  socket.addEventListener("message", (event) => {
    const envelope = JSON.parse(event.data);
    if (envelope.type === "CHAT_MESSAGE") {
      appendMessage(envelope.payload);
      return;
    }
    if (envelope.type === "ERROR") {
      feedback.value = envelope.payload?.message || "实时消息处理失败";
    }
  });

  socket.addEventListener("close", () => {
    socketStatus.value = "disconnected";
    scheduleReconnect();
  });

  socket.addEventListener("error", () => {
    socketStatus.value = "disconnected";
  });
}

function scheduleReconnect() {
  if (reconnectTimer || !currentUser.value || !shouldReconnect) {
    return;
  }

  reconnectTimer = window.setTimeout(() => {
    reconnectTimer = null;
    connectWebSocket();
  }, 2500);
}

function closeWebSocket(allowReconnect = false) {
  shouldReconnect = allowReconnect;
  if (!allowReconnect && reconnectTimer) {
    window.clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
  if (socket) {
    socket.close();
    socket = null;
  }
}

async function logout() {
  closeWebSocket();
  stopMessagePolling();
  await logoutUser();
  router.replace("/login");
}

onMounted(loadPage);

watch(() => props.friendId, loadPage);

onBeforeUnmount(() => {
  closeWebSocket();
  stopMessagePolling();
});
</script>

<template>
  <main class="chat-shell">
    <aside class="chat-sidebar">
      <div>
        <p class="eyebrow">Chat</p>
        <h1>聊天窗口</h1>
        <p class="copy">{{ currentUser ? `账号：${currentUser.username}` : "正在读取用户信息" }}</p>
      </div>

      <div class="sidebar-actions">
        <RouterLink class="button" to="/friends">好友管理</RouterLink>
        <button class="button" type="button" @click="logout">退出</button>
      </div>

      <div class="mini-list">
        <RouterLink
          v-for="item in friends"
          :key="item.id"
          class="mini-friend"
          :class="{ active: item.id === friendIdNumber }"
          :to="`/chat/${item.id}`"
        >
          <MiniProfileCard :user-id="item.id">
            <img class="avatar image-avatar" :src="item.avatarUrl" alt="">
          </MiniProfileCard>
          <span>
            <strong>{{ item.nickname }}</strong>
            <small>{{ item.username }}</small>
          </span>
        </RouterLink>
      </div>
    </aside>

    <section class="chat-panel">
      <header class="chat-header">
        <div>
          <h2>{{ friend ? friend.nickname : "请选择好友" }}</h2>
          <p>
            {{ friend ? friend.username : "从好友列表打开聊天窗口" }}
            <span v-if="friend" class="socket-status" :class="socketStatus">
              {{ socketStatus === "connected" ? "实时在线" : socketStatus === "connecting" ? "连接中" : "实时离线" }}
            </span>
          </p>
        </div>
        <button class="button" type="button" @click="refreshMessages">刷新记录</button>
      </header>

      <p v-if="feedback" class="notice error">{{ feedback }}</p>

      <div ref="messageList" class="message-list">
        <p v-if="!loading && messages.length === 0" class="empty">暂无聊天记录</p>
        <article
          v-for="message in messages"
          :key="message.id"
          class="chat-message"
          :class="{ mine: isMine(message) }"
        >
          <div class="message-bubble">
            <p>{{ message.content }}</p>
            <span>{{ formatTime(message.createdAt) }}</span>
          </div>
        </article>
      </div>

      <form class="composer" @submit.prevent="submitMessage">
        <textarea
          v-model="draft"
          maxlength="1000"
          placeholder="输入消息"
          rows="3"
          @keydown.enter.exact.prevent="submitMessage"
        ></textarea>
        <button class="button primary" type="submit" :disabled="sending || !draft.trim()">
          {{ sending ? "发送中" : "发送" }}
        </button>
      </form>
    </section>
  </main>
</template>
