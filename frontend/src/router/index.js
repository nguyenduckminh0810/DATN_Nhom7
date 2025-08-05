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
import ListUserQuiz from '@/components/client/ListUserQuiz.vue'
import ListQuizPublic from '@/components/client/ListQuizPublic.vue'
import ImportExcel from '@/components/client/ImportExcel.vue'

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
        path: 'contact',
        name: 'Contact',
        component: Home, // TODO: Create Contact component
      },
    ]
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
        path: 'quiz-crud/edit/:userId/:quizId',
        name: 'EditQuiz',
        component: EditQuiz,
        meta: { requiresAuth: true, requiresUser: true },
      },
      {
        path: 'quiz/:quizId/:userId/result',
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
        component: UserQuizHistory,
        meta: { requiresAuth: true, requiresUser: true },
      },
    ]
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
        component: AdminQuizAttempts,
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
    ]
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
  
  // ✅ ADMIN ROUTES CHECK
  if (to.meta.requiresAdmin === true) {
    const isAdmin = adminUser || (userRole === 'admin' || userRole === 'ADMIN')
    console.log('🔍 Admin check - adminUser:', !!adminUser, 'userRole:', userRole, 'isAdmin:', isAdmin)
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

