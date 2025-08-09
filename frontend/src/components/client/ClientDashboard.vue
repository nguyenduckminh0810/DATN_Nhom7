<script setup>
import { useRouter } from 'vue-router'
import { useLogin } from './useLogin'
import { useQuizCRUD } from './useQuizCRUD'
import ListQuizPublic from './ListQuizPublic.vue'
import ListUserQuiz from './ListUserQuiz.vue'
import Leaderboard from './Leaderboard.vue'
import { ref, onMounted, computed } from 'vue'
import api from '@/utils/axios'

const router = useRouter()
const { username, userId, getUserId } = useLogin()
const { toQuizCRUD } = useQuizCRUD()

// Join by code
const quizCodeInput = ref('')
const joinQuizByCode = () => {
  const code = quizCodeInput.value.trim()
  if (!code) {
    alert('Vui lòng nhập mã quiz!')
    return
  }
  router.push({ name: 'JoinQuiz', query: { code } })
}

// Profile & avatar
const userProfile = ref(null)
const avatarUrl = computed(() => {
  const url = userProfile.value?.avatarUrl
  if (!url) return null
  if (url.startsWith('http')) return url
  // chuẩn hoá cho /api/* | /uploads/* | relative
  const base = 'http://localhost:8080'
  return url.startsWith('/api/') || url.startsWith('/uploads/') ? `${base}${url}` : `${base}${url}`
})

async function fetchUserProfile() {
  try {
    const token = localStorage.getItem('token')
    if (!token) return
    const { data } = await api.get('/user/profile', {
      headers: { Authorization: `Bearer ${token}` },
    })
    userProfile.value = data
  } catch (err) {
    console.error('Error fetching user profile in dashboard:', err)
  }
}

function toQuizHistory() {
  if (userId.value) {
    router.push({ name: 'QuizHistory', params: { userId: userId.value } })
    return
  }
  getUserId().then(() => {
    if (userId.value) router.push({ name: 'QuizHistory', params: { userId: userId.value } })
    else alert('Không thể lấy thông tin người dùng.')
  })
}

onMounted(fetchUserProfile)

function handleAvatarError(e) {
  e.target.style.display = 'none'
}
</script>

<template>
  <div class="gradient-bg-with-floating">
    <div class="dashboard-container">
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
              <img v-if="avatarUrl" :src="avatarUrl" alt="User Avatar" class="welcome-avatar"
                @error="handleAvatarError" />
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
              <div class="btn-icon"><i class="bi bi-puzzle"></i></div>
              <div class="btn-content">
                <span class="btn-title">Quản lý Quiz</span>
                <span class="btn-desc">Tạo và chỉnh sửa quiz</span>
              </div>
              <div class="btn-arrow"><i class="bi bi-arrow-right"></i></div>
            </button>

            <button class="action-btn secondary" @click="toQuizHistory">
              <div class="btn-icon"><i class="bi bi-clock-history"></i></div>
              <div class="btn-content">
                <span class="btn-title">Lịch sử làm Quiz</span>
                <span class="btn-desc">Xem kết quả các bài đã làm</span>
              </div>
              <div class="btn-arrow"><i class="bi bi-arrow-right"></i></div>
            </button>

            <!-- Join Quiz By Code (card đồng bộ kích thước) -->
            <div class="action-btn join">
              <div class="btn-icon"><i class="bi bi-key"></i></div>
              <div class="btn-content">
                <span class="btn-title">Tham gia bằng mã</span>
                <div class="join-row">
                  <input v-model="quizCodeInput" class="join-input" type="text"
                    placeholder="Nhập mã quiz (ví dụ: ABC123)" @keyup.enter="joinQuizByCode" />
                  <button class="join-btn" @click.stop="joinQuizByCode">
                    <i class="bi bi-arrow-right-circle"></i> Tham gia
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="content-wrapper" style="position: relative; z-index: 3">
          <ListUserQuiz />
          <ListQuizPublic />
          <div class="leaderboard-section">
            <Leaderboard />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* === JOIN (đồng bộ kích thước với các action-btn) === */
.action-buttons .action-btn {
  min-height: 120px;
}

/* Join card khớp style & animation (nút thứ 3) */
.action-btn.join {
  animation: slideInUp 0.8s ease-out 2.6s forwards;
}

.action-btn.join:hover {
  border-color: #00d4ff;
  background: rgba(0, 212, 255, 0.1);
}

/* Hàng nhập mã + nút trong card */
.join-row {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-top: 6px;
}

.join-input {
  flex: 1;
  height: 42px;
  border: 2px solid rgba(255, 255, 255, 0.85);
  border-radius: 12px;
  padding: 0 12px;
  font-size: 1rem;
  background: #fff;
  outline: none;
}

.join-input:focus {
  border-color: #00d4ff;
}

.join-btn {
  height: 42px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0 14px;
  border-radius: 12px;
  font-weight: 800;
  cursor: pointer;
  border: 2px solid rgba(255, 255, 255, 0.85);
  color: #fff;
  background: linear-gradient(45deg, #00d4ff, #00b8d4);
  box-shadow: 0 10px 24px rgba(0, 212, 255, 0.3);
  transition: transform .2s ease, box-shadow .2s ease, background .2s ease;
  white-space: nowrap;
}

.join-btn:hover {
  transform: translateY(-2px);
  background: linear-gradient(45deg, #00b8d4, #0288d1);
}

/* ====== Phần sẵn có giữ nguyên từ đây ====== */
.gradient-bg-with-floating {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow-x: hidden;
  min-height: 100vh;
}

.gradient-bg-with-floating::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(120, 219, 255, 0.2) 0%, transparent 50%);
  pointer-events: none;
  z-index: 1;
}

.dashboard-container {
  min-height: 100vh;
  background: transparent;
}

.dashboard-hero {
  position: relative;
  padding: 60px 20px 80px;
  overflow: hidden;
}

.floating-element {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(45deg, rgba(255, 255, 255, .2), rgba(255, 255, 255, .1));
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, .3);
  animation: 6s ease-in-out infinite;
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
    transform: translate(0, 0) rotate(0)
  }

  33% {
    transform: translate(30px, -30px) rotate(120deg)
  }

  66% {
    transform: translate(-20px, 20px) rotate(240deg)
  }
}

@keyframes float2 {

  0%,
  100% {
    transform: translate(0, 0) rotate(0)
  }

  50% {
    transform: translate(-25px, 25px) rotate(180deg)
  }
}

@keyframes float3 {

  0%,
  100% {
    transform: translate(0, 0) rotate(0)
  }

  25% {
    transform: translate(20px, -40px) rotate(90deg)
  }

  75% {
    transform: translate(-30px, -10px) rotate(270deg)
  }
}

.hero-content {
  max-width: 1000px;
  margin: 0 auto;
  position: relative;
  z-index: 2;
}

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
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 3rem;
  background: linear-gradient(45deg, #ff6b9d, #ff3d71);
  box-shadow: 0 15px 40px rgba(255, 107, 157, 0.4);
  border: 4px solid rgba(255, 255, 255, 0.9);
  overflow: hidden;
  animation: welcomeIconPulse 3s ease-in-out infinite;
}

.welcome-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid rgba(255, 255, 255, .3);
  transition: .3s;
}

.welcome-avatar:hover {
  transform: scale(1.05);
  border-color: rgba(255, 255, 255, .6);
  box-shadow: 0 8px 25px rgba(0, 0, 0, .3);
}

@keyframes welcomeIconPulse {

  0%,
  100% {
    transform: scale(1)
  }

  50% {
    transform: scale(1.1)
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
  color: #fff;
  text-shadow: 3px 3px 6px rgba(0, 0, 0, .3);
  opacity: 0;
  animation: slideInLeft 1s ease-out .5s forwards;
}

.username {
  background: linear-gradient(45deg, #ffd700, #ffed4e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  filter: drop-shadow(0 3px 6px rgba(0, 0, 0, .3));
  opacity: 0;
  animation: slideInRight 1s ease-out 1s forwards;
}

.welcome-subtitle {
  font-size: 1.3rem;
  color: rgba(255, 255, 255, .9);
  line-height: 1.6;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, .2);
  opacity: 0;
  animation: fadeInUp 1s ease-out 1.5s forwards;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.action-btn {
  background: rgba(255, 255, 255, .15);
  backdrop-filter: blur(20px);
  border: 3px solid rgba(255, 255, 255, .8);
  border-radius: 25px;
  padding: 25px 30px;
  display: flex;
  align-items: center;
  gap: 25px;
  text-align: left;
  transition: .4s cubic-bezier(.4, 0, .2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  box-shadow: 0 15px 40px rgba(0, 0, 0, .15);
  opacity: 0;
  transform: translateY(30px);
}

.action-btn.primary {
  animation: slideInUp .8s ease-out 2s forwards;
}

.action-btn.secondary {
  animation: slideInUp .8s ease-out 2.3s forwards;
}

.action-btn:hover {
  transform: translateY(-5px);
  box-shadow: 0 25px 60px rgba(0, 0, 0, .25);
  background: rgba(255, 255, 255, .25);
}

.action-btn.primary:hover {
  border-color: #00d4ff;
  background: rgba(0, 212, 255, .1);
}

.action-btn.secondary:hover {
  border-color: #ff6b9d;
  background: rgba(255, 107, 157, .1);
}

.btn-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 8px 25px rgba(0, 0, 0, .2);
  border: 2px solid rgba(255, 255, 255, .8);
}

.action-btn.primary .btn-icon {
  background: linear-gradient(45deg, #00d4ff, #00b8d4);
}

.action-btn.secondary .btn-icon {
  background: linear-gradient(45deg, #ff6b9d, #ff3d71);
}

.action-btn.join .btn-icon {
  background: linear-gradient(45deg, #00d4ff, #00b8d4);
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
  color: #fff;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, .3);
}

.btn-desc {
  font-size: 1rem;
  color: rgba(255, 255, 255, .8);
  text-shadow: 1px 1px 2px rgba(0, 0, 0, .2);
}

.btn-arrow {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, .2);
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, .5);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  color: #fff;
  transition: .3s;
  flex-shrink: 0;
}

.action-btn:hover .btn-arrow {
  background: rgba(255, 255, 255, .3);
  border-color: rgba(255, 255, 255, .8);
  transform: translateX(5px);
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-50px)
  }

  to {
    opacity: 1;
    transform: none
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(50px)
  }

  to {
    opacity: 1;
    transform: none
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px)
  }

  to {
    opacity: 1;
    transform: none
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px)
  }

  to {
    opacity: 1;
    transform: none
  }
}

/* Responsive */
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

  .leaderboard-section {
    margin-top: 40px;
    padding: 0 15px;
  }

  .join-row {
    flex-direction: column;
    align-items: stretch;
  }

  .join-btn {
    width: 100%;
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
