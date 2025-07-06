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
    // làm thử cắt layout
    // Cắt layout thành admin layout và client layout
    // Gán admin layout.vue cho các route liên quan đến quản trị viên
    // Gán client layout.vue cho các route liên quan đến người dùng
    // Route về trang đăng nhập admin
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../components/admin/LoginAdmin.vue'),
    },
    // Route về trang quản lý quiz
    {
      path: '/quiz-crud',
      name: 'quizcrud',
      component: () => import('../components/client/QuizCRUD.vue')
    },
    {
      path: '/quiz/:quizId/:userId/play',
      name: 'PlayQuiz',
      component: () => import('../components/client/PlayQuiz.vue')
    },

    {
      path: '/quiz-crud/edit/:userId/:quizId',
      name: 'EditQuiz',
      component: () => import('../components/client/editQuiz.vue')
    },
    {
      path: '/quiz/:quizId/:userId/result',
      name: 'QuizResult',
      component: () => import('../components/client/QuizResult.vue')
    },
    {
      path: '/history/:userId',
      name: 'quizHistory',
      component: () => import('../components/client/QuizHistory.vue')
    }


  ],
})

export default router
