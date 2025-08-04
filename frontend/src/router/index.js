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
import QuizTrash from '@/components/client/QuizTrash.vue'
import JoinQuiz from '@/components/client/JoinQuiz.vue'

import UserProfilePage from '@/components/client/UserProfilePage.vue'

import Home from '@/components/client/Home.vue'
import ListUserQuiz from '@/components/client/ListUserQuiz.vue'
import ListQuizPublic from '@/components/client/ListQuizPublic.vue'
import ImportExcel from '@/components/client/ImportExcel.vue'

const routes = [
  // Client layout và các route người dùng
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
        path: 'client-dashboard/:userId',
        name: 'ClientDashboard',
        component: ClientDashboard,
        meta: { requiresAuth: true },
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: ClientDashboard,
        props: (route) => ({ userId: localStorage.getItem('userId') }),
        meta: { requiresAuth: true },
      },
      {
        path: 'quiz-crud',
        name: 'QuizCRUD',
        component: QuizCRUD,
        meta: { requiresAuth: true },
      },
      {
        path: 'quiz/:quizId/:userId/play',
        name: 'PlayQuiz',
        component: PlayQuiz,
        meta: { requiresAuth: true },
      },
      {
        path: 'quiz-crud/edit/:userId/:quizId',
        name: 'EditQuiz',
        component: EditQuiz,
        meta: { requiresAuth: true },
      },
      {
        path: 'quiz/:quizId/:userId/result',
        name: 'QuizResult',
        component: QuizResult,
        meta: { requiresAuth: true },
      },
      {
        path: 'history/:userId',
        name: 'QuizHistory',
        component: QuizHistory,
        meta: { requiresAuth: true },
      },
      {
        path: 'my-history',
        name: 'UserQuizHistory',
        component: UserQuizHistory,
        meta: { requiresAuth: true },
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: UserProfile,
        meta: { requiresAuth: true },
      },
      {
        path: 'categories',
        name: 'CategoryManager',
        component: CategoryManager,
        meta: { requiresAuth: true },
      },
      {
        path: 'my-quizzes',
        name: 'ListUserQuiz',
        component: ListUserQuiz,
        meta: { requiresAuth: true },
      },
      {
        path: 'public-quizzes',
        name: 'ListQuizPublic',
        component: ListQuizPublic,
      },
      {
        path: 'profile/:id',
        name: 'UserProfilePage',
        component: UserProfilePage,
        meta: { requiresAuth: true },
      },
      {
        path: 'import-excel',
        name: 'ImportExcel',
        component: ImportExcel,
        meta: { requiresAuth: true },
      },
      {
        path: 'trash',
        name: 'QuizTrash',
        component: QuizTrash,
        meta: { requiresAuth: true },
      },
      {
        path: 'join-quiz',
        name: 'JoinQuiz',
        component: JoinQuiz,
        meta: { requiresAuth: true },
      },
      {
        path: 'quiz/:code/join',
        name: 'JoinQuizDirect',
        component: JoinQuiz,
        meta: { requiresAuth: true },
      },
      {
        path: 'contact',
        name: 'Contact',
        component: () => import('@/components/client/Contact.vue'),
      },
    ],
  },

  //Route đăng nhập của admin
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: LoginAdmin,
  },

  // Admin layout và các route quản trị
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: AdminDashboard,
      },
      {
        path: 'attempts',
        name: 'AdminQuizAttempts',
        component: AdminQuizAttempts,
      },
      {
        path: 'all-users',
        name: 'UserManager',
        component: UserManager,
      },
      {
        path: 'all-quizzes',
        name: 'QuizManager',
        component: QuizManager,
      },
      {
        path: 'reports',
        name: 'AdminReport',
        component: AdminReport,
      },
      {
        path: 'categories',
        name: 'CategoryManager',
        component: CategoryManager,
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('accessToken');

  // Nếu route yêu cầu đăng nhập
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token) {
      // Nếu chưa có token, chuyển về trang login
      return next({ name: 'Login' });
    }
  }

  // Nếu đã đăng nhập mà vào trang login thì chuyển hướng
  if (to.name === 'Login' && token) {
    const userId = localStorage.getItem('userId')
    if (userId) {
      return next({ name: 'ClientDashboard', params: { userId } });
    } else {
      console.warn('⚠️ userId chưa có trong localStorage, không chuyển hướng!');
      return next(); // Không redirect nữa
    }
  }

  next(); // Cho phép đi tiếp
});


export default router
