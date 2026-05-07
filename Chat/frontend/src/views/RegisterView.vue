<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { registerUser } from "@/api/auth";

const router = useRouter();
const form = reactive({
  username: "",
  nickname: "",
  password: ""
});
const message = ref("");
const isSuccess = ref(false);
const isSubmitting = ref(false);

async function submit() {
  message.value = "";
  isSuccess.value = false;
  isSubmitting.value = true;

  try {
    await registerUser(form);
    isSuccess.value = true;
    message.value = "注册成功，正在前往登录";
    window.setTimeout(() => router.push("/login"), 600);
  } catch (error) {
    message.value = error.message || "注册失败";
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <main class="auth-shell">
    <form class="auth-panel auth-form" @submit.prevent="submit">
      <div>
        <p class="eyebrow">Create account</p>
        <h1>注册</h1>
      </div>

      <label>
        <span>用户名</span>
        <input v-model.trim="form.username" autocomplete="username" required minlength="3" maxlength="32">
      </label>

      <label>
        <span>昵称</span>
        <input v-model.trim="form.nickname" autocomplete="nickname" required maxlength="30">
      </label>

      <label>
        <span>密码</span>
        <input v-model="form.password" type="password" autocomplete="new-password" required minlength="6">
      </label>

      <p class="message" :class="{ success: isSuccess }">{{ message }}</p>

      <button class="button primary" type="submit" :disabled="isSubmitting">
        {{ isSubmitting ? "注册中" : "注册" }}
      </button>

      <RouterLink class="link" to="/login">已有账号？去登录</RouterLink>
    </form>
  </main>
</template>
