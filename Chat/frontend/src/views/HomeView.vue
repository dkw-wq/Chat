<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { getCurrentUser, logoutUser } from "@/api/auth";
import { uploadAvatar } from "@/api/users";

const router = useRouter();
const user = ref(null);
const message = ref("正在读取用户信息");
const avatarMessage = ref("");
const uploading = ref(false);

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

async function handleAvatarChange(event) {
  const file = event.target.files?.[0];
  if (!file) {
    return;
  }

  avatarMessage.value = "";
  uploading.value = true;
  try {
    user.value = await uploadAvatar(file);
    avatarMessage.value = "头像已更新";
  } catch (error) {
    avatarMessage.value = error.message || "头像上传失败";
  } finally {
    uploading.value = false;
    event.target.value = "";
  }
}

onMounted(loadCurrentUser);
</script>

<template>
  <main class="auth-shell">
    <section class="auth-panel">
      <div>
        <p class="eyebrow">Signed in</p>
        <img v-if="user" class="profile-avatar-large" :src="user.avatarUrl" alt="">
        <h1>{{ user ? `${user.nickname}，你好` : "在线聊天系统" }}</h1>
        <p class="copy">{{ user ? `账号：${user.username}` : message }}</p>
      </div>

      <label v-if="user" class="upload-row">
        <span>{{ uploading ? "上传中" : "上传头像" }}</span>
        <input type="file" accept="image/png,image/jpeg,image/gif,image/webp" :disabled="uploading" @change="handleAvatarChange">
      </label>
      <p v-if="avatarMessage" class="message success">{{ avatarMessage }}</p>

      <div class="actions">
        <RouterLink class="button primary" to="/friends">好友管理</RouterLink>
        <RouterLink class="button" to="/impressions">好友印象</RouterLink>
        <button class="button" type="button" @click="logout">退出登录</button>
      </div>
    </section>
  </main>
</template>
