import { createRouter, createWebHistory } from 'vue-router'

// Layouts
import AdminLayout from '@/layouts/AdminLayout.vue'
import ClientLayout from '@/layouts/ClientLayout.vue'

// Admin views
import AdminDashboard from '@/components/admin/AdminDashboard.vue'
import AdminQuizAttempts from '@/components/admin/AdminQuizAttempts.vue'
import AdminReport from '@/components/admin/AdminReport.vue'
import AdminAnalytics from '@/components/admin/AdminAnalytics.vue'

// Client views
import Login from '@/components/client/Login.vue'
import Register from '@/components/client/Register.vue'
import QuizCRUD from '@/components/client/QuizCRUD.vue'
import PlayQuiz from '@/components/client/PlayQuiz.vue'
import EditQuiz from '@/components/client/editQuiz.vue'
import QuizResult from '@/components/client/QuizResult.vue'
import QuizHistory from '@/components/client/QuizHistory.vue'
import QuizHistoryModern from '@/components/QuizHistoryModern.vue'
import ClientDashboard from '@/components/client/ClientDashboard.vue'
import CategoryManager from '@/components/client/CategoryManager.vue'
import CategoryView from '@/components/client/CategoryView.vue'
import CategoryTrash from '@/components/admin/CategoryTrash.vue'
import UserQuizHistory from '@/components/client/UserQuizHistory.vue'
import UserProfile from '@/components/client/UserProfile.vue'
import UserManager from '@/components/admin/UserManager.vue'
import QuizManager from '@/components/admin/QuizManager.vue'
import QuizTrash from '@/components/client/QuizTrash.vue'
import JoinQuiz from '@/components/client/JoinQuiz.vue'

import UserProfilePage from '@/components/client/UserProfilePage.vue'

import Home from '@/components/client/Home.vue'
import Contact from '@/components/client/Contact.vue'
import ListUserQuiz from '@/components/client/ListUserQuiz.vue'
import ListQuizPublic from '@/components/client/ListQuizPublic.vue'
import ImportExcel from '@/components/client/ImportExcel.vue'

import ForgotPassword from '@/components/client/ForgotPassword.vue'
import ResetPassword from '@/components/client/ResetPassword.vue'
import { quizAttemptService } from '@/services/quizAttemptService'

import Leaderboard from '@/components/client/Leaderboard.vue'
import GlobalLeaderboardPage from '@/components/client/GlobalLeaderboardPage.vue'

const routes = [
  // ✅ PUBLIC ROUTES
  {
    path: '/',
    component: ClientLayout,
    children: [
      {
        path: '',
        redirect: { name: 'Home' },
      },
      {
        path: 'home',
        name: 'Home',
        component: Home,
      },
      {
        path: 'login',
        name: 'Login',
        component: Login,
      },
      {
        path: 'register',
        name: 'Register',
        component: Register,
      },
      {
        path: 'forgot-password',
        name: 'ForgotPassword',
        component: ForgotPassword,
      },
      {
        path: 'reset-password',
        name: 'ResetPassword',
        component: ResetPassword,
      },
      {
        path: 'leaderboard',
        name: 'Leaderboard',
        component: Leaderboard,
      },
      {
        path: 'global-leaderboard',
        name: 'GlobalLeaderboard',
        component: GlobalLeaderboardPage,
        meta: { requiresAuth: true, requiresUser: true },
      },
      {
        path: 'contact',
        name: 'Contact',
        component: Contact,
      },
      { path: 'ban', name: 'Ban', component: () => import('@/components/BanPage.vue') },
    ],
  },

  // ✅ USER ROUTES (requires authentication)
  {
    path: '/',
    component: ClientLayout,
    children: [

      {
        path: 'dashboard',
        name: 'Dashboard',
        component: ClientDashboard,
        props: (route) => ({ userId: localStorage.getItem('userId') }),
        meta: { requiresAuth: true, requiresUser: true },
      },
      {
        path: 'quiz-crud',
        name: 'QuizCRUD',
        component: QuizCRUD,
        meta: { requiresAuth: true, requiresUser: true },
      },
      {
        path: 'quiz/:quizId/:userId/play',
        name: 'PlayQuiz',
        component: PlayQuiz,
        meta: { requiresAuth: true, requiresUser: true },
      },
      {
        path: 'attempt/:attemptId/play',
        name: 'PlayAttempt',
        component: PlayQuiz,
        meta: { requiresAuth: true, requiresUser: true },
      },
      {
        path: 'quiz-crud/edit/:userId/:quizId',
        name: 'EditQuiz',
        component: EditQuiz,
        meta: { requiresAuth: true, requiresUser: true },
      },
      {
        path: 'result/:resultId',
        name: 'QuizResult',
        component: QuizResult,
        meta: { requiresAuth: true, requiresUser: true },
      },
      {
        path: 'history/:userId',
        name: 'QuizHistory',
        component: QuizHistory,
        meta: { requiresAuth: true, requiresUser: true },
      },
      {
        path: 'my-quizzes',
        name: 'ListUserQuiz',
        component: ListUserQuiz,
        meta: { requiresAuth: true, requiresUser: true },
      },
      {
        path: 'public-quizzes',
        name: 'ListQuizPublic',
        component: ListQuizPublic,
        meta: { requiresAuth: true, requiresUser: true },
      },
      {
        path: 'join-quiz',
        name: 'JoinQuiz',
        component: JoinQuiz,
        meta: { requiresAuth: true, requiresUser: true },
      },
      {
        path: 'import-excel',
        name: 'ImportExcel',
        component: ImportExcel,
        meta: { requiresAuth: true, requiresUser: true },
      },
      {
        path: '/categories/:id',
        name: 'CategoryQuizzes',
        component: () => import('@/components/client/CategoryQuizzes.vue'),
        props: true,
        meta: { requiresAuth: true, requiresUser: true },
      },
      {
        path: 'trash',
        name: 'QuizTrash',
        component: QuizTrash,
        meta: { requiresAuth: true, requiresUser: true },
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: UserProfile,
        meta: { requiresAuth: true, requiresUser: true },
      },
      {
        path: 'profile/:userId',
        name: 'UserProfilePage',
        component: UserProfilePage,
        meta: { requiresAuth: true, requiresUser: true },
      },
      {
        path: 'categories',
        name: 'CategoryView',
        component: CategoryView,
        meta: { requiresAuth: true, requiresUser: true },
      },
      {
        path: 'history',
        name: 'UserQuizHistory',
        component: QuizHistoryModern,
        meta: { requiresAuth: true, requiresUser: true },
      },
    ],
  },

  // ✅ ADMIN ROUTES (requires admin authentication)
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        redirect: { name: 'AdminDashboard' },
      },

      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: AdminDashboard,
        meta: { requiresAuth: true, requiresAdmin: true },
      },
      {
        path: 'all-users',
        name: 'UserManager',
        component: UserManager,
        meta: { requiresAuth: true, requiresAdmin: true },
      },
      {
        path: 'all-quizzes',
        name: 'QuizManager',
        component: QuizManager,
        meta: { requiresAuth: true, requiresAdmin: true },
      },
      {
        path: 'attempts',
        name: 'AdminAttempts',
        component: QuizHistoryModern,
        props: { title: 'Quản lý lịch sử làm quiz', showUserFilter: true },
        meta: { requiresAuth: true, requiresAdmin: true },
      },
      {
        path: 'reports',
        name: 'AdminReport',
        component: AdminReport,
        meta: { requiresAuth: true, requiresAdmin: true },
      },
      {
        path: 'analytics',
        name: 'AdminAnalytics',
        component: AdminAnalytics,
        meta: { requiresAuth: true, requiresAdmin: true },
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: CategoryManager,
        meta: { requiresAuth: true, requiresAdmin: true },
      },
      {
        path: 'categories/trash',
        name: 'CategoryTrash',
        component: CategoryTrash,
        meta: { requiresAuth: true, requiresAdmin: true },
      },
    ],
  },

  // ✅ CATCH-ALL ROUTE
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: { name: 'Home' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// ✅ NAVIGATION GUARDS
router.beforeEach((to, from, next) => {
  const ALWAYS_ALLOW = new Set(['Ban', 'Login', 'Register', 'ForgotPassword', 'ResetPassword', 'Home', 'Contact'])

  const token = localStorage.getItem('token')
  const adminUser = localStorage.getItem('admin_user')
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  const role = (user?.role || '').toUpperCase()
  const isAdmin = !!adminUser || role === 'ADMIN'
  const isBanned = localStorage.getItem('banned') === '1' || role === 'BANNED'

  // ✅ Trang Login: nếu đã đăng nhập thì đẩy ra Dashboard phù hợp
  if (to.name === 'Login') {
    if (token || adminUser) {
      if (isBanned) return next({ name: 'Ban' })
      return next({ name: isAdmin ? 'AdminDashboard' : 'Dashboard' })
    }
    return next() // chưa login -> cho ở lại trang Login
  }

  // ✅ Trang Ban: luôn cho vào
  if (to.name === 'Ban') return next()

  // ✅ Các trang public khác
  if (ALWAYS_ALLOW.has(to.name)) return next()

  // 🔒 Nếu bị ban -> đẩy về /ban
  if (isBanned) return next({ name: 'Ban' })

  // 🔐 Phần còn lại giữ nguyên
  if (!to.meta.requiresAuth) return next()

  if (!token && !adminUser) return next({ name: 'Login' })

  if (to.meta.requiresAdmin === true) {
    if (!isAdmin) return next({ name: 'Login' })
    return next()
  }

  if (to.meta.requiresUser) {
    if (!token && !adminUser) return next({ name: 'Login' })
    return next()
  }

  return next()
})



export default router
