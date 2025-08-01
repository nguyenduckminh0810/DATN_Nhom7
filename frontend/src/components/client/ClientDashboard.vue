<script setup>
import { useRouter } from 'vue-router'
import { useLogin } from './useLogin'
import { useQuizCRUD } from './useQuizCRUD'
import ListQuizPublic from './ListQuizPublic.vue'
import ListUserQuiz from './ListUserQuiz.vue'
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

const router = useRouter()
const { username, userId, logout, getUserId } = useLogin()
const { toQuizCRUD } = useQuizCRUD()

// ✅ LẤY THÔNG TIN USER VÀ AVATAR
const userProfile = ref(null)

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

// ✅ LẤY THÔNG TIN PROFILE VÀ AVATAR
async function fetchUserProfile() {
  try {
    const token = localStorage.getItem('token')
    if (!token) return

    const response = await axios.get('http://localhost:8080/api/user/profile', {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })

    userProfile.value = response.data
    console.log('🔍 Dashboard - User Profile loaded:', response.data)
    console.log('🔍 Dashboard - Avatar URL:', response.data.avatarUrl)
  } catch (error) {
    console.error('Error fetching user profile in dashboard:', error)
  }
}

function toQuizHistory() {
  if (!userId.value) {
    return getUserId().then(() => {
      if (userId.value) {
        router.push({ name: 'QuizHistory', params: { userId: userId.value } })
      } else {
        alert('Không thể lấy thông tin người dùng.')
      }
    })
  }
  router.push({ name: 'QuizHistory', params: { userId: userId.value } })
}

function logoutForClientDashboard() {
  logout()
  router.push('/login')
}

// ✅ LOAD PROFILE KHI COMPONENT MOUNT
onMounted(() => {
  fetchUserProfile()
})

// ✅ XỬ LÝ LỖI AVATAR
function handleAvatarError(event) {
  console.log('❌ Dashboard Avatar load error, showing default icon')
  event.target.style.display = 'none'
  // Icon sẽ hiển thị tự động vì v-else
}
</script>

<template>
  <div class="gradient-bg-with-floating">
    <div class="dashboard-container">
      <!-- Hero Dashboard Section -->
      <div class="dashboard-hero">
        <div class="hero-background">
          <div class="floating-element element-1"></div>
          <div class="floating-element element-2"></div>
          <div class="floating-element element-3"></div>
        </div>

        <div class="hero-content">
          <!-- Welcome Section -->
          <div class="welcome-section">
            <div class="welcome-icon">
              <img
                v-if="avatarUrl"
                :src="avatarUrl"
                alt="User Avatar"
                class="welcome-avatar"
                @error="handleAvatarError"
              />
              <i v-else class="bi bi-person-hearts"></i>
            </div>
            <div class="welcome-text">
              <h1 class="welcome-title">
                <span class="greeting">Chào mừng,</span>
                <span class="username">{{ userProfile?.fullName || username }}!</span>
              </h1>
              <p class="welcome-subtitle">
                Chọn một hành động để bắt đầu quản lý và theo dõi quiz của bạn.
              </p>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="action-buttons">
            <button class="action-btn primary" @click="toQuizCRUD">
              <div class="btn-icon">
                <i class="bi bi-puzzle"></i>
              </div>
              <div class="btn-content">
                <span class="btn-title">Quản lý Quiz</span>
                <span class="btn-desc">Tạo và chỉnh sửa quiz</span>
              </div>
              <div class="btn-arrow">
                <i class="bi bi-arrow-right"></i>
              </div>
            </button>

            <button class="action-btn secondary" @click="toQuizHistory">
              <div class="btn-icon">
                <i class="bi bi-clock-history"></i>
              </div>
              <div class="btn-content">
                <span class="btn-title">Lịch sử làm Quiz</span>
                <span class="btn-desc">Xem kết quả các bài đã làm</span>
              </div>
              <div class="btn-arrow">
                <i class="bi bi-arrow-right"></i>
              </div>
            </button>

            <button class="action-btn danger" @click="logoutForClientDashboard">
              <div class="btn-icon">
                <i class="bi bi-box-arrow-right"></i>
              </div>
              <div class="btn-content">
                <span class="btn-title">Đăng xuất</span>
                <span class="btn-desc">Thoát khỏi tài khoản</span>
              </div>
              <div class="btn-arrow">
                <i class="bi bi-arrow-right"></i>
              </div>
            </button>
          </div>
        </div>
        <div class="content-wrapper" style="position: relative; z-index: 3">
          <ListUserQuiz />
          <ListQuizPublic />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.gradient-bg-with-floating {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow-x: hidden;
  min-height: 100vh;
}

.gradient-bg-with-floating::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(120, 219, 255, 0.2) 0%, transparent 50%);
  pointer-events: none;
  z-index: 1;
}

.floating-icon {
  position: absolute;
  font-size: 2rem;
  opacity: 0.1;
  animation: float 6s ease-in-out infinite;
  z-index: 2;
}

.floating-icon:nth-child(1) {
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.floating-icon:nth-child(2) {
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.floating-icon:nth-child(3) {
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0px);
  }

  50% {
    transform: translateY(-20px);
  }
}

/* === DASHBOARD CONTAINER === */
.dashboard-container {
  min-height: 100vh;
  background: transparent;
}

/* === HERO DASHBOARD SECTION === */
.dashboard-hero {
  position: relative;
  padding: 60px 20px 80px;
  overflow: hidden;
}

/* .hero-background {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(20px);
    border-radius: 0 0 50px 50px;
    border: 3px solid rgba(255, 255, 255, 0.8);
    border-top: none;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
} */

/* Floating Elements Animation */
.floating-element {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(45deg, rgba(255, 255, 255, 0.2), rgba(255, 255, 255, 0.1));
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.3);
  animation-duration: 6s;
  animation-iteration-count: infinite;
  animation-timing-function: ease-in-out;
}

.element-1 {
  width: 100px;
  height: 100px;
  top: 10%;
  left: 10%;
  animation-name: float1;
}

.element-2 {
  width: 60px;
  height: 60px;
  top: 20%;
  right: 15%;
  animation-name: float2;
  animation-delay: -2s;
}

.element-3 {
  width: 80px;
  height: 80px;
  bottom: 20%;
  left: 15%;
  animation-name: float3;
  animation-delay: -4s;
}

@keyframes float1 {
  0%,
  100% {
    transform: translate(0, 0) rotate(0deg);
  }

  33% {
    transform: translate(30px, -30px) rotate(120deg);
  }

  66% {
    transform: translate(-20px, 20px) rotate(240deg);
  }
}

@keyframes float2 {
  0%,
  100% {
    transform: translate(0, 0) rotate(0deg);
  }

  50% {
    transform: translate(-25px, 25px) rotate(180deg);
  }
}

@keyframes float3 {
  0%,
  100% {
    transform: translate(0, 0) rotate(0deg);
  }

  25% {
    transform: translate(20px, -40px) rotate(90deg);
  }

  75% {
    transform: translate(-30px, -10px) rotate(270deg);
  }
}

/* === HERO CONTENT === */
.hero-content {
  max-width: 1000px;
  margin: 0 auto;
  position: relative;
  z-index: 2;
}

/* === WELCOME SECTION === */
.welcome-section {
  text-align: center;
  margin-bottom: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 25px;
}

.welcome-icon {
  width: 100px;
  height: 100px;
  background: linear-gradient(45deg, #ff6b9d, #ff3d71);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 3rem;
  box-shadow: 0 15px 40px rgba(255, 107, 157, 0.4);
  border: 4px solid rgba(255, 255, 255, 0.9);
  animation: welcomeIconPulse 3s ease-in-out infinite;
  overflow: hidden;
}

.welcome-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.welcome-avatar:hover {
  transform: scale(1.05);
  border-color: rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
}

@keyframes welcomeIconPulse {
  0%,
  100% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.1);
  }
}

.welcome-text {
  max-width: 700px;
}

.welcome-title {
  font-size: 3.5rem;
  font-weight: 800;
  margin-bottom: 20px;
  line-height: 1.2;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.greeting {
  color: white;
  text-shadow: 3px 3px 6px rgba(0, 0, 0, 0.3);
  opacity: 0;
  animation: slideInLeft 1s ease-out 0.5s forwards;
}

.username {
  background: linear-gradient(45deg, #ffd700, #ffed4e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 3px 6px rgba(0, 0, 0, 0.3));
  opacity: 0;
  animation: slideInRight 1s ease-out 1s forwards;
}

.welcome-subtitle {
  font-size: 1.3rem;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
  opacity: 0;
  animation: fadeInUp 1s ease-out 1.5s forwards;
}

/* === ACTION BUTTONS === */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.action-btn {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  border: 3px solid rgba(255, 255, 255, 0.8);
  border-radius: 25px;
  padding: 25px 30px;
  display: flex;
  align-items: center;
  gap: 25px;
  text-align: left;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
  opacity: 0;
  transform: translateY(30px);
}

.action-btn.primary {
  animation: slideInUp 0.8s ease-out 2s forwards;
}

.action-btn.secondary {
  animation: slideInUp 0.8s ease-out 2.3s forwards;
}

.action-btn.danger {
  animation: slideInUp 0.8s ease-out 2.6s forwards;
}

.action-btn:hover {
  transform: translateY(-5px);
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.25);
  background: rgba(255, 255, 255, 0.25);
}

.action-btn.primary:hover {
  border-color: #00d4ff;
  background: rgba(0, 212, 255, 0.1);
}

.action-btn.secondary:hover {
  border-color: #ff6b9d;
  background: rgba(255, 107, 157, 0.1);
}

.action-btn.danger:hover {
  border-color: #ff4757;
  background: rgba(255, 71, 87, 0.1);
}

/* Button Components */
.btn-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.8);
}

.action-btn.primary .btn-icon {
  background: linear-gradient(45deg, #00d4ff, #00b8d4);
}

.action-btn.secondary .btn-icon {
  background: linear-gradient(45deg, #ff6b9d, #ff3d71);
}

.action-btn.danger .btn-icon {
  background: linear-gradient(45deg, #ff4757, #ff3742);
}

.btn-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.btn-title {
  font-size: 1.4rem;
  font-weight: 700;
  color: white;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.btn-desc {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.8);
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
}

.btn-arrow {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  color: white;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.action-btn:hover .btn-arrow {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.8);
  transform: translateX(5px);
}

/* === ANIMATIONS === */
@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-50px);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(50px);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* === RESPONSIVE DESIGN === */
@media (max-width: 768px) {
  .dashboard-hero {
    padding: 40px 15px 60px;
  }

  .hero-background {
    border-radius: 0 0 30px 30px;
  }

  .welcome-title {
    font-size: 2.5rem;
    text-align: center;
  }

  .welcome-subtitle {
    font-size: 1.1rem;
  }

  .action-btn {
    padding: 20px;
    gap: 20px;
    flex-direction: column;
    text-align: center;
  }

  .btn-content {
    align-items: center;
  }

  .btn-arrow {
    display: none;
  }

  .floating-element {
    display: none;
  }

  .quiz-lists-container {
    padding: 0 15px;
  }
}

@media (max-width: 480px) {
  .welcome-title {
    font-size: 2rem;
  }

  .action-btn {
    padding: 15px;
    gap: 15px;
  }

  .btn-icon {
    width: 50px;
    height: 50px;
    font-size: 1.5rem;
  }

  .btn-title {
    font-size: 1.2rem;
  }

  .btn-desc {
    font-size: 0.9rem;
  }
}
</style>
