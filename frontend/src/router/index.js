import { createRouter, createWebHistory } from 'vue-router'
import UserSearchPage from '@/views/UserSearchPage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../components/client/Home.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../components/client/Login.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../components/client/Register.vue'),
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../components/admin/LoginAdmin.vue'),
    },
    {
      path: '/quiz-crud',
      name: 'quizcrud',
      component: () => import('../components/client/QuizCRUD.vue'),
    },
    {
      path: '/users',
      name: 'usersearch',
      component: UserSearchPage,
    },
  ],
})

export default router
