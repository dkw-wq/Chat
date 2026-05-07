<script setup>
import { ref } from "vue";
import { getMiniProfile } from "@/api/users";

const props = defineProps({
  userId: {
    type: Number,
    required: true
  }
});

const profile = ref(null);
const loading = ref(false);
const visible = ref(false);
let hideTimer = null;

function formatDate(value) {
  if (!value) {
    return "";
  }
  return new Date(value).toLocaleDateString("zh-CN");
}

async function show() {
  if (hideTimer) {
    window.clearTimeout(hideTimer);
    hideTimer = null;
  }
  visible.value = true;
  if (profile.value || loading.value) {
    return;
  }

  loading.value = true;
  try {
    profile.value = await getMiniProfile(props.userId);
  } finally {
    loading.value = false;
  }
}

function hide() {
  hideTimer = window.setTimeout(() => {
    visible.value = false;
  }, 120);
}
</script>

<template>
  <span class="profile-hover" @mouseenter="show" @mouseleave="hide">
    <slot></slot>

    <span v-if="visible" class="mini-card" @mouseenter="show" @mouseleave="hide">
      <span v-if="loading" class="mini-loading">加载中</span>
      <template v-else-if="profile">
        <span class="mini-header">
          <img class="mini-avatar" :src="profile.avatarUrl" alt="">
          <span>
            <strong>{{ profile.nickname }}</strong>
            <small>{{ profile.username }}</small>
          </span>
        </span>
        <span class="mini-meta">用户编号：{{ profile.id }}</span>
        <span class="mini-meta">注册日期：{{ formatDate(profile.createdAt) }}</span>
        <span class="mini-badge">{{ profile.friend ? "好友" : "非好友" }}</span>
      </template>
    </span>
  </span>
</template>
