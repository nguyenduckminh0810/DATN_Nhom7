<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/utils/axios'
import { useUserStore } from '@/stores/user'
import ResumeQuizModal from './ResumeQuizModal.vue'
import { quizResumeService, progressStorageService, autoSaveService } from '@/services/quizResumeService'

/* ✅ THÊM: để cập nhật badge thông báo tức thì */
import { useNotificationStore } from '@/stores/notification'
import { storeToRefs } from 'pinia'

const route = useRoute()
const router = useRouter()

const attemptId = route.params.attemptId
let quizId = route.params.quizId

const userStore = useUserStore()
let userId = localStorage.getItem('userId')
  || userStore?.user?.id
  || route.params.userId
  || (() => {
    try { return JSON.parse(localStorage.getItem('user') || '{}')?.id }
    catch { return null }
  })()

/* ✅ THÊM: khởi tạo store thông báo */
const notificationStore = useNotificationStore()
const { unreadCount } = storeToRefs(notificationStore)

const questions = ref([])
const currentQuestionIndex = ref(0)
// Chỉ số xa nhất đã đạt tới để khóa thanh tiến độ không lùi khi quay lại
const furthestQuestionIndex = ref(0)
const selectedAnswers = ref({})
// Lưu deadline theo từng câu để thời gian tiếp tục trôi ngay cả khi rời câu
const deadlineByQuestion = ref({}) // { [questionId]: timestampMillis }
// Các câu hỏi đã bị khóa (hết giờ hoặc đã chuyển qua)
const lockedQuestionIds = ref(new Set())
const countdown = ref(30)
const isLoading = ref(true)
const showNextAnimation = ref(false)
const quizTitle = ref('')
const startTime = ref(null) // Thêm thời gian bắt đầu
let timer = null

// ✅ RESUME QUIZ STATE
const showResumeModal = ref(false)
const attemptData = ref(null)
const currentAttemptId = ref(null)
let autoSaveIntervalId = null

// Computed properties
const currentQuestion = computed(() => questions.value[currentQuestionIndex.value])
const isCurrentLocked = computed(() => !!currentQuestion.value && lockedQuestionIds.value.has(currentQuestion.value.id))
const progress = computed(() => ((furthestQuestionIndex.value + 1) / Math.max(questions.value.length, 1)) * 100)
// Lấy timelimit; nếu câu đã bị khóa thì coi như không đếm (0)
const currentTimeLimit = computed(() => (isCurrentLocked.value ? 0 : (currentQuestion.value?.timeLimit ?? 30)))
// Tránh chia cho 0 khi không giới hạn
const timeProgress = computed(() => (currentTimeLimit.value > 0 ? (countdown.value / currentTimeLimit.value) * 100 : 100))
const timeColor = computed(() => {
  const halfTime = currentTimeLimit.value / 2
  const quarterTime = currentTimeLimit.value / 4
  if (countdown.value > halfTime) return '#4ecdc4'
  if (countdown.value > quarterTime) return '#ffd700'
  return '#ff4757'
})

// Ngưỡng cảnh báo linh hoạt: min(10s, max(3s, 30% thời gian))
const warnThreshold = computed(() => {
  if (currentTimeLimit.value <= 0) return 0
  const percent = Math.ceil(currentTimeLimit.value * 0.3)
  return Math.min(10, Math.max(3, percent))
})

function startTimer() {
  clearInterval(timer)
  // Không đếm khi không giới hạn hoặc câu đã khóa
  if (currentTimeLimit.value <= 0 || isCurrentLocked.value) {
    const qid = currentQuestion.value?.id
    if (qid && deadlineByQuestion.value[qid]) {
      const remaining = Math.ceil((deadlineByQuestion.value[qid] - Date.now()) / 1000)
      countdown.value = Math.max(0, remaining)
    } else {
      countdown.value = 0
    }
    if (!startTime.value) startTime.value = Date.now()
    return
  }
  const qid = currentQuestion.value?.id
  // Khởi tạo deadline nếu chưa có
  if (qid && !deadlineByQuestion.value[qid]) {
    deadlineByQuestion.value[qid] = Date.now() + currentTimeLimit.value * 1000
  }
  const remaining = qid ? Math.ceil((deadlineByQuestion.value[qid] - Date.now()) / 1000) : currentTimeLimit.value
  countdown.value = Math.max(0, remaining)
  // Chỉ ghi lại thời điểm bắt đầu quiz một lần
  if (!startTime.value) startTime.value = Date.now()
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      // Khóa câu hiện tại vì đã hết giờ
      if (currentQuestion.value?.id) lockedQuestionIds.value.add(currentQuestion.value.id)
      nextQuestion()
    }
  }, 1000)
}

onMounted(async () => {
  try {
    console.info('🚀 CHECK start - Kiểm tra attempt dở cho quiz:', quizId)

    // Nếu đang ở flow attemptId thì lấy câu hỏi theo attempt
    if (attemptId) {
      console.info('📋 Đang ở flow attemptId, load attempt hiện tại')
      const { quizAttemptService } = await import('@/services/quizAttemptService')
      const resp = await quizAttemptService.getAttemptQuestions(attemptId)
      quizId = resp.quizId
      currentAttemptId.value = attemptId
      quizTitle.value = resp.quizTitle || 'Quiz'
      const questionList = resp.questions || []

      const enrichedQuestions = await Promise.all(
        questionList.map(async (question) => {
          try {
            const ansRes = await api.get(`/answer/${question.id}`)
            return { ...question, answers: ansRes.data || [] }
          } catch (err) {
            console.error(`Lỗi khi lấy answers cho câu hỏi ${question.id}:`, err)
            return { ...question, answers: [] }
          }
        }),
      )

      questions.value = enrichedQuestions
      isLoading.value = false
      startTimer()
      startAutoSave() // Bắt đầu auto-save cho attempt hiện tại
      return
    }

    // ✅ FLOW MỚI: Kiểm tra attempt dở trước khi tạo mới
    if (quizId) {
      try {
        const response = await quizResumeService.checkInProgressAttempt(quizId)
        console.info('🔍 CHECK ok - Response:', response)

        if (response.hasInProgressAttempt) {
          console.info('📋 OPEN MODAL - Có attempt dở, hiển thị modal resume')
          attemptData.value = response.attemptData
          showResumeModal.value = true
          // Không load questions ngay, đợi user quyết định
          return
        } else {
          console.info('✅ CHECK 204 - Không có attempt dở, tạo attempt mới')
          await createNewAttempt()
        }
      } catch (error) {
        console.error('❌ Lỗi khi kiểm tra attempt dở:', error)
        // Fallback: tạo attempt mới
        await createNewAttempt()
      }
    }
  } catch (err) {
    console.error('Lỗi khi tải câu hỏi:', err)
    isLoading.value = false
  }
})

// ✅ onBeforeUnmount đã được override ở cuối file với cleanup auto-save

// ✅ Functions selectAnswer và nextQuestion đã được override ở cuối file với auto-save

function prevQuestion() {
  if (currentQuestionIndex.value > 0) {
    goToQuestion(currentQuestionIndex.value - 1)
  }
}

function goToQuestion(newIndex) {
  // Không cần lưu giây còn lại; deadline đã đảm bảo thời gian tiếp tục trôi
  showNextAnimation.value = true
  setTimeout(() => {
    currentQuestionIndex.value = newIndex
    if (newIndex > furthestQuestionIndex.value) {
      furthestQuestionIndex.value = newIndex
    }
    showNextAnimation.value = false
    // Nếu câu mới chưa khóa và có timer > 0 thì tiếp tục từ phần còn lại
    startTimer()
  }, 300)
}

async function submitQuiz() {
  clearInterval(timer)

  // Tính thời gian làm quiz
  const endTime = Date.now()
  const timeTaken = startTime.value ? Math.round((endTime - startTime.value) / 1000) : 0

  const token = localStorage.getItem('token')
  const answerList = Object.entries(selectedAnswers.value).map(([questionId, answerId]) => ({
    questionId: parseInt(questionId),
    answerId: parseInt(answerId),
  }))

  try {
    let resultId
    if (currentAttemptId.value) {
      // ✅ Sử dụng currentAttemptId từ resume flow
      const { quizAttemptService } = await import('@/services/quizAttemptService')
      const resp = await quizAttemptService.submitAttempt(currentAttemptId.value, answerList, timeTaken)
      resultId = resp.resultId
    } else if (attemptId) {
      // Fallback: flow cũ theo attemptId
      const { quizAttemptService } = await import('@/services/quizAttemptService')
      const resp = await quizAttemptService.submitAttempt(attemptId, answerList, timeTaken)
      resultId = resp.resultId
    } else {
      // Fallback flow cũ
      const token = localStorage.getItem('token')
      const payload = { quizId: parseInt(quizId), userId: parseInt(userId), answers: answerList, timeTaken }
      const res = await api.post('/result/submit', payload)
      resultId = res.data.resultId
      try { localStorage.setItem(`quiz_completed_${quizId}_${userId}`, '1') } catch { }
    }

    /* ✅✅ THÊM: cập nhật badge thông báo TỨC THÌ (optimistic) */
    try {
      if (typeof notificationStore.bumpUnread === 'function') {
        notificationStore.bumpUnread(1)
      } else {
        // nếu chưa có action, tăng trực tiếp
        notificationStore.unreadCount = Number(unreadCount.value || 0) + 1
      }
    } catch (e) {
      console.warn('Không thể bump unreadCount lạc quan:', e)
    }

    // ✅ bắn event để Navbar (hoặc nơi khác) có thể tải danh sách/đồng bộ
    window.dispatchEvent(new Event('quiz-submitted'))

    // ✅ đồng bộ lại số chính xác từ server (không chặn UI)
    notificationStore.loadUnreadCount?.().catch(() => { })

    // ✅ Lưu selections để Result page có thể đọc nếu BE không trả lại
    try {
      localStorage.setItem(`result_selected_${resultId}`, JSON.stringify(selectedAnswers.value || {}))
    } catch { }

    router.replace({ name: 'QuizResult', params: { resultId: String(resultId) } })
  } catch (err) {
    console.error('Lỗi khi gửi kết quả:', err)
    alert('Có lỗi xảy ra khi nộp bài. Vui lòng thử lại!')
  }
}

// ✅ RESUME QUIZ FUNCTIONS
async function createNewAttempt() {
  try {
    console.info('🆕 NEW-ATTEMPT - Tạo attempt mới cho quiz:', quizId)
    const response = await quizResumeService.createNewAttempt(quizId)

    if (response.success) {
      currentAttemptId.value = response.attemptId
      console.info('✅ NEW-ATTEMPT ok - Attempt ID:', response.attemptId)

      // Load questions cho attempt mới
      await loadQuestionsForNewAttempt()
    }
  } catch (error) {
    console.error('❌ Lỗi khi tạo attempt mới:', error)
    alert('Có lỗi xảy ra khi tạo attempt mới. Vui lòng thử lại!')
  }
}

async function loadQuestionsForNewAttempt() {
  try {
    const quizRes = await api.get(`/quiz/${quizId}`)
    quizTitle.value = quizRes.data.title || 'Quiz'
    const res = await api.get(`/question/play/${quizId}`)
    const questionList = res.data

    const enrichedQuestions = await Promise.all(
      questionList.map(async (question) => {
        try {
          const ansRes = await api.get(`/answer/${question.id}`)
          return { ...question, answers: ansRes.data || [] }
        } catch (err) {
          console.error(`Lỗi khi lấy answers cho câu hỏi ${question.id}:`, err)
          return { ...question, answers: [] }
        }
      }),
    )

    questions.value = enrichedQuestions
    isLoading.value = false
    startTimer()
    startAutoSave() // Bắt đầu auto-save cho attempt mới
  } catch (err) {
    console.error('Lỗi khi tải câu hỏi cho attempt mới:', err)
    isLoading.value = false
  }
}

// ✅ HANDLE RESUME MODAL EVENTS
function handleResume(resumeData) {
  console.info('🔄 CONTINUE - Resume attempt:', resumeData.attemptId)

  currentAttemptId.value = resumeData.attemptId

  // Khôi phục tiến độ
  currentQuestionIndex.value = resumeData.currentQuestionIndex
  furthestQuestionIndex.value = resumeData.currentQuestionIndex

  // Khôi phục đáp án đã chọn
  if (resumeData.answersJson) {
    try {
      const answers = JSON.parse(resumeData.answersJson)
      selectedAnswers.value = answers
    } catch (error) {
      console.error('Lỗi khi parse answers JSON:', error)
      selectedAnswers.value = {}
    }
  }

  // Load questions và khôi phục state
  loadQuestionsForResume(resumeData)
}

/**
 * ✅ Đóng modal resume
 */
function closeResumeModal() {
  showResumeModal.value = false
}

function handleNewAttempt() {
  console.info('🔄 RESTART - User chọn làm lại từ đầu')

  // Tạo attempt mới
  createNewAttempt()
}

async function loadQuestionsForResume(resumeData) {
  try {
    const quizRes = await api.get(`/quiz/${resumeData.quizId}`)
    quizTitle.value = quizRes.data.title || 'Quiz'
    const res = await api.get(`/question/play/${resumeData.quizId}`)
    const questionList = res.data

    const enrichedQuestions = await Promise.all(
      questionList.map(async (question) => {
        try {
          const ansRes = await api.get(`/answer/${question.id}`)
          return { ...question, answers: ansRes.data || [] }
        } catch (err) {
          console.error(`Lỗi khi lấy answers cho câu hỏi ${question.id}:`, err)
          return { ...question, answers: [] }
        }
      }),
    )

    questions.value = enrichedQuestions
    isLoading.value = false

    // Khôi phục thời gian còn lại nếu có
    if (resumeData.timeRemaining && resumeData.timeRemaining > 0) {
      countdown.value = resumeData.timeRemaining
    }

    startTimer()
    startAutoSave() // Bắt đầu auto-save cho attempt resume
  } catch (err) {
    console.error('Lỗi khi tải câu hỏi cho resume:', err)
    isLoading.value = false
  }
}

// ✅ AUTO-SAVE FUNCTIONS
function startAutoSave() {
  if (autoSaveIntervalId) {
    autoSaveService.stopAutoSave(autoSaveIntervalId)
  }

  console.info('💾 AUTOSAVE scheduled - Bắt đầu auto-save mỗi 30 giây')
  autoSaveIntervalId = autoSaveService.startAutoSave(
    quizId,
    currentAttemptId.value,
    saveProgressCallback,
    30000 // 30 giây
  )
}

function saveProgressCallback() {
  if (!currentAttemptId.value) return

  const progressData = {
    questionIndex: currentQuestionIndex.value,
    timeRemaining: countdown.value,
    answers: selectedAnswers.value
  }

  console.info('💾 AUTOSAVE sent - Lưu tiến độ:', progressData)

  // Lưu vào localStorage trước
  progressStorageService.saveProgress(
    quizId,
    currentAttemptId.value,
    currentQuestionIndex.value,
    countdown.value,
    selectedAnswers.value
  )

  // Gửi lên server
  quizResumeService.saveProgress(
    currentAttemptId.value,
    currentQuestionIndex.value,
    countdown.value,
    selectedAnswers.value
  ).then(() => {
    console.info('💾 AUTOSAVE ok - Đã lưu tiến độ thành công')
  }).catch((error) => {
    console.error('💾 AUTOSAVE error - Lỗi khi lưu tiến độ:', error)
  })
}

// ✅ ENHANCED ANSWER SELECTION WITH AUTO-SAVE
function selectAnswer(questionId, answerId) {
  // Không cho chọn nếu câu đã khóa
  if (lockedQuestionIds.value.has(questionId)) return

  const oldAnswer = selectedAnswers.value[questionId]
  selectedAnswers.value[questionId] = answerId

  // Visual feedback
  const answerElement = document.getElementById(`answer-${answerId}`)
  if (answerElement) {
    answerElement.style.transform = 'scale(0.95)'
    setTimeout(() => {
      answerElement.style.transform = 'scale(1)'
    }, 150)
  }

  // ✅ AUTO-SAVE khi chọn đáp án (debounce 2 giây)
  if (oldAnswer !== answerId) {
    console.info('💾 AUTOSAVE scheduled - Chọn đáp án mới, lưu tiến độ sau 2 giây')
    setTimeout(() => {
      if (currentAttemptId.value) {
        saveProgressCallback()
      }
    }, 2000)
  }
}

// ✅ ENHANCED NAVIGATION WITH AUTO-SAVE
function nextQuestion() {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    // Khi chuyển câu, cũng khóa câu hiện tại để tránh quay lại sửa
    if (currentQuestion.value?.id) lockedQuestionIds.value.add(currentQuestion.value.id)

    // ✅ AUTO-SAVE khi chuyển câu
    if (currentAttemptId.value) {
      console.info('💾 AUTOSAVE scheduled - Chuyển câu, lưu tiến độ sau 1 giây')
      setTimeout(() => {
        saveProgressCallback()
      }, 1000)
    }

    goToQuestion(currentQuestionIndex.value + 1)
  } else {
    clearInterval(timer)
    submitQuiz()
  }
}

// ✅ CLEANUP ON UNMOUNT
onBeforeUnmount(() => {
  clearInterval(timer)
  if (autoSaveIntervalId) {
    autoSaveService.stopAutoSave(autoSaveIntervalId)
  }
})
</script>

<template>
  <!-- (template giữ nguyên như bạn gửi) -->
  <!-- ... Toàn bộ template & style của bạn không đổi ... -->
  <!-- Mình chỉ sửa phần <script setup> như trên để badge cập nhật ngay. -->
  <div class="quiz-play-container">
    <!-- Loading State -->
    <div v-if="isLoading" class="loading-container">
      <div class="loading-spinner">
        <div class="spinner-ring"></div>
        <div class="spinner-ring"></div>
        <div class="spinner-ring"></div>
      </div>
      <h3 class="loading-text">Đang tải câu hỏi...</h3>
      <p class="loading-subtitle">Vui lòng chờ trong giây lát</p>
    </div>

    <!-- Quiz Interface -->
    <div v-else-if="questions.length > 0" class="quiz-interface">
      <!-- Header Section -->
      <div class="quiz-header">
        <div class="quiz-info">
          <h1 class="quiz-title">{{ quizTitle }}</h1>
          <div class="quiz-meta">
            <span class="question-counter">
              <i class="bi bi-question-circle"></i>
              Câu {{ currentQuestionIndex + 1 }} / {{ questions.length }}
            </span>
          </div>
        </div>

        <!-- Progress Bar -->
        <div class="progress-section">
          <div class="progress-label">Tiến độ hoàn thành</div>
          <div class="progress-bar-container">
            <div class="progress-bar-bg">
              <div class="progress-bar-fill" :style="{ width: progress + '%' }"></div>
            </div>
            <span class="progress-text">{{ Math.round(progress) }}%</span>
          </div>
        </div>
      </div>

      <!-- Timer Section: ẩn nếu không giới hạn -->
      <div class="timer-section" v-if="currentTimeLimit > 0">
        <div class="timer-container">
          <div class="timer-circle">
            <svg width="120" height="120" class="timer-svg" viewBox="0 0 120 120">
              <!-- Background Circle -->
              <circle cx="60" cy="60" r="50" fill="none" stroke="rgba(255, 255, 255, 0.2)" stroke-width="8" />
              <!-- Progress Circle -->
              <circle cx="60" cy="60" r="50" fill="none" :stroke="timeColor" stroke-width="8" stroke-linecap="round"
                stroke-dasharray="314.16" :stroke-dashoffset="314.16 - (timeProgress * 314.16) / 100"
                class="timer-progress-circle" transform="rotate(-90 60 60)" />
            </svg>
            <div class="timer-content">
              <div class="timer-number">{{ countdown }}</div>
              <div class="timer-label">giây</div>
            </div>
          </div>
          <div class="timer-warning" v-if="currentTimeLimit > 0 && countdown <= warnThreshold">
            <i class="bi bi-exclamation-triangle"></i>
            Sắp hết thời gian!
          </div>
        </div>
      </div>

      <!-- Question Card -->
      <div class="question-section" :class="{ 'fade-out': showNextAnimation }">
        <div class="question-card">
          <div class="question-header">
            <div class="question-badge">
              <i class="bi bi-lightbulb"></i>
              <span>Câu hỏi {{ currentQuestionIndex + 1 }}</span>
            </div>
          </div>

          <div class="question-content">
            <h2 class="question-text">{{ currentQuestion?.content }}</h2>
          </div>
        </div>
      </div>

      <!-- Answers Section -->
      <div class="answers-section" :class="{ 'fade-out': showNextAnimation }">
        <div class="answers-grid" v-if="currentQuestion?.answers.length">
          <div class="answer-option" v-for="(answer, index) in currentQuestion.answers" :key="answer.id" :class="{
            selected: selectedAnswers[currentQuestion.id] === answer.id,
            'option-a': index === 0,
            'option-b': index === 1,
            'option-c': index === 2,
            'option-d': index === 3,
          }" @click="!isCurrentLocked && selectAnswer(currentQuestion.id, answer.id)">
            <input type="radio" :id="`answer-${answer.id}`" :name="`question-${currentQuestion.id}`" :value="answer.id"
              :checked="selectedAnswers[currentQuestion.id] === answer.id" :disabled="isCurrentLocked"
              style="display: none" />
            <div class="answer-label">
              <span class="answer-letter">{{ String.fromCharCode(65 + index) }}</span>
            </div>
            <div class="answer-content">
              <span class="answer-text">{{ answer.content }}</span>
            </div>
            <div class="answer-indicator">
              <i class="bi bi-check-circle-fill"></i>
            </div>
          </div>
        </div>

        <div v-else class="no-answers">
          <i class="bi bi-exclamation-triangle"></i>
          <p>Không có đáp án cho câu hỏi này</p>
        </div>
      </div>

      <!-- Navigation Section -->
      <div class="navigation-section">
        <div class="nav-buttons">
          <button class="nav-btn prev-btn" :disabled="currentQuestionIndex === 0" @click="prevQuestion">
            <i class="bi bi-arrow-left"></i>
            <span>Câu trước</span>
          </button>

          <div class="question-dots">
            <div class="question-dot" v-for="(question, index) in questions" :key="question.id" :class="{
              active: index === currentQuestionIndex,
              answered: selectedAnswers[question.id],
            }" @click="() => { currentQuestionIndex = index; if (!isCurrentLocked) startTimer() }">
              {{ index + 1 }}
            </div>
          </div>

          <button class="nav-btn next-btn" v-if="currentQuestionIndex < questions.length - 1" @click="nextQuestion">
            <span>Câu tiếp</span>
            <i class="bi bi-arrow-right"></i>
          </button>

          <button class="nav-btn submit-btn" v-else @click="submitQuiz">
            <i class="bi bi-check-circle"></i>
            <span>Nộp bài</span>
          </button>
        </div>
      </div>
    </div>

    <!-- No Questions State -->
    <div v-else class="no-questions">
      <div class="no-questions-card">
        <i class="bi bi-question-octagon"></i>
        <h3>Không có câu hỏi</h3>
        <p>Quiz này hiện chưa có câu hỏi nào.</p>
        <button class="back-btn" @click="router.go(-1)">
          <i class="bi bi-arrow-left"></i>
          Quay lại
        </button>
      </div>
    </div>

    <!-- ✅ RESUME QUIZ MODAL -->
    <ResumeQuizModal v-if="showResumeModal" :quiz-id="parseInt(quizId)" :attempt-data="attemptData"
      @resume="handleResume" @new-attempt="handleNewAttempt" @close="closeResumeModal" />
  </div>
</template>

<style scoped>
/* === CONTAINER === */
.quiz-play-container {
  min-height: 100vh;
  background: var(--app-background);
  padding: 20px;
  display: flex;
  flex-direction: column;
}

/* === LOADING STATE === */
.loading-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.loading-spinner {
  position: relative;
  width: 100px;
  height: 100px;
  margin-bottom: 30px;
}

.spinner-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 4px solid transparent;
  border-top: 4px solid rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.spinner-ring:nth-child(2) {
  width: 70%;
  height: 70%;
  top: 15%;
  left: 15%;
  animation-delay: -0.3s;
}

.spinner-ring:nth-child(3) {
  width: 40%;
  height: 40%;
  top: 30%;
  left: 30%;
  animation-delay: -0.6s;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

.loading-text {
  color: white;
  font-weight: 700;
  margin-bottom: 10px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.loading-subtitle {
  color: rgba(255, 255, 255, 0.8);
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
}

/* === QUIZ INTERFACE === */
.quiz-interface {
  max-width: 1000px;
  margin: 0 auto;
  width: 100%;
}

/* === HEADER SECTION === */
.quiz-header {
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(20px);
  border: 3px solid rgba(255, 255, 255, 0.9);
  border-radius: 25px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.3);
}

.quiz-title {
  font-size: 2.5rem;
  font-weight: 900;
  color: white;
  text-shadow: 4px 4px 10px rgba(0, 0, 0, 0.8);
  margin-bottom: 15px;
  text-align: center;
  background: rgba(0, 0, 0, 0.3);
  padding: 15px;
  border-radius: 15px;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.quiz-meta {
  text-align: center;
  margin-bottom: 25px;
}

.question-counter {
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
  padding: 10px 25px;
  border-radius: 20px;
  color: white;
  font-weight: 700;
  font-size: 1.1rem;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 3px solid rgba(255, 255, 255, 0.8);
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.7);
}

.progress-section {
  text-align: center;
}

.progress-label {
  color: white;
  font-weight: 700;
  font-size: 1.1rem;
  margin-bottom: 10px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.7);
}

.progress-bar-container {
  display: flex;
  align-items: center;
  gap: 15px;
}

.progress-bar-bg {
  flex: 1;
  height: 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.progress-bar-fill {
  height: 100%;
  background: linear-gradient(45deg, #00d4ff, #00b8d4);
  border-radius: 8px;
  transition: width 0.5s ease;
  position: relative;
}

.progress-bar-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }

  100% {
    transform: translateX(100%);
  }
}

.progress-text {
  color: white;
  font-weight: 800;
  font-size: 1.2rem;
  text-shadow: 3px 3px 6px rgba(0, 0, 0, 0.8);
  background: rgba(0, 0, 0, 0.3);
  padding: 5px 10px;
  border-radius: 10px;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

/* === TIMER SECTION === */
.timer-section {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.timer-container {
  text-align: center;
}

.timer-circle {
  position: relative;
  display: inline-block;
  width: 120px;
  height: 120px;
}

.timer-svg {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
}

.timer-progress-circle {
  transition:
    stroke-dashoffset 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94),
    stroke 0.3s ease;
}

.timer-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  z-index: 10;
}

.timer-number {
  font-size: 2rem;
  font-weight: 800;
  color: white;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
  line-height: 1;
}

.timer-label {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.8);
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
}

.timer-warning {
  margin-top: 15px;
  color: #ff4757;
  font-weight: 800;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  animation: pulse 1s infinite;
  text-shadow: 2px 2px 6px rgba(0, 0, 0, 0.8);
  background: rgba(0, 0, 0, 0.5);
  padding: 10px 20px;
  border-radius: 15px;
  border: 2px solid #ff4757;
}

@keyframes pulse {

  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0.7;
  }
}

/* === QUESTION SECTION === */
.question-section {
  margin-bottom: 30px;
  transition: all 0.3s ease;
}

.question-section.fade-out {
  opacity: 0;
  transform: translateX(-20px);
}

.question-card {
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(20px);
  border: 3px solid rgba(255, 255, 255, 0.9);
  border-radius: 25px;
  padding: 30px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.3);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.question-badge,
.question-points {
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
  padding: 10px 20px;
  border-radius: 20px;
  color: white;
  font-weight: 700;
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 8px;
  border: 3px solid rgba(255, 255, 255, 0.8);
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.7);
}

.question-points {
  background: rgba(255, 215, 0, 0.3);
  border-color: #ffd700;
  color: #ffd700;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.8);
}

.question-text {
  font-size: 1.8rem;
  font-weight: 800;
  color: white;
  text-shadow: 3px 3px 8px rgba(0, 0, 0, 0.8);
  line-height: 1.4;
  text-align: center;
  background: rgba(0, 0, 0, 0.3);
  padding: 20px;
  border-radius: 15px;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

/* === ANSWERS SECTION === */
.answers-section {
  margin-bottom: 40px;
  transition: all 0.3s ease;
}

.answers-section.fade-out {
  opacity: 0;
  transform: translateX(20px);
}

.answers-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

.answer-option {
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(20px);
  border: 3px solid rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.4);
}

.answer-option:hover {
  transform: translateY(-3px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.5);
  border-color: #00d4ff;
  background: rgba(0, 0, 0, 0.7);
}

.answer-option.selected {
  border-color: #00d4ff;
  background: rgba(0, 212, 255, 0.3);
  transform: translateY(-3px);
  box-shadow: 0 15px 40px rgba(0, 212, 255, 0.4);
}

.answer-option.selected .answer-indicator {
  opacity: 1;
}

.answer-label {
  width: 55px;
  height: 55px;
  background: linear-gradient(45deg, #ff6b9d, #ff3d71);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 800;
  font-size: 1.4rem;
  flex-shrink: 0;
  box-shadow: 0 6px 20px rgba(255, 107, 157, 0.4);
  border: 3px solid rgba(255, 255, 255, 0.9);
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.answer-option.selected .answer-label {
  background: linear-gradient(45deg, #00d4ff, #00b8d4);
  box-shadow: 0 6px 20px rgba(0, 212, 255, 0.4);
  border-color: white;
}

.answer-content {
  flex: 1;
}

.answer-text {
  font-size: 1.3rem;
  font-weight: 700;
  color: white;
  text-shadow: 2px 2px 6px rgba(0, 0, 0, 0.9);
  line-height: 1.4;
}

.answer-indicator {
  width: 30px;
  height: 30px;
  color: #00d4ff;
  font-size: 1.5rem;
  opacity: 0;
  transition: all 0.3s ease;
}

.no-answers {
  text-align: center;
  padding: 60px 20px;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(20px);
  border: 3px solid rgba(255, 71, 87, 0.5);
  border-radius: 25px;
  color: white;
}

.no-answers i {
  font-size: 3rem;
  color: #ff4757;
  margin-bottom: 20px;
}

.no-answers p {
  font-size: 1.2rem;
  margin: 0;
}

/* === NAVIGATION SECTION === */
.navigation-section {
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(20px);
  border: 3px solid rgba(255, 255, 255, 0.9);
  border-radius: 25px;
  padding: 25px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.3);
}

.nav-buttons {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.nav-btn {
  background: linear-gradient(45deg, #00d4ff, #00b8d4);
  color: white;
  border: none;
  padding: 18px 30px;
  border-radius: 20px;
  font-weight: 700;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 8px 25px rgba(0, 212, 255, 0.4);
  border: 3px solid rgba(255, 255, 255, 0.8);
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.5);
}

.nav-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 35px rgba(0, 212, 255, 0.4);
  background: linear-gradient(45deg, #00b8d4, #0288d1);
}

.nav-btn:disabled {
  background: rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.5);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.submit-btn {
  background: linear-gradient(45deg, #4ecdc4, #44a08d);
  box-shadow: 0 8px 25px rgba(78, 205, 196, 0.4);
  font-size: 1.2rem;
  padding: 20px 35px;
}

.submit-btn:hover {
  background: linear-gradient(45deg, #44a08d, #4ecdc4);
  box-shadow: 0 12px 35px rgba(78, 205, 196, 0.4);
}

/* Question Dots */
.question-dots {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}

.question-dot {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(10px);
  border: 3px solid rgba(255, 255, 255, 0.8);
  color: white;
  font-weight: 700;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.7);
}

.question-dot:hover {
  background: rgba(0, 0, 0, 0.8);
  border-color: white;
  transform: scale(1.1);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}

.question-dot.active {
  background: linear-gradient(45deg, #ff6b9d, #ff3d71);
  border-color: white;
  box-shadow: 0 6px 20px rgba(255, 107, 157, 0.4);
}

.question-dot.answered {
  background: linear-gradient(45deg, #4ecdc4, #44a08d);
  border-color: white;
  box-shadow: 0 6px 20px rgba(78, 205, 196, 0.4);
}

/* === NO QUESTIONS STATE === */
.no-questions {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-questions-card {
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(20px);
  border: 3px solid rgba(255, 255, 255, 0.9);
  border-radius: 25px;
  padding: 60px 40px;
  text-align: center;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.3);
  color: white;
}

.no-questions-card i {
  font-size: 4rem;
  color: #ff6b9d;
  margin-bottom: 20px;
}

.no-questions-card h3 {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 15px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.no-questions-card p {
  font-size: 1.2rem;
  margin-bottom: 30px;
  color: rgba(255, 255, 255, 0.9);
}

.back-btn {
  background: linear-gradient(45deg, #ff6b9d, #ff3d71);
  color: white;
  border: none;
  padding: 15px 30px;
  border-radius: 20px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 8px 25px rgba(255, 107, 157, 0.3);
}

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 35px rgba(255, 107, 157, 0.4);
  background: linear-gradient(45deg, #ff3d71, #ff6b9d);
}

/* === RESPONSIVE DESIGN === */
@media (max-width: 768px) {
  .quiz-play-container {
    padding: 15px;
  }

  .quiz-header {
    padding: 20px;
  }

  .quiz-title {
    font-size: 2rem;
    font-weight: 900;
  }

  .question-text {
    font-size: 1.5rem;
    font-weight: 800;
  }

  .nav-buttons {
    flex-direction: column;
    gap: 15px;
  }

  .question-dots {
    order: -1;
  }

  .nav-btn {
    width: 100%;
    justify-content: center;
  }

  .answer-option {
    padding: 15px;
  }

  .answer-text {
    font-size: 1.1rem;
    font-weight: 700;
  }

  .progress-bar-container {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
