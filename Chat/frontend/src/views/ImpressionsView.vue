<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { getCurrentUser, logoutUser } from "@/api/auth";
import { listFriends } from "@/api/friends";
import {
  addImpression,
  listGivenImpressions,
  listReceivedImpressions
} from "@/api/impressions";

const router = useRouter();
const currentUser = ref(null);
const friends = ref([]);
const receivedImpressions = ref([]);
const givenImpressions = ref([]);
const form = reactive({
  targetId: "",
  content: ""
});
const feedback = reactive({
  type: "",
  text: ""
});
const loading = ref(true);
const submitting = ref(false);

const selectedFriend = computed(() => friends.value.find((item) => item.id === Number(form.targetId)));

function showMessage(text, type = "success") {
  feedback.text = text;
  feedback.type = type;
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

async function loadPage() {
  loading.value = true;
  feedback.text = "";

  try {
    currentUser.value = await getCurrentUser();
    await Promise.all([loadFriends(), loadImpressions()]);
  } catch {
    router.replace("/login");
  } finally {
    loading.value = false;
  }
}

async function loadFriends() {
  friends.value = await listFriends();
  if (!form.targetId && friends.value.length > 0) {
    form.targetId = String(friends.value[0].id);
  }
}

async function loadImpressions() {
  const [received, given] = await Promise.all([
    listReceivedImpressions(),
    listGivenImpressions()
  ]);
  receivedImpressions.value = received;
  givenImpressions.value = given;
}

async function submitImpression() {
  if (!form.targetId) {
    showMessage("请选择好友", "error");
    return;
  }
  if (!form.content.trim()) {
    showMessage("请输入评价内容", "error");
    return;
  }

  submitting.value = true;
  feedback.text = "";
  try {
    await addImpression({
      targetId: Number(form.targetId),
      content: form.content.trim()
    });
    form.content = "";
    showMessage("好友印象已添加");
    await loadImpressions();
  } catch (error) {
    showMessage(error.message || "添加失败", "error");
  } finally {
    submitting.value = false;
  }
}

async function logout() {
  await logoutUser();
  router.replace("/login");
}

onMounted(loadPage);
</script>

<template>
  <main class="workspace-shell">
    <header class="topbar">
      <div>
        <p class="eyebrow">Impressions</p>
        <h1>好友印象</h1>
        <p class="copy">
          {{ currentUser ? `当前账号：${currentUser.username}` : "正在读取用户信息" }}
        </p>
      </div>
      <div class="actions">
        <RouterLink class="button" to="/home">首页</RouterLink>
        <RouterLink class="button" to="/friends">好友管理</RouterLink>
        <button class="button" type="button" @click="logout">退出</button>
      </div>
    </header>

    <p v-if="feedback.text" class="notice" :class="{ error: feedback.type === 'error' }">
      {{ feedback.text }}
    </p>

    <section class="workspace-grid impression-grid">
      <section class="tool-panel">
        <div class="section-heading">
          <h2>添加好友印象</h2>
          <p>{{ selectedFriend ? `评价好友：${selectedFriend.nickname}` : "先添加好友后再评价" }}</p>
        </div>

        <form class="impression-form" @submit.prevent="submitImpression">
          <label>
            <span>选择好友</span>
            <select v-model="form.targetId">
              <option value="" disabled>请选择好友</option>
              <option v-for="friend in friends" :key="friend.id" :value="String(friend.id)">
                {{ friend.nickname }}（{{ friend.username }}）
              </option>
            </select>
          </label>

          <label>
            <span>评价内容</span>
            <textarea v-model="form.content" maxlength="255" rows="5" placeholder="例如：真诚、靠谱、回复很及时"></textarea>
          </label>

          <button class="button primary" type="submit" :disabled="submitting || friends.length === 0">
            {{ submitting ? "添加中" : "添加印象" }}
          </button>
        </form>
      </section>

      <section class="tool-panel">
        <div class="section-heading">
          <h2>好友给我的评价</h2>
          <p>{{ receivedImpressions.length }} 条评价</p>
        </div>

        <div class="list">
          <p v-if="!loading && receivedImpressions.length === 0" class="empty">暂无好友评价</p>
          <article v-for="impression in receivedImpressions" :key="impression.id" class="impression-item">
            <div>
              <strong>{{ impression.authorNickname }}</strong>
              <p>{{ impression.content }}</p>
            </div>
            <span>{{ formatTime(impression.createdAt) }}</span>
          </article>
        </div>
      </section>

      <section class="tool-panel span-two">
        <div class="section-heading">
          <h2>我给好友的评价</h2>
          <p>{{ givenImpressions.length }} 条评价</p>
        </div>

        <div class="list two-column-list">
          <p v-if="!loading && givenImpressions.length === 0" class="empty">暂无已添加评价</p>
          <article v-for="impression in givenImpressions" :key="impression.id" class="impression-item">
            <div>
              <strong>{{ impression.targetNickname }}</strong>
              <p>{{ impression.content }}</p>
            </div>
            <span>{{ formatTime(impression.createdAt) }}</span>
          </article>
        </div>
      </section>
    </section>
  </main>
</template>
