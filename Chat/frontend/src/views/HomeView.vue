<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { getCurrentUser, logoutUser } from "@/api/auth";

const router = useRouter();
const user = ref(null);
const message = ref("正在读取用户信息");

async function loadCurrentUser() {
  try {
    user.value = await getCurrentUser();
  } catch {
    router.replace("/login");
  }
}

async function logout() {
  await logoutUser();
  router.replace("/login");
}

onMounted(loadCurrentUser);
</script>

<template>
  <main class="auth-shell">
    <section class="auth-panel">
      <div>
        <p class="eyebrow">Signed in</p>
        <h1>{{ user ? `${user.nickname}，你好` : "在线聊天系统" }}</h1>
        <p class="copy">{{ user ? `账号：${user.username}` : message }}</p>
      </div>

      <div class="actions">
        <RouterLink class="button primary" to="/friends">好友管理</RouterLink>
        <button class="button" type="button" @click="logout">退出登录</button>
      </div>
    </section>
  </main>
</template>
