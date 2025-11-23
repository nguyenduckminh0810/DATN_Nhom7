<template>
  <div class="join-quiz-container">
    <div class="join-quiz-card">
      <!-- Header -->
      <div class="card-header">
        <div class="header-icon">
          <i class="bi bi-key-fill"></i>
        </div>
        <h1 class="header-title">Tham gia Quiz</h1>
        <p class="header-subtitle">Nhập mã code để bắt đầu</p>
      </div>

      <!-- Main Content -->
      <div class="card-body">
        <!-- Code Input Section -->
        <div class="code-input-section">
          <div class="input-container">
            <input ref="codeInput" v-model="quizCode" @input="formatCode" @keyup.enter="joinQuiz"
              placeholder="Nhập mã code" maxlength="6" class="code-input"
              :class="{ 'error': hasError, 'success': quizInfo }" autocomplete="off" spellcheck="false" />
            <div class="input-decoration">
              <span class="decoration-text">CODE</span>
            </div>
          </div>

          <button @click="joinQuiz" :disabled="!quizCode || isLoading" class="join-btn">
            <i v-if="isLoading" class="bi bi-arrow-clockwise spin"></i>
            <span v-else>Tham gia ngay</span>
          </button>

          <p v-if="hasError" class="error-message">
            <i class="bi bi-exclamation-circle"></i>
            {{ errorMessage }}
          </p>
        </div>

        <!-- Quiz Found - Direct Start -->
        <div v-if="quizInfo" class="quiz-found">
          <div class="found-card">
            <div class="found-header">
              <i class="bi bi-check-circle-fill success-icon"></i>
              <h3>Đã tìm thấy quiz!</h3>
            </div>

            <div class="quiz-basic-info">
              <div class="quiz-title">
                <i class="bi bi-quiz"></i>
                <span>{{ getQuizTitle(quizInfo.quiz) }}</span>
              </div>
              <div class="quiz-code" v-if="quizInfo.quiz?.quizCode">
                <i class="bi bi-hash"></i>
                <span>Mã: {{ quizInfo.quiz.quizCode }}</span>
              </div>
            </div>

            <div class="start-message">
              <p>Quiz sẽ tự động bắt đầu trong vài giây...</p>
            </div>

            <div class="found-actions">
              <button @click="startQuiz" class="start-btn" :disabled="!quizInfo?.quiz?.quizId">
                <i class="bi bi-play-fill"></i>
                Bắt đầu làm bài ngay
              </button>
              <button @click="resetForm" class="reset-btn">
                <i class="bi bi-arrow-left"></i>
                Nhập code khác
              </button>
            </div>
          </div>
        </div>

        <!-- Instructions -->
        <div v-if="!quizInfo" class="instructions">
          <div class="instruction-card">
            <h4>Hướng dẫn sử dụng</h4>
            <div class="instruction-list">
              <div class="instruction-item">
                <div class="instruction-icon">
                  <i class="bi bi-1-circle"></i>
                </div>
                <div class="instruction-content">
                  <strong>Nhập mã code</strong>
                  <p>Nhập mã 6 ký tự được cung cấp bởi giáo viên</p>
                </div>
              </div>
              <div class="instruction-item">
                <div class="instruction-icon">
                  <i class="bi bi-2-circle"></i>
                </div>
                <div class="instruction-content">
                  <strong>Xác nhận thông tin</strong>
                  <p>Kiểm tra thông tin quiz hiển thị</p>
                </div>
              </div>
              <div class="instruction-item">
                <div class="instruction-icon">
                  <i class="bi bi-3-circle"></i>
                </div>
                <div class="instruction-content">
                  <strong>Bắt đầu làm bài</strong>
                  <p>Nhấn "Bắt đầu làm bài" để tham gia</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from '@/utils/axios'

const router = useRouter()
const route = useRoute()

const quizCode = ref('')
const quizInfo = ref(null)
const isLoading = ref(false)
const hasError = ref(false)
const errorMessage = ref('')
const codeInput = ref(null)

onMounted(() => {
  // ĐỌC CODE TỪ URL PARAMETER HOẶC ROUTE PARAMETER
  const urlCode = route.query.code || route.params.code
  if (urlCode) {
    quizCode.value = urlCode
    joinQuiz() // Tự động join nếu có code trong URL
  } else {
    // Auto focus nếu không có code
    if (codeInput.value) {
      codeInput.value.focus()
    }
  }
})

// Format code tự động
const formatCode = () => {
  quizCode.value = quizCode.value.toUpperCase().replace(/[^A-Z0-9]/g, '')
  hasError.value = false
  errorMessage.value = ''
}

// Tham gia quiz
const joinQuiz = async () => {
  if (!quizCode.value) return

  isLoading.value = true
  hasError.value = false
  errorMessage.value = ''

  try {
    const response = await axios.get(`/quiz/join/${quizCode.value}`)
    console.log(' Full response:', response.data)

    if (response.data.success) {
      quizInfo.value = response.data
      console.log(' Quiz found:', response.data)
      console.log(' Quiz ID:', response.data.quiz?.quizId)
      console.log(' Full quiz object:', response.data.quiz)
      console.log(' Quiz title:', response.data.quiz?.title)
      console.log(' Quiz creatorName:', response.data.quiz?.creatorName)
      console.log(' Quiz creator:', response.data.quiz?.creator)
      console.log(' Quiz category:', response.data.quiz?.category)
      console.log(' Quiz questions:', response.data.quiz?.questions)
      console.log(' Quiz timeLimit:', response.data.quiz?.timeLimit)
      console.log(' Quiz difficulty:', response.data.quiz?.difficulty)
      console.log(' Quiz quizCode:', response.data.quiz?.quizCode)
      console.log(' Quiz createdAt:', response.data.quiz?.createdAt)

      // Debug cấu trúc dữ liệu
      debugQuizData(response.data.quiz)

      // Tự động bắt đầu quiz sau 1 giây
      setTimeout(() => {
        startQuiz()
      }, 1000)
    } else {
      hasError.value = true
      errorMessage.value = response.data.message || 'Mã code không đúng'
    }
  } catch (error) {
    console.error(' Error joining quiz:', error)
    hasError.value = true
    errorMessage.value = error.response?.data?.message || 'Lỗi khi tham gia quiz'
  } finally {
    isLoading.value = false
  }
}

// Bắt đầu làm bài
const startQuiz = async () => {
  console.log(' startQuiz called, quizInfo:', quizInfo.value)

  //  SỬA: LẤY QUIZ ID TỪ BACKEND RESPONSE STRUCTURE
  const quizId = quizInfo.value?.quiz?.quizId
  const userId = localStorage.getItem('userId') || '1'

  console.log(' Quiz ID from response:', quizId)
  console.log(' User ID:', userId)

  if (quizId) {
    console.log(' Navigating to quiz:', quizId, 'user:', userId)
    try {
      const { quizAttemptService } = await import('@/services/quizAttemptService')
      const resp = await quizAttemptService.startAttempt(quizId)
      router.push({ name: 'PlayAttempt', params: { attemptId: resp.attemptId } })
    } catch (e) {
      console.error('Không thể bắt đầu attempt:', e)
    }
  } else {
    console.error(' No quiz ID found in quizInfo:', quizInfo.value)
    console.error(' quizInfo.quiz:', quizInfo.value?.quiz)
  }
}

// Reset form
const resetForm = () => {
  quizCode.value = ''
  quizInfo.value = null
  hasError.value = false
  errorMessage.value = ''
}

// Helper functions để hiển thị thông tin quiz
const getQuizTitle = (quiz) => {
  if (!quiz) return 'Không có tiêu đề'

  // Tìm kiếm từ nhiều nguồn khác nhau
  const title = quiz.title || quiz.quizName || quiz.name || quiz.quizTitle
  if (title) return title

  // Fallback
  return 'Không có tiêu đề'
}

const getCreatorName = (quiz) => {
  if (!quiz) return 'Không xác định'

  // Tìm kiếm từ nhiều nguồn khác nhau
  if (quiz.creatorName) return quiz.creatorName // Thêm creatorName
  if (quiz.creator) return quiz.creator
  if (quiz.user) return quiz.user.fullName || quiz.user.username || 'Không xác định'
  if (quiz.createdBy) return quiz.createdBy
  if (quiz.author) return quiz.author
  if (quiz.owner) return quiz.owner

  // Fallback - hiển thị thông tin có sẵn
  return 'Thông tin sẽ hiển thị khi bắt đầu quiz'
}

const getCategoryName = (category) => {
  if (!category) return 'Không phân loại'

  // Tìm kiếm từ nhiều nguồn khác nhau
  if (typeof category === 'string') return category
  if (category.name) return category.name
  if (category.categoryName) return category.categoryName
  if (category.title) return category.title

  // Fallback - hiển thị thông tin có sẵn
  return 'Thông tin sẽ hiển thị khi bắt đầu quiz'
}

const getStatusText = (isPublic) => {
  if (isPublic === true || isPublic === 'true') return 'Công khai'
  if (isPublic === false || isPublic === 'false') return 'Riêng tư'
  return 'Không xác định'
}

const getStatusIcon = (isPublic) => {
  if (isPublic === true || isPublic === 'true') return 'bi bi-globe'
  if (isPublic === false || isPublic === 'false') return 'bi bi-lock'
  return 'bi bi-question-circle'
}

const getStatusClass = (isPublic) => {
  if (isPublic === true || isPublic === 'true') return 'status-public'
  if (isPublic === false || isPublic === 'false') return 'status-private'
  return 'status-unknown'
}

const getQuestionCount = (quiz) => {
  if (!quiz) return 'Thông tin sẽ hiển thị khi bắt đầu quiz'

  // Tìm kiếm từ nhiều nguồn khác nhau
  if (quiz.questionCount) return `${quiz.questionCount} câu hỏi`
  if (quiz.questions && Array.isArray(quiz.questions)) return `${quiz.questions.length} câu hỏi`
  if (quiz.totalQuestions) return `${quiz.totalQuestions} câu hỏi`
  if (quiz.numberOfQuestions) return `${quiz.numberOfQuestions} câu hỏi`

  // Fallback - hiển thị thông tin có sẵn
  return 'Thông tin sẽ hiển thị khi bắt đầu quiz'
}

const getTimeLimit = (quiz) => {
  if (!quiz) return 'Thông tin sẽ hiển thị khi bắt đầu quiz'

  // Tìm kiếm từ nhiều nguồn khác nhau
  if (quiz.timeLimit) return `${quiz.timeLimit} phút`
  if (quiz.duration) return `${quiz.duration} phút`
  if (quiz.timeAllowed) return `${quiz.timeAllowed} phút`
  if (quiz.maxTime) return `${quiz.maxTime} phút`

  // Fallback - hiển thị thông tin có sẵn
  return 'Thông tin sẽ hiển thị khi bắt đầu quiz'
}

const getDifficulty = (quiz) => {
  if (!quiz) return 'Thông tin sẽ hiển thị khi bắt đầu quiz'

  // Tìm kiếm từ nhiều nguồn khác nhau
  if (quiz.difficulty) {
    const diff = quiz.difficulty.toLowerCase()
    if (diff === 'easy' || diff === 'dễ') return 'Dễ'
    if (diff === 'medium' || diff === 'trung bình') return 'Trung bình'
    if (diff === 'hard' || diff === 'khó') return 'Khó'
    return quiz.difficulty
  }

  // Fallback - hiển thị thông tin có sẵn
  return 'Thông tin sẽ hiển thị khi bắt đầu quiz'
}

// Debug function để kiểm tra cấu trúc dữ liệu
const debugQuizData = (quiz) => {
  if (!quiz) {
    console.log(' Quiz object is null/undefined')
    return
  }

  console.log(' Debug Quiz Data Structure:')
  console.log('  - quiz.title:', quiz.title)
  console.log('  - quiz.quizName:', quiz.quizName)
  console.log('  - quiz.name:', quiz.name)
  console.log('  - quiz.quizTitle:', quiz.quizTitle)
  console.log('  - quiz.creatorName:', quiz.creatorName) // Thêm creatorName
  console.log('  - quiz.creator:', quiz.creator)
  console.log('  - quiz.user:', quiz.user)
  console.log('  - quiz.createdBy:', quiz.createdBy)
  console.log('  - quiz.author:', quiz.author)
  console.log('  - quiz.owner:', quiz.owner)
  console.log('  - quiz.category:', quiz.category)
  console.log('  - quiz.questionCount:', quiz.questionCount)
  console.log('  - quiz.totalQuestions:', quiz.totalQuestions)
  console.log('  - quiz.numberOfQuestions:', quiz.numberOfQuestions)
  console.log('  - quiz.questions:', quiz.questions)
  console.log('  - quiz.timeLimit:', quiz.timeLimit)
  console.log('  - quiz.duration:', quiz.duration)
  console.log('  - quiz.timeAllowed:', quiz.timeAllowed)
  console.log('  - quiz.maxTime:', quiz.maxTime)
  console.log('  - quiz.difficulty:', quiz.difficulty)
  console.log('  - quiz.isPublic:', quiz.isPublic)
  console.log('  - quiz.quizCode:', quiz.quizCode) // Thêm quizCode
  console.log('  - quiz.createdAt:', quiz.createdAt) // Thêm createdAt
}
</script>

<style scoped>
.join-quiz-container {
  min-height: 100vh;
  background: var(--app-background);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.join-quiz-card {
  background: var(--card-bg);
  border-radius: 20px;
  box-shadow: 0 20px 40px var(--shadow-color);
  overflow: hidden;
  width: 100%;
  max-width: 500px;
  border: 1px solid var(--border-color);
  backdrop-filter: blur(10px);
  position: relative;
}

.card-header {
  background: var(--card-header-bg);
  color: var(--card-header-text);
  padding: 30px;
  text-align: center;
  border-bottom: 1px solid var(--border-color);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.header-icon {
  font-size: 3rem;
  margin-bottom: 10px;
  color: var(--primary-color);
}

.header-title {
  font-size: 2rem;
  font-weight: 700;
  margin: 0 0 10px 0;
  color: var(--text-primary);
}

.header-subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary);
  margin: 0;
}

.card-body {
  padding: 30px;
  background: var(--card-bg);
  color: var(--text-primary);
  border-top: 1px solid var(--border-color);
}

.code-input-section {
  margin-bottom: 30px;
}

.input-container {
  position: relative;
  margin-bottom: 15px;
}

.code-input {
  width: 100%;
  padding: 15px 20px;
  border: 2px solid var(--input-border);
  border-radius: 10px;
  font-size: 1.2rem;
  font-weight: 600;
  text-align: center;
  letter-spacing: 2px;
  transition: all 0.3s ease;
  box-sizing: border-box;
  background: var(--input-bg);
  color: var(--text-primary);
  min-height: 50px;
}

.code-input::placeholder {
  color: var(--text-muted);
  opacity: 0.8;
  font-weight: 500;
}

.code-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--primary-shadow);
  background: var(--input-bg);
  color: var(--text-primary);
}

.code-input.error {
  border-color: var(--danger-color);
  background: var(--danger-bg);
}

.code-input.success {
  border-color: var(--success-color);
  background: var(--success-bg);
}

.input-decoration {
  display: none;
  /* Ẩn hoàn toàn text "CODE" */
}

.join-btn {
  width: 100%;
  padding: 15px 25px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 700;
  font-size: 1.1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
  border: 2px solid rgba(59, 130, 246, 0.2);
  position: relative;
  overflow: hidden;
}

/* Ensure button text is always visible */
.join-btn span {
  position: relative;
  z-index: 2;
  color: white;
  font-weight: 700;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.join-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.4);
  border-color: rgba(59, 130, 246, 0.4);
}

.join-btn:disabled {
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 2px 8px rgba(107, 114, 128, 0.2);
  border-color: rgba(107, 114, 128, 0.3);
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

.error-message {
  color: var(--danger-color);
  font-size: 0.9rem;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.quiz-preview {
  background: var(--bg-secondary);
  border-radius: 15px;
  padding: 25px;
  margin-top: 20px;
  border: 1px solid var(--border-color);
  backdrop-filter: blur(5px);
}

.preview-card {
  background: var(--card-bg);
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 5px 15px var(--shadow-color);
  border: 1px solid var(--border-color);
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.quiz-info {
  flex: 1;
}

.quiz-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 5px 0;
}

.quiz-meta {
  display: flex;
  gap: 15px;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.quiz-status {
  display: flex;
  align-items: center;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
  border: 1px solid;
}

.status-public {
  background: var(--success-color);
  color: white;
  border-color: var(--success-color);
}

.status-private {
  background: var(--warning-color);
  color: white;
  border-color: var(--warning-color);
}

.status-unknown {
  background: var(--text-muted);
  color: white;
  border-color: var(--text-muted);
}

.quiz-details {
  display: flex;
  gap: 20px;
  margin: 20px 0;
  padding: 15px;
  background: var(--bg-secondary);
  border-radius: 10px;
  border: 1px solid var(--border-color);
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.detail-item i {
  color: var(--primary-color);
  font-size: 1rem;
}

.info-notice {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  color: white;
  padding: 15px;
  margin: 15px 0;
  border-radius: 10px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.info-notice i {
  margin-right: 8px;
  font-size: 1.1rem;
}

.quiz-found {
  margin-top: 2rem;
}

.found-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 2rem;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  margin: 0 auto;
}

.found-header {
  margin-bottom: 1.5rem;
}

.success-icon {
  font-size: 3rem;
  color: #10b981;
  margin-bottom: 1rem;
  display: block;
}

.found-header h3 {
  color: var(--text-primary);
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0;
}

.quiz-basic-info {
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: var(--bg-secondary);
  border-radius: 12px;
}

.quiz-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
}

.quiz-title i {
  color: var(--primary-color);
  font-size: 1.3rem;
}

.quiz-code {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-size: 0.95rem;
  color: var(--text-secondary);
}

.quiz-code i {
  color: var(--primary-color);
}

.start-message {
  margin-bottom: 2rem;
}

.start-message p {
  color: var(--text-secondary);
  font-size: 1rem;
  margin: 0;
}

.found-actions {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: center;
}

.found-actions .start-btn {
  min-width: 200px;
  padding: 1rem 2rem;
  font-size: 1.1rem;
  font-weight: 600;
}

.found-actions .reset-btn {
  min-width: 200px;
  padding: 0.75rem 1.5rem;
  font-size: 1rem;
}

.preview-actions {
  display: flex;
  gap: 10px;
}

.start-btn {
  flex: 1;
  padding: 12px 20px;
  background: var(--success-color);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.start-btn:hover {
  background: var(--success-dark);
  transform: translateY(-1px);
}

.start-btn:disabled {
  background: var(--text-muted);
  cursor: not-allowed;
  opacity: 0.6;
  transform: none;
}

.start-btn:disabled:hover {
  background: var(--text-muted);
  transform: none;
}

.reset-btn {
  padding: 12px 20px;
  background: var(--text-muted);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.reset-btn:hover {
  background: var(--text-secondary);
}

.instructions {
  background: var(--bg-secondary);
  border-radius: 10px;
  padding: 20px;
  margin-top: 20px;
  border: 1px solid var(--border-color);
  backdrop-filter: blur(5px);
}

.instruction-card {
  background: var(--card-bg);
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 5px 15px var(--shadow-color);
  border: 1px solid var(--border-color);
}

.instruction-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.instruction-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 15px;
}

.instruction-icon {
  font-size: 1.5rem;
  color: var(--primary-color);
  margin-right: 15px;
  flex-shrink: 0;
}

.instruction-content {
  flex: 1;
}

.instruction-content strong {
  display: block;
  margin-bottom: 5px;
  color: var(--text-primary);
}

.instruction-content p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 0.9rem;
}
</style>