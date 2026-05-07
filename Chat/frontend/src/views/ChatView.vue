<script setup>
import { computed, nextTick, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
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
const messageList = ref(null);

const friendIdNumber = computed(() => Number(props.friendId));

function isMine(message) {
  return currentUser.value && message.senderId === currentUser.value.id;
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
    const message = await sendChatMessage(friend.value.id, content);
    messages.value.push(message);
    draft.value = "";
    await scrollToBottom();
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

async function logout() {
  await logoutUser();
  router.replace("/login");
}

onMounted(loadPage);
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
          <span class="avatar">{{ item.nickname.slice(0, 1).toUpperCase() }}</span>
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
          <p>{{ friend ? friend.username : "从好友列表打开聊天窗口" }}</p>
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
