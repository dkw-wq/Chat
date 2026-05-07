import { createRouter, createWebHistory } from "vue-router";
import ChatView from "@/views/ChatView.vue";
import FriendsView from "@/views/FriendsView.vue";
import HomeView from "@/views/HomeView.vue";
import ImpressionsView from "@/views/ImpressionsView.vue";
import LoginView from "@/views/LoginView.vue";
import RegisterView from "@/views/RegisterView.vue";
import WelcomeView from "@/views/WelcomeView.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      name: "welcome",
      component: WelcomeView
    },
    {
      path: "/login",
      name: "login",
      component: LoginView
    },
    {
      path: "/register",
      name: "register",
      component: RegisterView
    },
    {
      path: "/home",
      name: "home",
      component: HomeView
    },
    {
      path: "/friends",
      name: "friends",
      component: FriendsView
    },
    {
      path: "/impressions",
      name: "impressions",
      component: ImpressionsView
    },
    {
      path: "/chat/:friendId",
      name: "chat",
      component: ChatView,
      props: true
    }
  ]
});

export default router;
