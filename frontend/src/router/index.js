import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // Route về trang chủ
    {
      path: '/home',
      name: 'home',
      component: () => import('../components/client/BaseView.vue'),
    },
    // Route về trang đăng nhập
    {
      path: '/login',
      name: 'login',
      component: () => import('../components/client/Login.vue'),
    },
  ],
})

export default router
