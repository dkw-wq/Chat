<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { loginUser } from "@/api/auth";

const router = useRouter();
const form = reactive({
  username: "",
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
    await loginUser(form);
    isSuccess.value = true;
    message.value = "登录成功，正在进入系统";
    window.setTimeout(() => router.push("/home"), 400);
  } catch (error) {
    message.value = error.message || "登录失败";
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <main class="auth-shell">
    <form class="auth-panel auth-form" @submit.prevent="submit">
      <div>
        <p class="eyebrow">Welcome back</p>
        <h1>登录</h1>
      </div>

      <label>
        <span>用户名</span>
        <input v-model.trim="form.username" autocomplete="username" required>
      </label>

      <label>
        <span>密码</span>
        <input v-model="form.password" type="password" autocomplete="current-password" required>
      </label>

      <p class="message" :class="{ success: isSuccess }">{{ message }}</p>

      <button class="button primary" type="submit" :disabled="isSubmitting">
        {{ isSubmitting ? "登录中" : "登录" }}
      </button>

      <RouterLink class="link" to="/register">还没有账号？去注册</RouterLink>
    </form>
  </main>
</template>
