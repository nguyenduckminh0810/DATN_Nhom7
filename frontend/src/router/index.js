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
router.beforeEach(async (to, from, next) => {
  // Chặn quay lại trang Play của attempt vừa hoàn thành (client-side)
  if (to.name === 'PlayQuiz') {
    const quizId = to.params.quizId
    const userIdParam = to.params.userId
    const completedKey = `quiz_completed_${quizId}_${userIdParam}`
    try {
      // Cho phép bypass khi có retake=1
      if (to.query && (to.query.retake === '1' || to.query.retake === 1)) {
        localStorage.removeItem(completedKey)
        return next()
      }
      const done = localStorage.getItem(completedKey) === '1'
      if (done) {
        return next({ name: 'QuizResult', params: { quizId, userId: userIdParam } })
      }
    } catch { }
  }
  const token = localStorage.getItem('token')
  const adminUser = localStorage.getItem('admin_user')
  const userInfo = localStorage.getItem('user')
  const userRole = userInfo ? JSON.parse(userInfo).role : null

  console.log('🔍 Navigation guard - Route:', to.path)
  console.log('🔍 Token exists:', !!token)
  console.log('🔍 Admin user exists:', !!adminUser)
  console.log('🔍 User role:', userRole)

  // ✅ PUBLIC ROUTES - Always accessible
  if (!to.meta.requiresAuth) {
    console.log('✅ Public route - allowing access')
    return next()
  }

  // ✅ AUTHENTICATION CHECK
  if (!token && !adminUser) {
    console.log('❌ No authentication - redirecting to login')
    return next({ name: 'Login' })
  }

  // ✅ ATTEMPT ROUTE CHECK (PlayAttempt): chặn vào attempt đã hoàn thành
  if (to.name === 'PlayAttempt') {
    try {
      console.log('🔍 Checking PlayAttempt status for attemptId:', to.params.attemptId)

      // Kiểm tra xem attemptId có hợp lệ không
      if (!to.params.attemptId) {
        console.log('❌ No attemptId provided, redirecting to Home')
        return next({ name: 'Home' })
      }

      // Thêm timeout để tránh chờ quá lâu
      const statusPromise = quizAttemptService.getAttemptStatus(to.params.attemptId)
      const timeoutPromise = new Promise((_, reject) =>
        setTimeout(() => reject(new Error('Timeout')), 5000),
      )

      const { status } = await Promise.race([statusPromise, timeoutPromise])
      console.log('🔍 Attempt status:', status)

      if (['SUBMITTED', 'COMPLETED', 'CANCELLED', 'EXPIRED'].includes(status)) {
        console.log('❌ Attempt already completed, redirecting to Home')
        return next({ name: 'Home' })
      }

      console.log('✅ Attempt status valid, allowing access')
    } catch (e) {
      console.error('❌ Error checking attempt status:', e)

      // Nếu có lỗi khi kiểm tra status, kiểm tra xem có phải lỗi network không
      if (
        e.code === 'NETWORK_ERROR' ||
        e.message?.includes('Network Error') ||
        e.message?.includes('Timeout')
      ) {
        console.log('⚠️ Network error or timeout, allowing access to avoid infinite redirect')
        return next()
      }

      // Nếu là lỗi khác, vẫn cho phép vào để tránh redirect vô hạn
      console.log('⚠️ Allowing access despite status check error')
      return next()
    }
  }

  // ✅ ADMIN ROUTES CHECK
  if (to.meta.requiresAdmin === true) {
    const isAdmin = adminUser || userRole === 'admin' || userRole === 'ADMIN'
    console.log(
      '🔍 Admin check - adminUser:',
      !!adminUser,
      'userRole:',
      userRole,
      'isAdmin:',
      isAdmin,
    )
    console.log('🔍 Route meta:', to.meta)
    console.log('🔍 RequiresAdmin:', to.meta.requiresAdmin)
    if (!isAdmin) {
      console.log('❌ Admin route but no admin user - redirecting to login')
      return next({ name: 'Login' })
    }
    console.log('✅ Admin route - allowing access')
    return next()
  }

  // ✅ USER ROUTES CHECK
  if (to.meta.requiresUser) {
    if (!token) {
      console.log('❌ User route but no token - redirecting to login')
      return next({ name: 'Login' })
    }
    console.log('✅ User route - allowing access')
    return next()
  }

  // ✅ GENERAL AUTHENTICATED ROUTES
  if (to.meta.requiresAuth && (token || adminUser)) {
    console.log('✅ Authenticated route - allowing access')
    return next()
  }

  // ✅ LOGIN REDIRECT LOGIC - Allow access to login page even if logged in
  if (to.name === 'Login') {
    console.log('✅ Login page - allowing access')
    return next()
  }

  console.log('✅ Default case - allowing access')
  next()
})

export default router
