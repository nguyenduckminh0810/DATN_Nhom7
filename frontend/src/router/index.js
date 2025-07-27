import { createRouter, createWebHistory } from 'vue-router'

// Layouts
import AdminLayout from '@/layouts/AdminLayout.vue'
import ClientLayout from '@/layouts/ClientLayout.vue'

// Admin views
import LoginAdmin from '@/components/admin/LoginAdmin.vue'
import AdminDashboard from '@/components/admin/AdminDashboard.vue'
import AdminQuizAttempts from '@/components/admin/AdminQuizAttempts.vue'
import AdminReport from '@/components/admin/AdminReport.vue'

// Client views
import Login from '@/components/client/Login.vue'
import Register from '@/components/client/Register.vue'
import QuizCRUD from '@/components/client/QuizCRUD.vue'
import PlayQuiz from '@/components/client/PlayQuiz.vue'
import EditQuiz from '@/components/client/editQuiz.vue'
import QuizResult from '@/components/client/QuizResult.vue'
import QuizHistory from '@/components/client/QuizHistory.vue'
import ClientDashboard from '@/components/client/ClientDashboard.vue'
import CategoryManager from '@/components/client/CategoryManager.vue'
import UserQuizHistory from '@/components/client/UserQuizHistory.vue'
import UserProfile from '@/components/client/UserProfile.vue'
import UserManager from '@/components/admin/UserManager.vue'
import QuizManager from '@/components/admin/QuizManager.vue'

import UserProfilePage from '@/components/client/UserProfilePage.vue'

import Home from '@/components/client/Home.vue'
import ListUserQuiz from '@/components/client/ListUserQuiz.vue'
import ListQuizPublic from '@/components/client/ListQuizPublic.vue'


const routes = [
  // Client layout và các route người dùng
  {
    path: '/',
    component: ClientLayout,
    children: [
      { path: '', redirect: { name: 'Home' } },
      { path: 'home', name: 'Home', component: Home },
      { path: 'login', name: 'Login', component: Login },
      { path: 'register', name: 'Register', component: Register },
      { path: 'client-dashboard/:userId', name: 'ClientDashboard', component: ClientDashboard },
      { path: 'quiz-crud', name: 'QuizCRUD', component: QuizCRUD },
      { path: 'quiz/:quizId/:userId/play', name: 'PlayQuiz', component: PlayQuiz },
      { path: 'quiz-crud/edit/:userId/:quizId', name: 'EditQuiz', component: EditQuiz },
      { path: 'quiz/:quizId/:userId/result', name: 'QuizResult', component: QuizResult },
      { path: 'history/:userId', name: 'QuizHistory', component: QuizHistory },
      { path: 'my-history', name: 'UserQuizHistory', component: UserQuizHistory },
      { path: 'profile', name: 'UserProfile', component: UserProfile },
      { path: '/categories', name: 'CategoryManager', component: CategoryManager },
      { path: 'my-quizzes', name: 'ListUserQuiz', component: ListUserQuiz },
      { path: 'public-quizzes', name: 'ListQuizPublic', component: ListQuizPublic },
      { path: 'profile/:id', name: 'UserProfilePage', component: UserProfilePage },
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
      { path: 'reports', name: 'AdminReport', component: AdminReport },

      // { path: 'quiz-attempts', name: 'QuizAttemptManager', component: QuizAttemtpManager },
      { path: 'categories', name: 'CategoryManager', component: CategoryManager },
      // { path: 'quiz-approval', name: 'QuizApprovalManager', component: QuizApprovalManager },
      // { path: 'reports', name: 'ReportManager', component: ReportManager },
    ],
  },

  // Route cho lịch sử quiz của user (duplicate removed - already exists in ClientLayout)
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
