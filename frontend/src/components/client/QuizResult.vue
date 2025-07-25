<script setup>
import { useRoute, useRouter } from 'vue-router'
import { computed, onMounted, ref } from 'vue'
import { useLogin } from './useLogin'

const { username } = useLogin()
const route = useRoute()
const router = useRouter()

const quizId = route.params.quizId
const userId = route.params.userId
const score = parseInt(route.query.score) || 0

const correctAnswers = ref(JSON.parse(route.query.correctAnswers || '[]'))
const selectedAnswers = ref(JSON.parse(route.query.selectedAnswers || '[]'))
const isLoaded = ref(false)

onMounted(() => {
    correctAnswers.value = JSON.parse(localStorage.getItem('correctAnswers') || '[]')
    selectedAnswers.value = JSON.parse(localStorage.getItem('selectedAnswers') || '[]')

    // Animation delay
    setTimeout(() => {
        isLoaded.value = true
    }, 500)

    // Sau khi dùng xong thì xóa
    localStorage.removeItem('correctAnswers')
    localStorage.removeItem('selectedAnswers')
})

const radius = 85
const circumference = 2 * Math.PI * radius

const dashOffset = computed(() => {
    const percent = Math.min(score, 100)
    return circumference - (percent / 100) * circumference
})

const scoreColor = computed(() => {
    if (score >= 80) return '#28a745'
    if (score >= 60) return '#17a2b8'
    if (score >= 40) return '#ffc107'
    return '#dc3545'
})

const performanceLevel = computed(() => {
    if (score >= 90) return { text: 'Xuất sắc', icon: '🏆', class: 'excellent' }
    if (score >= 80) return { text: 'Tốt', icon: '🎉', class: 'good' }
    if (score >= 60) return { text: 'Khá', icon: '👍', class: 'fair' }
    if (score >= 40) return { text: 'Trung bình', icon: '📈', class: 'average' }
    return { text: 'Cần cải thiện', icon: '💪', class: 'poor' }
})

const combinedResults = computed(() => {
    return correctAnswers.value.map((ca, index) => {
        const userAns = selectedAnswers.value.find(sa => sa.questionId === ca.questionId)
        return {
            questionId: ca.questionId,
            questionNumber: index + 1,
            correctAnswerId: ca.correctAnswerId,
            selectedAnswerId: userAns?.answerId ?? null,
            isCorrect: userAns?.answerId === ca.correctAnswerId
        }
    })
})

const stats = computed(() => {
    const total = combinedResults.value.length
    const correct = combinedResults.value.filter(r => r.isCorrect).length
    const incorrect = total - correct
    const accuracy = total > 0 ? Math.round((correct / total) * 100) : 0
    
    return { total, correct, incorrect, accuracy }
})

function goBack() {
    router.push({ name: 'Home' })
}

function playAgain() {
    router.push({ name: 'PlayQuiz', params: { quizId, userId } })
}

function viewQuizzes() {
    router.push({ name: 'ListQuizPublic' })
}
</script>

<template>
  <div class="quiz-result-container">
    <!-- Animated Background -->
    <div class="background-animation">
      <div class="floating-element" v-for="n in 15" :key="n" :style="{ 
        left: Math.random() * 100 + '%', 
        animationDelay: Math.random() * 3 + 's',
        animationDuration: (3 + Math.random() * 2) + 's'
      }">
        {{ ['🎉', '⭐', '🏆', '🎊', '✨'][Math.floor(Math.random() * 5)] }}
      </div>
    </div>

    <div class="container">
      <!-- Hero Section -->
      <div class="hero-section" :class="{ 'loaded': isLoaded }">
        <div class="hero-content">
          <div class="celebration-icon">
            {{ performanceLevel.icon }}
          </div>
          <h1 class="hero-title">
            Chúc mừng {{ username || 'bạn' }}!
          </h1>
          <p class="hero-subtitle">
            Bạn đã hoàn thành quiz với kết quả {{ performanceLevel.text.toLowerCase() }}
          </p>
        </div>
      </div>

      <!-- Main Content -->
      <div class="content-section">
        <div class="row g-4">
          <!-- Score Card -->
          <div class="col-lg-6">
            <div class="result-card score-card" :class="{ 'loaded': isLoaded }">
              <div class="card-header">
                <h3 class="card-title">
                  <i class="bi bi-trophy-fill"></i>
                  Điểm số của bạn
                </h3>
              </div>
              <div class="card-body">
                <!-- Score Circle -->
                <div class="score-circle-container">
                  <svg class="score-circle" width="200" height="200" viewBox="0 0 200 200">
                    <!-- Background Circle -->
                    <circle 
                      cx="100" cy="100" :r="radius" 
                      fill="none" 
                      stroke="rgba(255, 255, 255, 0.2)" 
                      stroke-width="8"
                    />
                    <!-- Progress Circle -->
                    <circle 
                      cx="100" cy="100" :r="radius" 
                      fill="none" 
                      :stroke="scoreColor" 
                      stroke-width="8"
                      stroke-linecap="round" 
                      :stroke-dasharray="circumference" 
                      :stroke-dashoffset="isLoaded ? dashOffset : circumference"
                      transform="rotate(-90 100 100)"
                      class="progress-ring"
                    />
                    <!-- Score Text -->
                    <text 
                      x="100" y="95" 
                      text-anchor="middle" 
                      dominant-baseline="middle" 
                      class="score-text"
                    >
                      {{ isLoaded ? score : 0 }}
                    </text>
                    <text 
                      x="100" y="115" 
                      text-anchor="middle" 
                      dominant-baseline="middle" 
                      class="score-unit"
                    >
                      điểm
                    </text>
                  </svg>
                </div>

                <!-- Performance Badge -->
                <div class="performance-badge" :class="performanceLevel.class">
                  <span class="badge-icon">{{ performanceLevel.icon }}</span>
                  <span class="badge-text">{{ performanceLevel.text }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Stats Card -->
          <div class="col-lg-6">
            <div class="result-card stats-card" :class="{ 'loaded': isLoaded }">
              <div class="card-header">
                <h3 class="card-title">
                  <i class="bi bi-graph-up"></i>
                  Thống kê chi tiết
                </h3>
              </div>
              <div class="card-body">
                <div class="stats-grid">
                  <div class="stat-item">
                    <div class="stat-icon total">
                      <i class="bi bi-list-ul"></i>
                    </div>
                    <div class="stat-content">
                      <div class="stat-number">{{ stats.total }}</div>
                      <div class="stat-label">Tổng câu hỏi</div>
                    </div>
                  </div>

                  <div class="stat-item">
                    <div class="stat-icon correct">
                      <i class="bi bi-check-circle-fill"></i>
                    </div>
                    <div class="stat-content">
                      <div class="stat-number">{{ stats.correct }}</div>
                      <div class="stat-label">Câu đúng</div>
                    </div>
                  </div>

                  <div class="stat-item">
                    <div class="stat-icon incorrect">
                      <i class="bi bi-x-circle-fill"></i>
                    </div>
                    <div class="stat-content">
                      <div class="stat-number">{{ stats.incorrect }}</div>
                      <div class="stat-label">Câu sai</div>
                    </div>
                  </div>

                  <div class="stat-item">
                    <div class="stat-icon accuracy">
                      <i class="bi bi-percent"></i>
                    </div>
                    <div class="stat-content">
                      <div class="stat-number">{{ stats.accuracy }}%</div>
                      <div class="stat-label">Độ chính xác</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Detailed Results -->
        <div class="result-card details-card" :class="{ 'loaded': isLoaded }">
          <div class="card-header">
            <h3 class="card-title">
              <i class="bi bi-clipboard-data"></i>
              Chi tiết từng câu hỏi
            </h3>
          </div>
          <div class="card-body">
            <div class="results-list">
              <div 
                v-for="(result, index) in combinedResults" 
                :key="result.questionId"
                class="result-item"
                :class="{ 'correct': result.isCorrect, 'incorrect': !result.isCorrect }"
                :style="{ animationDelay: (index * 0.1) + 's' }"
              >
                <div class="result-number">
                  <span class="number">{{ result.questionNumber }}</span>
                  <div class="result-indicator">
                    <i v-if="result.isCorrect" class="bi bi-check-lg"></i>
                    <i v-else class="bi bi-x-lg"></i>
                  </div>
                </div>
                <div class="result-content">
                  <div class="result-info">
                    <span class="label">Câu trả lời của bạn:</span>
                    <span class="value" :class="{ 'correct': result.isCorrect, 'incorrect': !result.isCorrect }">
                      {{ result.selectedAnswerId ?? 'Không chọn' }}
                    </span>
                  </div>
                  <div class="result-info">
                    <span class="label">Đáp án đúng:</span>
                    <span class="value correct">{{ result.correctAnswerId }}</span>
                  </div>
                </div>
                <div class="result-status">
                  <span class="status-badge" :class="{ 'correct': result.isCorrect, 'incorrect': !result.isCorrect }">
                    {{ result.isCorrect ? 'Đúng' : 'Sai' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="actions-section" :class="{ 'loaded': isLoaded }">
          <div class="actions-container">
            <button @click="playAgain" class="action-btn primary">
              <i class="bi bi-arrow-clockwise"></i>
              <span>Chơi lại</span>
            </button>
            <button @click="viewQuizzes" class="action-btn secondary">
              <i class="bi bi-collection"></i>
              <span>Xem quiz khác</span>
            </button>
            <button @click="goBack" class="action-btn outline">
              <i class="bi bi-house"></i>
              <span>Về trang chủ</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.quiz-result-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  padding: 40px 0;
  overflow-x: hidden;
}

.quiz-result-container::before {
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
}

/* Background Animation */
.background-animation {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.floating-element {
  position: absolute;
  font-size: 1.5rem;
  opacity: 0.7;
  animation: float 4s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
    opacity: 0.7;
  }
  25% {
    transform: translateY(-20px) rotate(90deg);
    opacity: 1;
  }
  50% {
    transform: translateY(-40px) rotate(180deg);
    opacity: 0.5;
  }
  75% {
    transform: translateY(-20px) rotate(270deg);
    opacity: 1;
  }
}

/* Container */
.container {
  position: relative;
  z-index: 2;
  max-width: 1200px;
}

/* Hero Section */
.hero-section {
  text-align: center;
  margin-bottom: 50px;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.hero-section.loaded {
  opacity: 1;
  transform: translateY(0);
}

.hero-content {
  max-width: 600px;
  margin: 0 auto;
}

.celebration-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-20px);
  }
  60% {
    transform: translateY(-10px);
  }
}

.hero-title {
  font-size: 3rem;
  font-weight: 800;
  color: white;
  margin-bottom: 15px;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.hero-subtitle {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 0;
  font-weight: 400;
}

/* Content Section */
.content-section {
  position: relative;
  z-index: 2;
}

/* Result Cards */
.result-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
  margin-bottom: 30px;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.result-card.loaded {
  opacity: 1;
  transform: translateY(0);
}

.result-card:nth-child(2) {
  transition-delay: 0.2s;
}

.result-card:nth-child(3) {
  transition-delay: 0.4s;
}

.card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 25px;
  border: none;
}

.card-title {
  margin: 0;
  font-size: 1.3rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-body {
  padding: 30px 25px;
}

/* Score Card */
.score-circle-container {
  display: flex;
  justify-content: center;
  margin-bottom: 25px;
}

.score-circle {
  filter: drop-shadow(0 10px 20px rgba(0, 0, 0, 0.1));
}

.progress-ring {
  transition: stroke-dashoffset 2s cubic-bezier(0.4, 0, 0.2, 1);
}

.score-text {
  font-size: 2.5rem;
  font-weight: 800;
  fill: #333;
}

.score-unit {
  font-size: 1rem;
  font-weight: 500;
  fill: #666;
}

.performance-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 15px 25px;
  border-radius: 50px;
  font-weight: 600;
  font-size: 1.1rem;
  margin: 0 auto;
  max-width: fit-content;
}

.performance-badge.excellent {
  background: linear-gradient(135deg, #28a745, #20c997);
  color: white;
}

.performance-badge.good {
  background: linear-gradient(135deg, #17a2b8, #007bff);
  color: white;
}

.performance-badge.fair {
  background: linear-gradient(135deg, #ffc107, #fd7e14);
  color: white;
}

.performance-badge.average {
  background: linear-gradient(135deg, #fd7e14, #dc3545);
  color: white;
}

.performance-badge.poor {
  background: linear-gradient(135deg, #dc3545, #6f42c1);
  color: white;
}

.badge-icon {
  font-size: 1.3rem;
}

/* Stats Card */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 15px;
  transition: transform 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-3px);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  color: white;
}

.stat-icon.total {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.stat-icon.correct {
  background: linear-gradient(135deg, #28a745, #20c997);
}

.stat-icon.incorrect {
  background: linear-gradient(135deg, #dc3545, #fd5e53);
}

.stat-icon.accuracy {
  background: linear-gradient(135deg, #17a2b8, #007bff);
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 1.8rem;
  font-weight: 800;
  color: #333;
  line-height: 1;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
}

/* Details Card */
.results-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: rgba(102, 126, 234, 0.03);
  border-radius: 15px;
  border-left: 4px solid transparent;
  opacity: 0;
  animation: slideInUp 0.6s forwards;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.result-item.correct {
  border-left-color: #28a745;
  background: rgba(40, 167, 69, 0.05);
}

.result-item.incorrect {
  border-left-color: #dc3545;
  background: rgba(220, 53, 69, 0.05);
}

.result-number {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  min-width: 60px;
}

.number {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.9rem;
}

.result-indicator {
  width: 25px;
  height: 25px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
}

.result-item.correct .result-indicator {
  background: #28a745;
  color: white;
}

.result-item.incorrect .result-indicator {
  background: #dc3545;
  color: white;
}

.result-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.result-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.label {
  font-weight: 500;
  color: #666;
  font-size: 0.9rem;
}

.value {
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.9rem;
}

.value.correct {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.value.incorrect {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.result-status {
  min-width: 80px;
  text-align: center;
}

.status-badge {
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 0.85rem;
}

.status-badge.correct {
  background: #28a745;
  color: white;
}

.status-badge.incorrect {
  background: #dc3545;
  color: white;
}

/* Actions Section */
.actions-section {
  margin-top: 40px;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.6s;
}

.actions-section.loaded {
  opacity: 1;
  transform: translateY(0);
}

.actions-container {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px 25px;
  border-radius: 50px;
  font-weight: 600;
  font-size: 1rem;
  border: none;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  text-decoration: none;
  min-width: 160px;
  justify-content: center;
}

.action-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

.action-btn.primary {
  background: linear-gradient(135deg, #28a745, #20c997);
  color: white;
}

.action-btn.secondary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.action-btn.outline {
  background: rgba(255, 255, 255, 0.9);
  color: #667eea;
  border: 2px solid rgba(102, 126, 234, 0.3);
}

.action-btn.outline:hover {
  background: rgba(102, 126, 234, 0.1);
  border-color: #667eea;
}

/* Responsive Design */
@media (max-width: 768px) {
  .hero-title {
    font-size: 2.2rem;
  }
  
  .hero-subtitle {
    font-size: 1rem;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .result-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .result-number {
    flex-direction: row;
    min-width: auto;
  }
  
  .actions-container {
    flex-direction: column;
    align-items: center;
  }
  
  .action-btn {
    width: 100%;
    max-width: 300px;
  }
}

@media (max-width: 576px) {
  .quiz-result-container {
    padding: 20px 0;
  }
  
  .card-body {
    padding: 20px 15px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .celebration-icon {
    font-size: 3rem;
  }
  
  .hero-title {
    font-size: 1.8rem;
  }
}
</style>
