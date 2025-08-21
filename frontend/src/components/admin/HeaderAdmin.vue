<template>
  <header id="header" class="modern-navbar fixed-top">
    <div class="navbar-container">
      <!-- Logo Section -->
      <div class="brand-section">
        <div @click="handleLogoClick" class="brand-link" style="cursor: pointer">
          <div class="logo-wrapper">
            <h1 class="nabla">QuizMaster</h1>
            <span class="brand-subtitle">Admin Panel</span>
          </div>
        </div>
      </div>

      <!-- Main Navigation -->
      <nav class="main-nav">
        <div class="nav-group">
          <!-- Home Page -->
          <div @click="handleHomeClick" class="nav-item" style="cursor: pointer">
            <div class="nav-content">
              <i class="bi bi-house"></i>
              <span>Trang chủ</span>
            </div>
          </div>

          <!-- Dashboard -->
          <RouterLink to="/admin/dashboard" class="nav-item" active-class="router-link-active">
            <div class="nav-content">
              <i class="bi bi-speedometer2"></i>
              <span>Dashboard</span>
            </div>
          </RouterLink>

          <!-- Categories Dropdown -->
          <div class="nav-item dropdown" @mouseenter="handleDropdownHover" @mouseleave="handleDropdownLeave">
            <div class="nav-content" @click="handleDropdownClick">
              <i class="bi bi-tags"></i>
              <span>Danh mục</span>
              <i class="bi bi-chevron-down dropdown-arrow"></i>
            </div>
            <div class="dropdown-panel">
              <RouterLink to="/admin/all-users" class="dropdown-link" @click="closeAllDropdowns">
                <i class="bi bi-people"></i>
                <div class="link-content">
                  <span class="link-title">Quản lý User</span>
                  <small class="link-desc">Quản lý người dùng</small>
                </div>
              </RouterLink>
              <RouterLink to="/admin/all-quizzes" class="dropdown-link" @click="closeAllDropdowns">
                <i class="bi bi-journal-code"></i>
                <div class="link-content">
                  <span class="link-title">Quản lý Quiz</span>
                  <small class="link-desc">Quản lý quiz</small>
                </div>
              </RouterLink>
              <RouterLink to="/admin/categories" class="dropdown-link" @click="closeAllDropdowns">
                <i class="bi bi-folder2"></i>
                <div class="link-content">
                  <span class="link-title">Quản lý Danh mục</span>
                  <small class="link-desc">Tạo & sửa danh mục</small>
                </div>
              </RouterLink>
              <RouterLink to="/admin/categories/trash" class="dropdown-link" @click="closeAllDropdowns">
                <i class="bi bi-trash3"></i>
                <div class="link-content">
                  <span class="link-title">Thùng rác</span>
                  <small class="link-desc">Danh mục đã xóa</small>
                </div>
              </RouterLink>
            </div>
          </div>

          <!-- Reports -->
          <RouterLink to="/admin/reports" class="nav-item" active-class="router-link-active">
            <div class="nav-content">
              <i class="bi bi-flag"></i>
              <span>Báo cáo</span>
            </div>
          </RouterLink>

          <!-- Analytics -->
          <RouterLink to="/admin/analytics" class="nav-item" active-class="router-link-active">
            <div class="nav-content">
              <i class="bi bi-graph-up"></i>
              <span>Thống kê</span>
            </div>
          </RouterLink>
        </div>
      </nav>

      <!-- User Section -->
      <div class="user-section">
        <!-- ✅ ADMIN NOTIFICATION COMPONENT (VISIBLE) -->
        <AdminNotificationComponent ref="notificationComponent" />
        <!-- Dark Mode Toggle -->
        <button @click="themeStore.toggleTheme" class="theme-toggle-btn"
          :title="themeStore.isDarkMode ? 'Chế độ sáng' : 'Chế độ tối'">
          <i :class="themeStore.isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-fill'"></i>
        </button>

        <!-- Admin Profile -->
        <div class="user-menu dropdown" @mouseenter="handleUserDropdownHover" @mouseleave="handleUserDropdownLeave">
          <div class="user-trigger" @click="handleUserDropdownClick">
            <div class="user-avatar">
              <img v-if="avatarUrl" :src="avatarUrl" alt="Admin Avatar" class="avatar-image"
                @error="handleAvatarError" />
              <i v-else class="bi bi-person-circle"></i>
            </div>
            <div class="user-info">
              <div class="user-name-row">
                <span class="user-name">{{
                  adminInfo?.fullName || adminInfo?.username || 'Admin'
                  }}</span>
              </div>
              <small class="user-status">Online</small>
            </div>
            <i class="bi bi-chevron-down user-arrow"></i>
          </div>

          <div class="user-dropdown">
            <div class="user-profile-header">
              <div class="profile-avatar">
                <img v-if="avatarUrl" :src="avatarUrl" alt="Admin Avatar" class="profile-avatar-image"
                  @error="handleAvatarError" />
                <i v-else class="bi bi-person-circle"></i>
              </div>
              <div class="profile-info">
                <strong>{{ adminInfo?.fullName || adminInfo?.username || 'Administrator' }}</strong>
                <small>{{ adminInfo?.role || 'Admin' }}</small>
                <small class="profile-email">{{
                  adminInfo?.email || 'admin@quizmaster.com'
                  }}</small>
              </div>
            </div>

            <div class="dropdown-divider"></div>

            <div class="profile-actions">
              <RouterLink to="/admin/profile" class="user-dropdown-link">
                <i class="bi bi-person"></i>
                <span>Hồ sơ cá nhân</span>
              </RouterLink>
              <RouterLink to="/admin/settings" class="user-dropdown-link">
                <i class="bi bi-gear"></i>
                <span>Cài đặt</span>
              </RouterLink>
              <button @click="showNotifications" class="user-dropdown-link">
                <i class="bi bi-bell"></i>
                <span>Thông báo</span>
                <span v-if="notificationCount > 0" class="notification-badge">{{
                  notificationCount
                  }}</span>
              </button>
              <button @click="logout" class="user-dropdown-link logout-link">
                <i class="bi bi-box-arrow-right"></i>
                <span>Đăng xuất</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/utils/axios'
import { useThemeStore } from '@/stores/theme'
import AdminNotificationComponent from './AdminNotificationComponent.vue'

const router = useRouter()
const themeStore = useThemeStore()
const adminInfo = ref(null)
const reportCount = ref(5)
const notificationComponent = ref(null)
const notificationCount = ref(0)

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

    adminInfo.value = response.data
    console.log('🔍 Admin Profile loaded:', response.data)
    console.log('🔍 Avatar URL:', response.data.avatarUrl)
  } catch (error) {
    console.error('Error fetching admin profile:', error)

    // Nếu lỗi 401, có thể token đã hết hạn
    if (error.response?.status === 401) {
      console.log('❌ Token expired or invalid, clearing localStorage')
      localStorage.clear()
      adminInfo.value = null

      // Redirect về login page
      router.push('/login')
    }
  }
}

// ✅ LOAD NOTIFICATION COUNT
async function loadNotificationCount() {
  try {
    const response = await api.get('/notifications/unread/count')
    notificationCount.value = response.data.count
    console.log('🔔 Admin notification count loaded:', notificationCount.value)
  } catch (error) {
    console.error('Error loading notification count:', error)
    notificationCount.value = 0
  }
}

// Avatar URL computed
const avatarUrl = computed(() => {
  if (adminInfo.value?.avatarUrl) {
    // Nếu avatarUrl bắt đầu bằng /api/ hoặc /uploads/
    if (
      adminInfo.value.avatarUrl.startsWith('/api/') ||
      adminInfo.value.avatarUrl.startsWith('/uploads/')
    ) {
      return `http://localhost:8080${adminInfo.value.avatarUrl}`
    }
    // Nếu là URL đầy đủ
    if (adminInfo.value.avatarUrl.startsWith('http')) {
      return adminInfo.value.avatarUrl
    }
    // Nếu là đường dẫn tương đối
    return `http://localhost:8080${adminInfo.value.avatarUrl}`
  }
  return null
})

function handleLogoClick() {
  router.push({ name: 'Dashboard' })
}

function handleHomeClick() {
  router.push({ name: 'Dashboard' })
}

// ✅ SHOW NOTIFICATIONS
const showNotifications = () => {
  console.log('🔔 Show notifications clicked from Admin Header')
  console.log('🔍 notificationComponent.value:', notificationComponent.value)
  if (notificationComponent.value) {
    console.log('🔍 notificationComponent methods:', Object.keys(notificationComponent.value))
    // ✅ Trigger toggle panel trực tiếp
    notificationComponent.value.toggleNotificationPanel()
    console.log('✅ AdminNotificationComponent found and toggle called')
  } else {
    console.log('❌ AdminNotificationComponent not found')
  }
}

function logout() {
  // ✅ Clear all localStorage completely
  localStorage.clear()

  // ✅ Redirect to login page
  router.push('/login')
  console.log('✅ Admin logout completed - redirected to login')

  // ✅ Force refresh immediately
  window.location.reload()
}

// ✅ Xử lý lỗi avatar
function handleAvatarError(event) {
  console.log('❌ Avatar load error, showing fallback icon')
  event.target.style.display = 'none'
  const fallbackIcon = event.target.nextElementSibling
  if (fallbackIcon) {
    fallbackIcon.style.display = 'block'
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

// ✅ CLICK OUTSIDE TO CLOSE
const handleClickOutside = (event) => {
  if (
    !event.target.closest('.dropdown') &&
    !event.target.closest('.user-menu') &&
    !event.target.closest('.notification-dropdown')
  ) {
    closeAllDropdowns()
  }
}

onMounted(async () => {
  // Load admin info from multiple sources
  await loadAdminInfo()
  await fetchUserProfile()
  await loadNotificationCount()

  // ✅ DEBUG: Check if notificationComponent is available
  console.log('🔍 AdminNotificationComponent ref:', notificationComponent.value)

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
})

async function loadAdminInfo() {
  try {
    // Try to get from admin_user first
    const adminUserStr = localStorage.getItem('admin_user')
    if (adminUserStr) {
      adminInfo.value = JSON.parse(adminUserStr)
      console.log('✅ Admin user loaded from admin_user:', adminInfo.value)
      return
    }

    // Try to get from user data
    const userInfo = localStorage.getItem('user')
    if (userInfo) {
      const userData = JSON.parse(userInfo)
      if (userData.role === 'admin' || userData.role === 'ADMIN') {
        adminInfo.value = userData
        console.log('✅ Admin user loaded from user data:', adminInfo.value)
        return
      }
    }

    // Try to get from localStorage user data
    const username = localStorage.getItem('username')
    const userId = localStorage.getItem('userId')
    if (username && userId) {
      adminInfo.value = {
        username: username,
        id: userId,
        role: 'admin',
        fullName: username,
      }
      console.log('✅ Admin user loaded from localStorage:', adminInfo.value)
      return
    }

    console.log('❌ No admin user found')
    adminInfo.value = null
  } catch (error) {
    console.error('Error loading admin info:', error)
    adminInfo.value = null
  }
}

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)

  // Remove mouse leave listeners
  const dropdowns = document.querySelectorAll('.nav-item.dropdown')
  dropdowns.forEach((dropdown) => {
    dropdown.removeEventListener('mouseleave', () => { })
  })
})
</script>

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

.brand-subtitle {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
  margin-top: -0.5rem;
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

/* ✅ DROPDOWN NOTIFICATION BADGE */
.notification-badge {
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
  margin-left: auto;
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

.profile-email {
  display: block;
  color: #a0aec0;
  font-size: 0.8rem;
}

.dropdown-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.2), transparent);
  margin: 1rem 0;
}

.profile-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
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
