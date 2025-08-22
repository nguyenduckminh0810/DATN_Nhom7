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
            <input
              ref="codeInput"
              v-model="quizCode"
              @input="formatCode"
              @keyup.enter="joinQuiz"
              placeholder="Nhập mã code"
              maxlength="6"
              class="code-input"
              :class="{ 'error': hasError, 'success': quizInfo }"
              autocomplete="off"
              spellcheck="false"
            />
            <div class="input-decoration">
              <span class="decoration-text">CODE</span>
            </div>
          </div>
          
          <button 
            @click="joinQuiz" 
            :disabled="!quizCode || isLoading"
            class="join-btn"
          >
            <i v-if="isLoading" class="bi bi-arrow-clockwise spin"></i>
            <span v-else>Tham gia ngay</span>
          </button>
          
          <p v-if="hasError" class="error-message">
            <i class="bi bi-exclamation-circle"></i>
            {{ errorMessage }}
          </p>
        </div>

        <!-- Quiz Preview (Quizizz Style) -->
        <div v-if="quizInfo" class="quiz-preview">
          <div class="preview-card">
            <div class="preview-header">
              <div class="quiz-info">
                <h3 class="quiz-title">{{ quizInfo.title }}</h3>
                <div class="quiz-meta">
                  <span class="creator">
                    <i class="bi bi-person-circle"></i>
                    {{ quizInfo.creator }}
                  </span>
                  <span class="category">
                    <i class="bi bi-tag"></i>
                    {{ quizInfo.category?.name || 'Không phân loại' }}
                  </span>
                </div>
              </div>
              <div class="quiz-status">
                <span class="status-badge">
                  <i class="bi bi-globe"></i>
                  Công khai
                </span>
              </div>
            </div>
            
            <div class="preview-actions">
              <button @click="startQuiz" class="start-btn" :disabled="!quizInfo?.quiz?.quizId">
                <i class="bi bi-play-fill"></i>
                Bắt đầu làm bài
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
  // ✅ ĐỌC CODE TỪ URL PARAMETER HOẶC ROUTE PARAMETER
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
    console.log('🔍 Full response:', response.data)
    
    if (response.data.success) {
      quizInfo.value = response.data
      console.log('✅ Quiz found:', response.data)
      console.log('✅ Quiz ID:', response.data.quiz?.quizId)
    } else {
      hasError.value = true
      errorMessage.value = response.data.message || 'Mã code không đúng'
    }
  } catch (error) {
    console.error('❌ Error joining quiz:', error)
    hasError.value = true
    errorMessage.value = error.response?.data?.message || 'Lỗi khi tham gia quiz'
  } finally {
    isLoading.value = false
  }
}

// Bắt đầu làm bài
const startQuiz = async () => {
  console.log('🎯 startQuiz called, quizInfo:', quizInfo.value)
  
  // ✅ SỬA: LẤY QUIZ ID TỪ BACKEND RESPONSE STRUCTURE
  const quizId = quizInfo.value?.quiz?.quizId
  const userId = localStorage.getItem('userId') || '1'
  
  console.log('🔍 Quiz ID from response:', quizId)
  console.log('🔍 User ID:', userId)
  
  if (quizId) {
    console.log('✅ Navigating to quiz:', quizId, 'user:', userId)
    try {
      const { quizAttemptService } = await import('@/services/quizAttemptService')
      const resp = await quizAttemptService.startAttempt(quizId)
      router.push({ name: 'PlayAttempt', params: { attemptId: resp.attemptId } })
    } catch (e) {
      console.error('Không thể bắt đầu attempt:', e)
    }
  } else {
    console.error('❌ No quiz ID found in quizInfo:', quizInfo.value)
    console.error('❌ quizInfo.quiz:', quizInfo.value?.quiz)
  }
}

// Reset form
const resetForm = () => {
  quizCode.value = ''
  quizInfo.value = null
  hasError.value = false
  errorMessage.value = ''
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
  display: none; /* Ẩn hoàn toàn text "CODE" */
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
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
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
  background: var(--success-color);
  color: white;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
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