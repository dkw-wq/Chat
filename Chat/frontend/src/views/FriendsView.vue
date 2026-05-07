<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { getCurrentUser, logoutUser } from "@/api/auth";
import {
  acceptFriendRequest,
  deleteFriend,
  listFriends,
  listIncomingRequests,
  listOutgoingRequests,
  rejectFriendRequest,
  searchUsers,
  sendFriendRequest
} from "@/api/friends";

const router = useRouter();
const currentUser = ref(null);
const friends = ref([]);
const incomingRequests = ref([]);
const outgoingRequests = ref([]);
const searchResults = ref([]);
const searchKeyword = ref("");
const requestMessage = ref("你好，我想添加你为好友");
const feedback = reactive({
  type: "",
  text: ""
});
const loading = reactive({
  page: true,
  search: false,
  friends: false,
  requests: false
});

const pendingIncoming = computed(() => incomingRequests.value.filter((item) => item.status === "PENDING"));
const pendingOutgoing = computed(() => outgoingRequests.value.filter((item) => item.status === "PENDING"));

function showMessage(text, type = "success") {
  feedback.text = text;
  feedback.type = type;
}

async function loadPage() {
  loading.page = true;
  try {
    currentUser.value = await getCurrentUser();
    await Promise.all([loadFriends(), loadRequests()]);
  } catch {
    router.replace("/login");
  } finally {
    loading.page = false;
  }
}

async function loadFriends() {
  loading.friends = true;
  try {
    friends.value = await listFriends();
  } finally {
    loading.friends = false;
  }
}

async function loadRequests() {
  loading.requests = true;
  try {
    const [incoming, outgoing] = await Promise.all([
      listIncomingRequests(),
      listOutgoingRequests()
    ]);
    incomingRequests.value = incoming;
    outgoingRequests.value = outgoing;
  } finally {
    loading.requests = false;
  }
}

async function submitSearch() {
  const keyword = searchKeyword.value.trim();
  if (!keyword) {
    searchResults.value = [];
    showMessage("请输入用户名或昵称", "error");
    return;
  }

  loading.search = true;
  feedback.text = "";
  try {
    searchResults.value = await searchUsers(keyword);
    if (searchResults.value.length === 0) {
      showMessage("没有找到匹配用户", "error");
    }
  } catch (error) {
    showMessage(error.message || "搜索失败", "error");
  } finally {
    loading.search = false;
  }
}

async function addFriend(user) {
  try {
    await sendFriendRequest({
      receiverId: user.id,
      message: requestMessage.value
    });
    user.relationshipStatus = "PENDING_SENT";
    showMessage("好友申请已发送");
    await loadRequests();
  } catch (error) {
    showMessage(error.message || "发送失败", "error");
  }
}

async function acceptRequest(request) {
  try {
    await acceptFriendRequest(request.id);
    showMessage("已同意好友申请");
    await Promise.all([loadFriends(), loadRequests()]);
  } catch (error) {
    showMessage(error.message || "处理失败", "error");
  }
}

async function rejectRequest(request) {
  try {
    await rejectFriendRequest(request.id);
    showMessage("已拒绝好友申请");
    await loadRequests();
  } catch (error) {
    showMessage(error.message || "处理失败", "error");
  }
}

async function removeFriend(friend) {
  try {
    await deleteFriend(friend.id);
    showMessage("已删除好友");
    await loadFriends();
  } catch (error) {
    showMessage(error.message || "删除失败", "error");
  }
}

async function logout() {
  await logoutUser();
  router.replace("/login");
}

function statusText(status) {
  const labels = {
    FRIEND: "已是好友",
    PENDING_SENT: "已发送申请",
    PENDING_RECEIVED: "待你处理",
    NONE: "可添加"
  };
  return labels[status] || status;
}

onMounted(loadPage);
</script>

<template>
  <main class="workspace-shell">
    <header class="topbar">
      <div>
        <p class="eyebrow">Friends</p>
        <h1>好友管理</h1>
        <p class="copy">
          {{ currentUser ? `当前账号：${currentUser.username}` : "正在读取用户信息" }}
        </p>
      </div>
      <div class="actions">
        <RouterLink class="button" to="/home">首页</RouterLink>
        <button class="button" type="button" @click="logout">退出</button>
      </div>
    </header>

    <p v-if="feedback.text" class="notice" :class="{ error: feedback.type === 'error' }">
      {{ feedback.text }}
    </p>

    <section class="workspace-grid">
      <section class="tool-panel">
        <div class="section-heading">
          <h2>添加好友</h2>
          <p>通过用户名或昵称查找用户并发送验证请求。</p>
        </div>

        <form class="search-row" @submit.prevent="submitSearch">
          <input v-model="searchKeyword" placeholder="输入用户名或昵称" maxlength="32">
          <button class="button primary" type="submit" :disabled="loading.search">
            {{ loading.search ? "搜索中" : "搜索" }}
          </button>
        </form>

        <label>
          <span>验证消息</span>
          <input v-model="requestMessage" maxlength="255">
        </label>

        <div class="list">
          <article v-for="user in searchResults" :key="user.id" class="list-item">
            <div class="avatar">{{ user.nickname.slice(0, 1).toUpperCase() }}</div>
            <div>
              <strong>{{ user.nickname }}</strong>
              <p>{{ user.username }} · {{ statusText(user.relationshipStatus) }}</p>
            </div>
            <button
              class="button"
              type="button"
              :disabled="user.relationshipStatus !== 'NONE'"
              @click="addFriend(user)"
            >
              添加
            </button>
          </article>
        </div>
      </section>

      <section class="tool-panel">
        <div class="section-heading">
          <h2>好友列表</h2>
          <p>{{ friends.length }} 位好友</p>
        </div>

        <div class="list">
          <p v-if="!loading.friends && friends.length === 0" class="empty">暂无好友</p>
          <article v-for="friend in friends" :key="friend.id" class="list-item">
            <div class="avatar">{{ friend.nickname.slice(0, 1).toUpperCase() }}</div>
            <div>
              <strong>{{ friend.nickname }}</strong>
              <p>{{ friend.username }}</p>
            </div>
            <div class="actions">
              <RouterLink class="button primary" :to="`/chat/${friend.id}`">聊天</RouterLink>
              <button class="button" type="button" @click="removeFriend(friend)">删除</button>
            </div>
          </article>
        </div>
      </section>

      <section class="tool-panel">
        <div class="section-heading">
          <h2>收到的申请</h2>
          <p>{{ pendingIncoming.length }} 条待处理</p>
        </div>

        <div class="list">
          <p v-if="pendingIncoming.length === 0" class="empty">暂无待处理申请</p>
          <article v-for="request in pendingIncoming" :key="request.id" class="request-item">
            <div>
              <strong>{{ request.requesterNickname }}</strong>
              <p>{{ request.requesterUsername }}：{{ request.message || "请求添加你为好友" }}</p>
            </div>
            <div class="actions">
              <button class="button primary" type="button" @click="acceptRequest(request)">同意</button>
              <button class="button" type="button" @click="rejectRequest(request)">拒绝</button>
            </div>
          </article>
        </div>
      </section>

      <section class="tool-panel">
        <div class="section-heading">
          <h2>发出的申请</h2>
          <p>{{ pendingOutgoing.length }} 条等待对方处理</p>
        </div>

        <div class="list">
          <p v-if="pendingOutgoing.length === 0" class="empty">暂无待处理申请</p>
          <article v-for="request in pendingOutgoing" :key="request.id" class="request-item">
            <div>
              <strong>{{ request.receiverNickname }}</strong>
              <p>{{ request.receiverUsername }}：{{ request.message || "等待验证" }}</p>
            </div>
          </article>
        </div>
      </section>
    </section>
  </main>
</template>
