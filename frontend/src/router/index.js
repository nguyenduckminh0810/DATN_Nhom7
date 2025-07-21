import { createRouter, createWebHistory } from 'vue-router'

// Layouts
import AdminLayout from '@/layouts/AdminLayout.vue'
import ClientLayout from '@/layouts/ClientLayout.vue'

// Admin views
import LoginAdmin from '@/components/admin/LoginAdmin.vue'
import AdminDashboard from '@/components/admin/AdminDashboard.vue'
import AdminQuizHistory from '@/components/admin/AdminQuizHistory.vue'
import AdminQuizAttempts from '@/components/admin/AdminQuizAttempts.vue'

// Client views
import Login from '@/components/client/Login.vue'
import Register from '@/components/client/Register.vue'
import QuizCRUD from '@/components/client/QuizCRUD.vue'
import PlayQuiz from '@/components/client/PlayQuiz.vue'
import EditQuiz from '@/components/client/editQuiz.vue'
import QuizResult from '@/components/client/QuizResult.vue'
import QuizHistory from '@/components/client/QuizHistory.vue'
import ClientDashboard from '@/components/client/ClientDashboard.vue'

import UserQuizHistory from '@/components/client/UserQuizHistory.vue'
import UserManager from '@/components/admin/UserManager.vue'
import QuizManager from '@/components/admin/QuizManager.vue'


const routes = [
  // Client layout và các route người dùng
  {
    path: '/',
    component: ClientLayout,
    children: [
      { path: 'login', name: 'Login', component: Login },
      { path: 'register', name: 'Register', component: Register },
      { path: 'client-dashboard/:userId', name: 'ClientDashboard', component: ClientDashboard },
      { path: 'quiz-crud', name: 'QuizCRUD', component: QuizCRUD },
      { path: 'quiz/:quizId/:userId/play', name: 'PlayQuiz', component: PlayQuiz },
      { path: 'quiz-crud/edit/:userId/:quizId', name: 'EditQuiz', component: EditQuiz },
      { path: 'quiz/:quizId/:userId/result', name: 'QuizResult', component: QuizResult },
      { path: 'history/:userId', name: 'QuizHistory', component: QuizHistory },
      { path: 'my-history', name: 'UserQuizHistory', component: UserQuizHistory },
    ],
  },

  //Route đăng nhập của admin
  { path: '/admin/login', name: 'AdminLogin', component: LoginAdmin },

  // Admin layout và các route quản trị
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      { path: 'dashboard', name: 'AdminDashboard', component: AdminDashboard },

      { path: 'attempts', name: 'AdminQuizAttempts', component: AdminQuizAttempts },
      { path: 'all-users', name: 'UserManager', component: UserManager },
      { path: 'all-quizzes', name: 'QuizManager', component: QuizManager },
      // { path: 'quiz-attempts', name: 'QuizAttemptManager', component: QuizAttemtpManager },
      // { path: 'categories', name: 'CategoryManager', component: CategoryManager },
      // { path: 'quiz-approval', name: 'QuizApprovalManager', component: QuizApprovalManager },
      // { path: 'reports', name: 'ReportManager', component: ReportManager },
    ],
  },

  // Route cho lịch sử quiz của user
  { path: '/my-history', name: 'UserQuizHistory', component: UserQuizHistory },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
