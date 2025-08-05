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
const startQuiz = () => {
  console.log('🎯 startQuiz called, quizInfo:', quizInfo.value)
  
  // ✅ SỬA: LẤY QUIZ ID TỪ BACKEND RESPONSE STRUCTURE
  const quizId = quizInfo.value?.quiz?.quizId
  const userId = localStorage.getItem('userId') || '1'
  
  console.log('🔍 Quiz ID from response:', quizId)
  console.log('🔍 User ID:', userId)
  
  if (quizId) {
    console.log('✅ Navigating to quiz:', quizId, 'user:', userId)
    router.push(`/quiz/${quizId}/${userId}/play`)
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.join-quiz-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  width: 100%;
  max-width: 500px;
}

.card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  text-align: center;
}

.header-icon {
  font-size: 3rem;
  margin-bottom: 10px;
}

.header-title {
  font-size: 2rem;
  font-weight: 700;
  margin: 0 0 10px 0;
}

.header-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
  margin: 0;
}

.card-body {
  padding: 30px;
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
  border: 2px solid #e1e5e9;
  border-radius: 10px;
  font-size: 1.2rem;
  font-weight: 600;
  text-align: center;
  letter-spacing: 2px;
  transition: all 0.3s ease;
  box-sizing: border-box; /* Ensure padding and border are included in width */
}

.code-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.code-input.error {
  border-color: #e74c3c;
}

.code-input.success {
  border-color: #28a745;
}

.input-decoration {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #e1e5e9;
  color: #666;
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  white-space: nowrap;
  pointer-events: none;
  transition: opacity 0.3s ease;
}

.code-input:not(:placeholder-shown) + .input-decoration,
.code-input:focus + .input-decoration {
  opacity: 0;
}

.join-btn {
  width: 100%;
  padding: 15px 25px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.join-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.join-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.error-message {
  color: #e74c3c;
  font-size: 0.9rem;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.quiz-preview {
  background: #f8f9fa;
  border-radius: 15px;
  padding: 25px;
  margin-top: 20px;
}

.preview-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
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
  color: #333;
  margin: 0 0 5px 0;
}

.quiz-meta {
  display: flex;
  gap: 15px;
  color: #666;
  font-size: 0.9rem;
}

.quiz-status {
  background: #28a745;
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
  background: #28a745;
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
  background: #218838;
  transform: translateY(-1px);
}

.start-btn:disabled {
  background: #6c757d;
  cursor: not-allowed;
  opacity: 0.6;
  transform: none;
}

.start-btn:disabled:hover {
  background: #6c757d;
  transform: none;
}

.reset-btn {
  padding: 12px 20px;
  background: #6c757d;
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
  background: #5a6268;
}

.instructions {
  background: #e3f2fd;
  border-radius: 10px;
  padding: 20px;
  margin-top: 20px;
}

.instruction-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
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
  color: #1976d2;
  margin-right: 15px;
  flex-shrink: 0;
}

.instruction-content {
  flex: 1;
}

.instruction-content strong {
  display: block;
  margin-bottom: 5px;
  color: #333;
}

.instruction-content p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}
</style> 