<script setup>
import { useRouter, RouterLink } from 'vue-router'
import { useLogin } from './useLogin'
import { computed, watch, onMounted, onUnmounted, ref } from 'vue'
import api from '@/utils/axios'
import { useThemeStore } from '@/stores/theme'
import { useNotificationStore } from '@/stores/notification'
import NotificationComponent from '../NotificationComponent.vue'
import { storeToRefs } from 'pinia'

// Define component name for Vue DevTools
defineOptions({
  name: 'MainNavbar',
})

const { logout, username, message, userId, getUserId, token } = useLogin()
const router = useRouter()
const themeStore = useThemeStore()
const notificationStore = useNotificationStore()
const notificationComponent = ref(null)
const { unreadCount } = storeToRefs(notificationStore)

const isLoggedIn = computed(() => !!token.value)
const userProfile = ref(null)
const notificationCount = computed(() => unreadCount.value || 0)

// ✅ CHECK USER ROLE
const isAdmin = computed(() => {
  const isAdminUser = userProfile.value?.role === 'admin' || userProfile.value?.role === 'ADMIN'
  console.log('🔍 isAdmin computed:', isAdminUser, 'userProfile:', userProfile.value)
  return isAdminUser
})

const isUser = computed(() => {
  const isRegularUser =
    userProfile.value?.role === 'user' ||
    userProfile.value?.role === 'USER' ||
    !userProfile.value?.role
  console.log('🔍 isUser computed:', isRegularUser, 'userProfile:', userProfile.value)
  return isRegularUser
})

// ✅ ROLE-BASED MENU ITEMS
const userMenuItems = computed(() => {
  const items = [
    {
      label: 'Hồ sơ cá nhân',
      icon: 'bi bi-person',
      link: '/profile',
      description: 'Xem và chỉnh sửa thông tin cá nhân',
    },
    {
      label: 'Thùng rác',
      icon: 'bi bi-trash3',
      link: '/trash',
      description: 'Quiz đã xóa',
    },
    {
      label: 'Thông báo',
      icon: 'bi bi-bell',
      action: 'notifications',
      description: 'Xem thông báo mới',
      badge: notificationCount.value,
    },
  ]
  return items
})

// ✅ LẤY THÔNG TIN PROFILE VÀ AVATAR
async function fetchUserProfile() {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      console.log('❌ No token found in localStorage')
      return
    }

    console.log('🔍 Token found:', token.substring(0, 20) + '...')

    const response = await api.get('/user/profile')

    userProfile.value = response.data
    console.log('🔍 User Profile loaded:', response.data)
    console.log('🔍 Avatar URL:', response.data.avatarUrl)
  } catch (error) {
    console.error('Error fetching user profile:', error)

    // Nếu lỗi 401 hoặc 403, có thể token đã hết hạn hoặc không hợp lệ
    if (error.response?.status === 401 || error.response?.status === 403) {
      console.log('❌ Token expired or invalid, clearing localStorage')
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      userProfile.value = null

      // Redirect về login page nếu đang ở trang cần authentication
      if (router.currentRoute.value.path !== '/login' && router.currentRoute.value.path !== '/') {
        router.push('/login')
      }
    }
  }
}

// Avatar URL computed
const avatarUrl = computed(() => {
  if (userProfile.value?.avatarUrl) {
    // Nếu avatarUrl bắt đầu bằng /api/ hoặc /uploads/
    if (
      userProfile.value.avatarUrl.startsWith('/api/') ||
      userProfile.value.avatarUrl.startsWith('/uploads/')
    ) {
      return `http://localhost:8080${userProfile.value.avatarUrl}`
    }
    // Nếu là URL đầy đủ
    if (userProfile.value.avatarUrl.startsWith('http')) {
      return userProfile.value.avatarUrl
    }
    // Nếu là đường dẫn tương đối
    return `http://localhost:8080${userProfile.value.avatarUrl}`
  }
  return null
})

function login() {
  router.push('/login')
}

// ✅ FUNCTION XỬ LÝ NAVIGATION THÔNG MINH
function handleLogoClick() {
  if (isLoggedIn.value) {
    // Nếu đã login -> dashboard
    router.push('/dashboard')
  } else {
    // Nếu chưa login -> home
    router.push('/')
  }
}

// ✅ SWITCH TO ADMIN PANEL
const switchToAdminPanel = () => {
  router.push('/admin/dashboard')
}

// ✅ SWITCH TO USER PANEL
const switchToUserPanel = () => {
  router.push('/dashboard')
}

// ✅ SHOW NOTIFICATIONS: đóng dropdown trước khi mở panel để tránh chồng chéo
const showNotifications = (event) => {
  if (event) {
    event.preventDefault()
    event.stopPropagation()
  }
  // Đóng mọi dropdown
  closeAllDropdowns()
  // Mở panel sau khi dropdown đóng
  setTimeout(() => {
    if (notificationComponent.value) {
      notificationComponent.value.toggleNotificationPanel()
    }
  }, 0)
}

// ✅ LOGOUT FOR NAVBAR
const logoutForNavbar = () => {
  // Để composable useLogin xử lý việc bảo lưu username khi rememberMe=1
  const remembered = localStorage.getItem('rememberMe') === '1'
  const rememberedUsername = remembered ? localStorage.getItem('username') : null

  logout()
  // ✅ Reset user profile & UI
  userProfile.value = null
  closeAllDropdowns()
  // ❗ Không set avatarUrl (là computed), chỉ cần reset userProfile

  // Không xóa toàn bộ localStorage để giữ lại username/rememberMe nếu người dùng đã tick
  if (remembered && rememberedUsername) {
    localStorage.setItem('username', rememberedUsername)
    localStorage.setItem('rememberMe', '1')
  }

  // ✅ Redirect to login page after logout
  router.push('/login')
  console.log('✅ Logout completed - redirected to login (preserved rememberMe if enabled)')
}

watch(message, (newVal) => {
  if (newVal === 'SUCCESS' && !username.value) {
    username.value = localStorage.getItem('username')
    fetchUserProfile() // ✅ Lấy profile khi login thành công
  }
})

// ✅ Lấy profile khi component mount nếu đã login
watch(isLoggedIn, (newVal) => {
  // ✅ Chỉ load profile nếu có token
  const token = localStorage.getItem('token')
  if (newVal && token) {
    fetchUserProfile()
  } else {
    // ✅ Reset profile khi không login
    userProfile.value = null
  }
})

// ✅ Watch for token changes
watch(
  () => localStorage.getItem('token'),
  (newToken) => {
    if (!newToken) {
      // ✅ Reset profile khi token bị xóa
      userProfile.value = null
      console.log('✅ Token removed - reset user profile')
    }
  },
)

// ✅ REFRESH PROFILE KHI ROUTE THAY ĐỔI (để load avatar mới)
watch(
  () => router.currentRoute.value.path,
  () => {
    // ✅ Chỉ reload profile nếu user thực sự đã login và có token
    const token = localStorage.getItem('token')
    if (isLoggedIn.value && token) {
      fetchUserProfile()
    }
  },
)

// ✅ Hàm cập nhật badge + danh sách ngay sau khi nộp bài
async function refreshNotificationsAfterSubmit() {
  try {
    // gọi API count trước để badge nhảy nhanh
    await notificationStore.loadUnreadCount()
    // rồi load danh sách (nếu panel đang mở)
    await notificationStore.loadNotifications()
  } catch (e) {
    console.error('Failed to refresh notifications after submit:', e)
  }
}

// ✅ Lấy profile khi component mount nếu đã login
onMounted(() => {
  // ✅ Chỉ load profile nếu có token
  const token = localStorage.getItem('token')
  if (isLoggedIn.value && token) {
    fetchUserProfile()
    // ✅ Khởi tạo notification store
    notificationStore.initialize().then(async () => {
      // Đồng bộ badge ngay sau initialize nếu server đã ghi nhận đã đọc hết
      const countBefore = unreadCount.value
      await notificationStore.loadUnreadCount()
      if (unreadCount.value === 0 && countBefore !== 0) {
        await notificationStore.loadNotifications()
      }
    })
  }

  // Add click outside listener
  document.addEventListener('click', handleClickOutside)

  // Add mouse leave listeners for dropdowns
  const dropdowns = document.querySelectorAll('.nav-item.dropdown')
  dropdowns.forEach((dropdown) => {
    dropdown.addEventListener('mouseleave', () => {
      setTimeout(() => {
        const panel = dropdown.querySelector('.dropdown-panel')
        if (panel && !dropdown.matches(':hover')) {
          panel.style.opacity = '0'
          panel.style.visibility = 'hidden'
          panel.style.transform = 'translateX(-50%) translateY(-10px)'
          dropdown.classList.remove('active')
        }
      }, 100)
    })
  })

  // ✅ Lắng nghe sự kiện nộp bài từ PlayQuiz/QuizResult
  window.addEventListener('quiz-submitted', refreshNotificationsAfterSubmit)
})

onUnmounted(() => {
  // Remove click outside listener
  document.removeEventListener('click', handleClickOutside)

  // Remove mouse leave listeners
  const dropdowns = document.querySelectorAll('.nav-item.dropdown')
  dropdowns.forEach((dropdown) => {
    dropdown.removeEventListener('mouseleave', () => { })
  })

  // Hủy đăng ký sự kiện toàn cục
  window.removeEventListener('quiz-submitted', refreshNotificationsAfterSubmit)
})

// ✅ Xử lý lỗi avatar
function handleAvatarError(event) {
  console.log('❌ Avatar load error, showing fallback icon')
  event.target.style.display = 'none'
  const fallbackIcon = event.target.nextElementSibling
  if (fallbackIcon) {
    fallbackIcon.style.display = 'block'
  }
}

// ✅ GO TO HISTORY
function goToHistory() {
  if (isLoggedIn.value) {
    router.push('/history')
  } else {
    router.push('/login')
  }
}

// ✅ DROPDOWN HANDLING
const handleDropdownClick = (event) => {
  event.preventDefault()
  event.stopPropagation()

  const dropdown = event.currentTarget.closest('.dropdown')
  const panel = dropdown.querySelector('.dropdown-panel')

  // Close all other dropdowns first
  closeAllDropdowns()

  // Toggle current dropdown
  if (panel.style.visibility === 'visible') {
    panel.style.opacity = '0'
    panel.style.visibility = 'hidden'
    panel.style.transform = 'translateX(-50%) translateY(-10px)'
    dropdown.classList.remove('active')
  } else {
    panel.style.opacity = '1'
    panel.style.visibility = 'visible'
    panel.style.transform = 'translateX(-50%) translateY(0)'
    dropdown.classList.add('active')
  }
}

// ✅ CLOSE ALL DROPDOWNS
const closeAllDropdowns = () => {
  const dropdowns = document.querySelectorAll('.dropdown-panel')
  const dropdownItems = document.querySelectorAll('.nav-item.dropdown')
  const userDropdowns = document.querySelectorAll('.user-dropdown')
  const userMenus = document.querySelectorAll('.user-menu')

  dropdowns.forEach((panel) => {
    panel.style.opacity = '0'
    panel.style.visibility = 'hidden'
    panel.style.transform = 'translateX(-50%) translateY(-10px)'
  })

  dropdownItems.forEach((item) => {
    item.classList.remove('active')
  })

  userDropdowns.forEach((dropdown) => {
    dropdown.style.display = 'none'
  })

  userMenus.forEach((menu) => {
    menu.classList.remove('active')
  })

  // ✅ KHÔNG Ẩn notification panel khi close dropdown
  // Notification panel sẽ được đóng bằng nút X hoặc click outside
}

// ✅ CLICK OUTSIDE TO CLOSE
const handleClickOutside = (event) => {
  if (!event.target.closest('.dropdown')) {
    closeAllDropdowns()
  }
}

// ✅ USER DROPDOWN HANDLING
const handleUserDropdownClick = (event) => {
  event.preventDefault()
  event.stopPropagation()

  const userMenu = event.currentTarget.closest('.user-menu')
  const userDropdown = userMenu.querySelector('.user-dropdown')

  // Close all other dropdowns first
  closeAllDropdowns()

  // Toggle user dropdown
  if (userDropdown.style.display === 'block') {
    userDropdown.style.display = 'none'
    userMenu.classList.remove('active')
  } else {
    userDropdown.style.display = 'block'
    userMenu.classList.add('active')
  }
}

// ✅ HOVER DROPDOWN HANDLING
const handleDropdownHover = (event) => {
  const dropdown = event.currentTarget.closest('.dropdown')
  const panel = dropdown.querySelector('.dropdown-panel')

  // Close all other dropdowns first
  closeAllDropdowns()

  // Show current dropdown
  panel.style.opacity = '1'
  panel.style.visibility = 'visible'
  panel.style.transform = 'translateX(-50%) translateY(0)'
  dropdown.classList.add('active')
}

const handleDropdownLeave = (event) => {
  const dropdown = event.currentTarget.closest('.dropdown')
  const panel = dropdown.querySelector('.dropdown-panel')

  // Hide dropdown after delay
  setTimeout(() => {
    if (!dropdown.matches(':hover')) {
      panel.style.opacity = '0'
      panel.style.visibility = 'hidden'
      panel.style.transform = 'translateX(-50%) translateY(-10px)'
      dropdown.classList.remove('active')
    }
  }, 150)
}

// ✅ USER DROPDOWN HOVER HANDLING
const handleUserDropdownHover = (event) => {
  const userMenu = event.currentTarget.closest('.user-menu')
  const userDropdown = userMenu.querySelector('.user-dropdown')

  // Close all other dropdowns first
  closeAllDropdowns()

  // Show user dropdown
  userDropdown.style.display = 'block'
  userDropdown.style.opacity = '1'
  userDropdown.style.visibility = 'visible'
  userDropdown.style.transform = 'translateY(0)'
  userMenu.classList.add('active')
}

const handleUserDropdownLeave = (event) => {
  const userMenu = event.currentTarget.closest('.user-menu')
  const userDropdown = userMenu.querySelector('.user-dropdown')

  // Hide user dropdown after delay
  setTimeout(() => {
    if (!userMenu.matches(':hover')) {
      userDropdown.style.display = 'none'
      userDropdown.style.opacity = '0'
      userDropdown.style.visibility = 'hidden'
      userDropdown.style.transform = 'translateY(-10px)'
      userMenu.classList.remove('active')
    }
  }, 150)
}
</script>

<template>
  <header id="header" class="modern-navbar fixed-top">
    <div class="navbar-container">
      <!-- Logo Section -->
      <div class="brand-section">
        <div @click="handleLogoClick" class="brand-link" style="cursor: pointer">
          <div class="logo-wrapper">
            <h1 class="nabla">QuizMaster</h1>
          </div>
        </div>
      </div>

      <!-- Main Navigation -->
      <nav class="main-nav">
        <div class="nav-group">
          <div @click="handleLogoClick" class="nav-item" style="cursor: pointer">
            <div class="nav-content">
              <i class="bi bi-house"></i>
              <span>Trang chủ</span>
            </div>
          </div>

          <div class="nav-item dropdown" @mouseenter="handleDropdownHover" @mouseleave="handleDropdownLeave">
            <div class="nav-content" @click="handleDropdownClick">
              <i class="bi bi-puzzle"></i>
              <span>Quiz</span>
              <i class="bi bi-chevron-down dropdown-arrow"></i>
            </div>
            <div class="dropdown-panel">
              <RouterLink to="/quiz-crud" class="dropdown-link" @click="closeAllDropdowns">
                <i class="bi bi-plus-circle"></i>
                <div class="link-content">
                  <span class="link-title">Tạo Quiz</span>
                  <small class="link-desc">Tạo quiz mới</small>
                </div>
              </RouterLink>
              <RouterLink to="/join-quiz" class="dropdown-link" @click="closeAllDropdowns">
                <i class="bi bi-key"></i>
                <div class="link-content">
                  <span class="link-title">Tham gia Quiz</span>
                  <small class="link-desc">Nhập mã code</small>
                </div>
              </RouterLink>
              <RouterLink to="/my-quizzes" class="dropdown-link" @click="closeAllDropdowns">
                <i class="bi bi-collection"></i>
                <div class="link-content">
                  <span class="link-title">Quiz của tôi</span>
                  <small class="link-desc">Quản lý quiz</small>
                </div>
              </RouterLink>
              <RouterLink to="/public-quizzes" class="dropdown-link" @click="closeAllDropdowns">
                <i class="bi bi-globe"></i>
                <div class="link-content">
                  <span class="link-title">Quiz công khai</span>
                  <small class="link-desc">Khám phá quiz</small>
                </div>
              </RouterLink>
            </div>
          </div>

          <div class="nav-item dropdown" @mouseenter="handleDropdownHover" @mouseleave="handleDropdownLeave">
            <div class="nav-content" @click="handleDropdownClick">
              <i class="bi bi-folder2"></i>
              <span>Danh mục</span>
              <i class="bi bi-chevron-down dropdown-arrow"></i>
            </div>
            <div class="dropdown-panel">
              <RouterLink :to="{ name: 'CategoryView' }" class="dropdown-link" @click="closeAllDropdowns">
                <i class="bi bi-grid"></i>
                <div class="link-content">
                  <span class="link-title">Xem danh mục</span>
                  <small class="link-desc">Duyệt theo chủ đề</small>
                </div>
              </RouterLink>
              <RouterLink v-if="isAdmin" :to="{ name: 'AdminCategories' }" class="dropdown-link"
                @click="closeAllDropdowns">
                <i class="bi bi-gear"></i>
                <div class="link-content">
                  <span class="link-title">Quản lý</span>
                  <small class="link-desc">Tạo & sửa danh mục</small>
                </div>
              </RouterLink>
            </div>
          </div>

          <a @click="goToHistory" class="nav-item" style="cursor: pointer">
            <div class="nav-content">
              <i class="bi bi-graph-up"></i>
              <span>Thống kê</span>
            </div>
          </a>

          <RouterLink to="/global-leaderboard" class="nav-item">
            <div class="nav-content">
              <i class="bi bi-trophy"></i>
              <span>Bảng xếp hạng</span>
            </div>
          </RouterLink>
        </div>
      </nav>

      <!-- User Section -->
      <div class="user-section">
        <!-- Dark Mode Toggle -->
        <button @click="themeStore.toggleTheme" class="theme-toggle-btn"
          :title="themeStore.isDarkMode ? 'Chế độ sáng' : 'Chế độ tối'">
          <i :class="themeStore.isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-fill'"></i>
        </button>

        <div v-if="!isLoggedIn" class="auth-actions">
          <RouterLink to="/register" class="btn btn-ghost"> Đăng ký </RouterLink>
          <button @click="login" class="btn btn-primary">
            <i class="bi bi-box-arrow-in-right"></i>
            Đăng nhập
          </button>
        </div>

        <!-- ✅ Notification moved into profile dropdown -->

        <div v-if="isLoggedIn" class="user-menu dropdown" @mouseenter="handleUserDropdownHover"
          @mouseleave="handleUserDropdownLeave">
          <div class="user-trigger" @click="handleUserDropdownClick">
            <div class="user-avatar">
              <img v-if="avatarUrl" :src="avatarUrl" alt="Avatar" class="avatar-image" @error="handleAvatarError" />
              <i v-else class="bi bi-person-circle"></i>
            </div>
            <div class="user-info">
              <div class="user-name-row">
                <span class="user-name">{{ userProfile?.fullName || username }}</span>
              </div>
              <small class="user-status">Online</small>
            </div>
            <i class="bi bi-chevron-down user-arrow"></i>
          </div>

          <div class="user-dropdown">
            <div class="user-profile-header">
              <div class="profile-avatar">
                <img v-if="avatarUrl" :src="avatarUrl" alt="Avatar" class="profile-avatar-image"
                  @error="handleAvatarError" />
                <i v-else class="bi bi-person-circle"></i>
              </div>
              <div class="profile-info">
                <strong>{{ userProfile?.fullName || username }}</strong>
                <small>{{ userProfile?.role || 'Thành viên' }}</small>
              </div>
            </div>

            <div class="dropdown-divider"></div>

            <!-- Admin quick access (compact) -->
            <RouterLink v-if="isAdmin" to="/admin/dashboard" class="user-dropdown-link admin-link compact-link"
              @click="closeAllDropdowns">
              <i class="bi bi-speedometer2"></i>
              <span>Admin Panel</span>
            </RouterLink>

            <!-- Menu Items - Show user menu items for all users -->
            <div v-if="userMenuItems && userMenuItems.length > 0">
              <!-- User Menu Items -->
              <template v-for="(item, index) in userMenuItems" :key="`user-item-${index}`">
                <RouterLink v-if="item.link" :to="item.link" class="user-dropdown-link" @click="closeAllDropdowns">
                  <i :class="item.icon"></i>
                  <span>{{ item.label }}</span>
                  <span v-if="unreadCount > 0 && item.label === 'Thông báo'" class="notification-badge">{{ unreadCount
                  }}</span>
                </RouterLink>

                <a v-else-if="item.action" href="#" class="user-dropdown-link" @click="showNotifications($event)">
                  <i :class="item.icon"></i>
                  <span>{{ item.label }}</span>
                  <span v-if="unreadCount > 0 && item.label === 'Thông báo'" class="notification-badge">{{ unreadCount
                  }}</span>
                </a>
              </template>
            </div>

            <!-- Inline notifications panel inside dropdown -->
            <div class="dropdown-notifications">
              <NotificationComponent ref="notificationComponent" />
            </div>

            <!-- Logout Button - Always show -->
            <div class="dropdown-divider"></div>
            <a href="#" class="user-dropdown-link logout-link" @click.prevent="logoutForNavbar">
              <i class="bi bi-box-arrow-right"></i>
              <span>Đăng xuất</span>
            </a>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
/* Logo styles giữ nguyên */
.nabla {
  font-family: 'Nabla', system-ui;
  font-optical-sizing: auto;
  font-weight: 400;
  font-style: normal;
  font-variation-settings:
    'EDPT' 100,
    'EHLT' 12;
  margin: 0;
  background: linear-gradient(45deg, #ffd700, #ffed4e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 1.8rem;
}

.nabla:hover {
  animation: glitch 0.5s linear;
  text-shadow:
    2px 0 red,
    -2px 0 rgb(1, 225, 255),
    0 0 12px rgba(255, 110, 108, 0.6);
}

@keyframes glitch {
  0% {
    transform: translate(0);
  }

  15% {
    transform: translate(-2px, 1px);
  }

  30% {
    transform: translate(2px, -1px);
  }

  45% {
    transform: translate(-1px, 2px);
  }

  60% {
    transform: translate(1px, -2px);
  }

  75% {
    transform: translate(0.5px, 1px);
  }

  100% {
    transform: translate(0);
  }
}

/* MODERN NAVBAR */
.modern-navbar {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1000;
}

.modern-navbar.scrolled {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.95), rgba(118, 75, 162, 0.95));
  backdrop-filter: blur(25px);
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.2);
}

.navbar-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 80px;
}

/* BRAND SECTION */
.brand-section {
  flex-shrink: 0;
}

.brand-link {
  text-decoration: none;
  transition: transform 0.3s ease;
}

.brand-link:hover {
  transform: translateY(-2px);
}

/* MAIN NAVIGATION */
.main-nav {
  flex: 1;
  display: flex;
  justify-content: center;
  margin: 0 2rem;
}

.nav-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50px;
  padding: 0.5rem;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.nav-item {
  position: relative;
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  padding: 0.75rem 1.5rem;
  border-radius: 30px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.nav-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 500;
  font-size: 0.95rem;
}

.nav-item:hover,
.nav-item.router-link-active {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  transform: translateY(-1px);
}

.dropdown-arrow {
  font-size: 0.8rem;
  transition: transform 0.3s ease;
}

.nav-item.dropdown:hover .dropdown-arrow {
  transform: rotate(180deg);
}

.nav-item.dropdown.active .dropdown-arrow {
  transform: rotate(180deg);
}

/* DROPDOWN PANELS */
.dropdown-panel {
  position: absolute;
  top: calc(100% + 1rem);
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 1rem;
  min-width: 280px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  opacity: 0;
  visibility: hidden;
  transform: translateX(-50%) translateY(-10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1001;
}

.nav-item.dropdown:hover .dropdown-panel,
.nav-item.dropdown.active .dropdown-panel {
  opacity: 1;
  visibility: visible;
  transform: translateX(-50%) translateY(0);
}

.dropdown-link {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border-radius: 12px;
  color: #2d3748;
  text-decoration: none;
  transition: all 0.3s ease;
  margin-bottom: 0.5rem;
}

.dropdown-link:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  color: #667eea;
  transform: translateX(4px);
}

.dropdown-link i {
  font-size: 1.2rem;
  width: 20px;
  text-align: center;
  color: #667eea;
}

.link-content {
  flex: 1;
}

.link-title {
  display: block;
  font-weight: 600;
  font-size: 0.95rem;
  margin-bottom: 0.2rem;
}

.link-desc {
  display: block;
  font-size: 0.8rem;
  color: #718096;
}

/* USER SECTION */
.user-section {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 1rem;
}

/* THEME TOGGLE BUTTON */
.theme-toggle-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.9);
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.theme-toggle-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.theme-toggle-btn i {
  font-size: 1.1rem;
  transition: transform 0.3s ease;
}

.theme-toggle-btn:hover i {
  transform: scale(1.1);
}

.auth-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border-radius: 25px;
  font-weight: 600;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
}

.btn-ghost {
  background: transparent;
  color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.btn-ghost:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-color: rgba(255, 255, 255, 0.4);
}

.btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.btn-primary:hover {
  background: linear-gradient(135deg, #5a6fd8, #6a4190);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

/* USER MENU */
.user-menu {
  position: relative;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 1rem;
  border-radius: 50px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-trigger:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-1px);
}

.user-avatar i {
  font-size: 2rem;
  color: rgba(255, 255, 255, 0.9);
}

.avatar-image {
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.avatar-image:hover {
  transform: scale(1.1);
  border-color: rgba(255, 255, 255, 0.6);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

/* ✅ USER NAME ROW TRONG NAVBAR */
.user-name-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.user-name {
  font-weight: 600;
  color: white;
  font-size: 0.9rem;
}

.user-status {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.6);
}

/* ✅ NAVBAR NOTIFICATION BADGE */
.navbar-notification-badge {
  background: linear-gradient(135deg, #ff4757, #ff3742);
  color: white;
  font-size: 0.6rem;
  font-weight: 700;
  padding: 0.15rem 0.4rem;
  border-radius: 8px;
  min-width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(255, 71, 87, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.9);
  animation: pulse 2s infinite;
  flex-shrink: 0;
}

.user-arrow {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.7);
  transition: transform 0.3s ease;
}

.user-menu:hover .user-arrow {
  transform: rotate(180deg);
}

.user-menu.active .user-arrow {
  transform: rotate(180deg);
}

/* USER DROPDOWN */
.user-dropdown {
  position: absolute;
  top: calc(100% + 1rem);
  right: 0;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 1.5rem;
  min-width: 280px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1001;
  display: none;
}

.user-menu:hover .user-dropdown,
.user-menu.active .user-dropdown {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
  display: block;
}

/* HOVER BEHAVIOR FOR NAV DROPDOWNS */
.nav-item.dropdown:hover .dropdown-panel {
  opacity: 1;
  visibility: visible;
  transform: translateX(-50%) translateY(0);
}

.user-profile-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.profile-avatar i {
  font-size: 3rem;
  color: #667eea;
}

.profile-avatar-image {
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #667eea;
}

.profile-info strong {
  display: block;
  color: #2d3748;
  font-size: 1.1rem;
}

.profile-info small {
  color: #718096;
  font-size: 0.85rem;
}

.dropdown-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.2), transparent);
  margin: 1rem 0;
}

.user-dropdown-link {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 1rem;
  border-radius: 10px;
  color: #2d3748;
  text-decoration: none;
  transition: all 0.3s ease;
  margin-bottom: 0.5rem;
  background: none;
  border: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
  font-size: 0.9rem;
}

.user-dropdown-link:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  transform: translateX(4px);
}

.user-dropdown-link i {
  width: 20px;
  text-align: center;
  color: #667eea;
}

/* Notification component inside dropdown */
.dropdown-notifications :deep(.notification-bell) {
  display: none;
}

.dropdown-notifications :deep(.notification-panel) {
  position: fixed;
  top: calc(80px + 8px);
  right: 24px;
  width: 420px;
  max-height: 520px;
  z-index: 1100;
}

.dropdown-header {
  background: #f8f9fa;
  font-weight: 600;
  color: #495057;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid #e9ecef;
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.admin-link {
  color: #dc3545 !important;
}

.admin-link:hover {
  background-color: #dc3545 !important;
  color: white !important;
}

.admin-link i {
  color: #dc3545 !important;
}

.compact-link {
  padding: 0.5rem 0.75rem;
  margin-bottom: 0.25rem;
}

.logout-link {
  color: #e53e3e !important;
}

.logout-link:hover {
  background: rgba(229, 62, 62, 0.1) !important;
  color: #c53030 !important;
}

.logout-link i {
  color: #e53e3e !important;
}

.notification-badge {
  background: linear-gradient(135deg, #ff4757, #ff3742);
  color: white;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 0.25rem 0.6rem;
  border-radius: 12px;
  margin-left: auto;
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.4);
  animation: pulse 2s infinite;
  min-width: 18px;
  text-align: center;
}

.nav-group .nav-item {
  flex: 0 0 auto;
  /* tắt shrink */
}

/* Giữ chữ nằm trên một dòng */
.nav-group .nav-item .nav-content {
  white-space: nowrap;
  /* no-wrap */
}

@keyframes pulse {
  0% {
    box-shadow: 0 2px 8px rgba(255, 71, 87, 0.4);
  }

  50% {
    box-shadow: 0 4px 16px rgba(255, 71, 87, 0.6);
    transform: scale(1.05);
  }

  100% {
    box-shadow: 0 2px 8px rgba(255, 71, 87, 0.4);
  }
}

/* RESPONSIVE */
@media (max-width: 1200px) {
  .navbar-container {
    padding: 0 1rem;
  }

  .nav-group {
    gap: 0.25rem;
  }

  .nav-item {
    padding: 0.6rem 1rem;
  }
}

@media (max-width: 991px) {
  .main-nav {
    display: none;
  }

  .navbar-container {
    height: 70px;
  }
}
</style>
