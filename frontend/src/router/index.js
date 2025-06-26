import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // Route về trang đăng nhập
    {
      path: '/login',
      name: 'login',
      component: () => import('../components/client/Login.vue'),
    },
    // Route về trang đăng ký
    {
      path: '/register',
      name: 'register',
      component: () => import('../components/client/Register.vue'),
    },
  ],
})

export default router
