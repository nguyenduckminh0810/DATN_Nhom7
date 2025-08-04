<script setup>
import { useRouter, RouterLink } from 'vue-router'
import { useLogin } from './useLogin'
import { computed, watch, onMounted, onUnmounted, ref } from 'vue'
import axios from 'axios'
import api from '@/utils/axios'


const { logout, username, message, userId, getUserId, token } = useLogin()
const router = useRouter()

const isLoggedIn = computed(() => !!token.value)
const userProfile = ref(null)
const notificationCount = ref(3) // Có thể lấy từ API sau

// ✅ LẤY THÔNG TIN PROFILE VÀ AVATAR
async function fetchUserProfile() {
  try {
    const token = localStorage.getItem('accessToken')
    if (!token) return

    const response = await api.get('/user/profile', {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })

    userProfile.value = response.data
    console.log('🔍 User Profile loaded:', response.data)
    console.log('🔍 Avatar URL:', response.data.avatarUrl)
  } catch (error) {
    console.error('Error fetching user profile:', error)
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

watch(message, (newVal) => {
  if (newVal === 'SUCCESS' && !username.value) {
    username.value = localStorage.getItem('username')
    fetchUserProfile() // ✅ Lấy profile khi login thành công
  }
})

// ✅ Lấy profile khi component mount nếu đã login
watch(isLoggedIn, (newVal) => {
  if (newVal) {
    fetchUserProfile()
  }
})

// ✅ REFRESH PROFILE KHI ROUTE THAY ĐỔI (để load avatar mới)
watch(
  () => router.currentRoute.value.path,
  () => {
    if (isLoggedIn.value) {
      fetchUserProfile()
    }
  },
)

function logoutForNavbar() {
  console.log('🔴 LOGOUT BUTTON CLICKED')

  if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
    logout()
    router.push('/login')
  }
}

async function goToHistory() {
  try {
    // Ensure we have userId
    if (!userId.value) {
      await getUserId()
    }

    if (userId.value) {
      router.push({ name: 'QuizHistory', params: { userId: userId.value } })
    } else {
      console.error('Cannot get user ID for history navigation')
      router.push('/login')
    }
  } catch (error) {
    console.error('Error navigating to history:', error)
    router.push('/login')
  }
}

function showNotifications() {
  alert('🔔 Tính năng thông báo sẽ được phát triển trong tương lai!')
}

// ✅ XỬ LÝ LỖI AVATAR
function handleAvatarError(event) {
  console.log('❌ Avatar load error, showing default icon')
  event.target.style.display = 'none'
  // Icon sẽ hiển thị tự động vì v-else
}

// ✅ FORCE REFRESH AVATAR (có thể gọi từ bên ngoài)
function refreshAvatar() {
  if (isLoggedIn.value) {
    fetchUserProfile()
  }
}

// ✅ EXPOSE FUNCTION CHO COMPONENT KHÁC
defineExpose({
  refreshAvatar,
})

let handleScroll

onMounted(() => {
  const header = document.getElementById('header')
  handleScroll = () => {
    if (window.scrollY > 10) {
      header?.classList.add('scrolled')
    } else {
      header?.classList.remove('scrolled')
    }
  }

  window.addEventListener('scroll', handleScroll)
  handleScroll()

  // ✅ Lấy profile nếu đã login
  console.log('🔎 Navbar mounted - username:', username.value)
  if (isLoggedIn.value) {
    fetchUserProfile()
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
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

          <div class="nav-item dropdown">
            <div class="nav-content">
              <i class="bi bi-puzzle"></i>
              <span>Quiz</span>
              <i class="bi bi-chevron-down dropdown-arrow"></i>
            </div>
            <div class="dropdown-panel">
              <RouterLink to="/quiz-crud" class="dropdown-link">
                <i class="bi bi-plus-circle"></i>
                <div class="link-content">
                  <span class="link-title">Tạo Quiz</span>
                  <small class="link-desc">Tạo quiz mới</small>
                </div>
              </RouterLink>
              <RouterLink to="/join-quiz" class="dropdown-link">
                <i class="bi bi-key"></i>
                <div class="link-content">
                  <span class="link-title">Tham gia Quiz</span>
                  <small class="link-desc">Nhập mã code</small>
                </div>
              </RouterLink>
              <RouterLink to="/my-quizzes" class="dropdown-link">
                <i class="bi bi-collection"></i>
                <div class="link-content">
                  <span class="link-title">Quiz của tôi</span>
                  <small class="link-desc">Quản lý quiz</small>
                </div>
              </RouterLink>
              <RouterLink to="/public-quizzes" class="dropdown-link">
                <i class="bi bi-globe"></i>
                <div class="link-content">
                  <span class="link-title">Quiz công khai</span>
                  <small class="link-desc">Khám phá quiz</small>
                </div>
              </RouterLink>
            </div>
          </div>

          <div class="nav-item dropdown">
            <div class="nav-content">
              <i class="bi bi-folder2"></i>
              <span>Danh mục</span>
              <i class="bi bi-chevron-down dropdown-arrow"></i>
            </div>
            <div class="dropdown-panel">
              <RouterLink :to="{ name: 'CategoryManager' }" class="dropdown-link">
                <i class="bi bi-grid"></i>
                <div class="link-content">
                  <span class="link-title">Xem danh mục</span>
                  <small class="link-desc">Duyệt theo chủ đề</small>
                </div>
              </RouterLink>
              <RouterLink :to="{ name: 'CategoryManager' }" class="dropdown-link">
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

          <RouterLink to="/contact" class="nav-item">
            <div class="nav-content">
              <i class="bi bi-envelope"></i>
              <span>Liên hệ</span>
            </div>
          </RouterLink>
        </div>
      </nav>

      <!-- User Section -->
      <div class="user-section">
        <div v-if="!isLoggedIn" class="auth-actions">
          <RouterLink to="/register" class="btn btn-ghost"> Đăng ký </RouterLink>
          <button @click="login" class="btn btn-primary">
            <i class="bi bi-box-arrow-in-right"></i>
            Đăng nhập
          </button>
        </div>

        <div v-else class="user-menu dropdown">
          <div class="user-trigger">
            <div class="user-avatar">
              <img v-if="avatarUrl" :src="avatarUrl" alt="Avatar" class="avatar-image" @error="handleAvatarError" />
              <i v-else class="bi bi-person-circle"></i>
            </div>
            <div class="user-info">
              <div class="user-name-row">
                <span class="user-name">{{ userProfile?.fullName || username }}</span>
                <!-- ✅ NOTIFICATION BADGE BÊN NGOÀI -->
                <span v-if="notificationCount > 0" class="navbar-notification-badge">{{
                  notificationCount
                  }}</span>
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

            <RouterLink to="/profile" class="user-dropdown-link">
              <i class="bi bi-person"></i>
              <span>Hồ sơ cá nhân</span>
            </RouterLink>

            <RouterLink to="/profile" class="user-dropdown-link">
              <i class="bi bi-gear"></i>
              <span>Cài đặt</span>
            </RouterLink>

            <RouterLink to="/trash" class="user-dropdown-link">
              <i class="bi bi-trash3"></i>
              <span>Thùng rác</span>
            </RouterLink>

            <a href="#" class="user-dropdown-link" @click.prevent="showNotifications">
              <i class="bi bi-bell"></i>
              <span>Thông báo</span>
              <span v-if="notificationCount > 0" class="notification-badge">{{
                notificationCount
                }}</span>
            </a>

            <div class="dropdown-divider"></div>

            <button @click="logoutForNavbar" class="user-dropdown-link logout-link">
              <i class="bi bi-box-arrow-right"></i>
              <span>Đăng xuất</span>
            </button>
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

.nav-item.dropdown:hover .dropdown-panel {
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
}

.user-menu:hover .user-dropdown {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
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
