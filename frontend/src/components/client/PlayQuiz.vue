<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const quizId = route.params.quizId
const userId = route.params.userId
const questions = ref([])
const currentQuestionIndex = ref(0)
const selectedAnswers = ref({})
const countdown = ref(30)
const isLoading = ref(true)
const showNextAnimation = ref(false)
const quizTitle = ref('')
let timer = null

// Computed properties
const currentQuestion = computed(() => questions.value[currentQuestionIndex.value])
const progress = computed(() => ((currentQuestionIndex.value + 1) / questions.value.length) * 100)
const timeProgress = computed(() => (countdown.value / 30) * 100)
const timeColor = computed(() => {
    if (countdown.value > 20) return '#4ecdc4'
    if (countdown.value > 10) return '#ffd700'
    return '#ff4757'
})

function startTimer() {
    clearInterval(timer)
    countdown.value = 30
    timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
            clearInterval(timer)
            nextQuestion()
        }
    }, 1000)
}

onMounted(async () => {
    try {
        // Get quiz info
        const quizRes = await axios.get(`http://localhost:8080/api/quiz/${quizId}`)
        quizTitle.value = quizRes.data.title || 'Quiz'

        // Get questions
        const res = await axios.get(`http://localhost:8080/api/question/${quizId}`)
        const questionList = res.data

        const enrichedQuestions = await Promise.all(
            questionList.map(async (question) => {
                try {
                    const ansRes = await axios.get(`http://localhost:8080/api/answer/${question.id}`)
                    return { ...question, answers: ansRes.data || [] }
                } catch (err) {
                    console.error(`Lỗi khi lấy answers cho câu hỏi ID ${question.id}:`, err)
                    return { ...question, answers: [] }
                }
            })
        )

        questions.value = enrichedQuestions
        isLoading.value = false
        startTimer()
    } catch (err) {
        console.error('Lỗi khi tải câu hỏi:', err)
        isLoading.value = false
    }
})

onBeforeUnmount(() => {
    clearInterval(timer)
})

function selectAnswer(questionId, answerId) {
    selectedAnswers.value[questionId] = answerId
    
    // Visual feedback
    const answerElement = document.getElementById(`answer-${answerId}`)
    if (answerElement) {
        answerElement.style.transform = 'scale(0.95)'
        setTimeout(() => {
            answerElement.style.transform = 'scale(1)'
        }, 150)
    }
}

function nextQuestion() {
    if (currentQuestionIndex.value < questions.value.length - 1) {
        showNextAnimation.value = true
        setTimeout(() => {
            currentQuestionIndex.value++
            showNextAnimation.value = false
            startTimer()
        }, 300)
    } else {
        clearInterval(timer)
        submitQuiz()
    }
}

function prevQuestion() {
    if (currentQuestionIndex.value > 0) {
        showNextAnimation.value = true
        setTimeout(() => {
            currentQuestionIndex.value--
            showNextAnimation.value = false
            startTimer()
        }, 300)
    }
}

async function submitQuiz() {
    clearInterval(timer)

    const token = localStorage.getItem('token')
    const answerList = Object.entries(selectedAnswers.value).map(
        ([questionId, answerId]) => ({
            questionId: parseInt(questionId),
            answerId: parseInt(answerId)
        })
    )

    const payload = {
        quizId: parseInt(quizId),
        userId: parseInt(userId),
        answers: answerList
    }

    try {
        const res = await axios.post(
            'http://localhost:8080/api/result/submit',
            payload,
            {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            }
        )

        localStorage.setItem('correctAnswers', JSON.stringify(res.data.correctAnswers))
        localStorage.setItem('selectedAnswers', JSON.stringify(answerList))

        router.push({
            name: 'QuizResult',
            params: { quizId, userId },
            query: { score: res.data.score }
        })
    } catch (err) {
        console.error('Lỗi khi gửi kết quả:', err)
        alert('Có lỗi xảy ra khi nộp bài. Vui lòng thử lại!')
    }
}
</script>

<template>
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

            <!-- Timer Section -->
            <div class="timer-section">
                <div class="timer-container">
                    <div class="timer-circle">
                        <svg width="120" height="120" class="timer-svg" viewBox="0 0 120 120">
                            <!-- Background Circle -->
                            <circle 
                                cx="60" 
                                cy="60" 
                                r="50" 
                                fill="none"
                                stroke="rgba(255, 255, 255, 0.2)"
                                stroke-width="8"
                            />
                            <!-- Progress Circle -->
                            <circle 
                                cx="60" 
                                cy="60" 
                                r="50" 
                                fill="none"
                                :stroke="timeColor"
                                stroke-width="8"
                                stroke-linecap="round"
                                stroke-dasharray="314.16"
                                :stroke-dashoffset="314.16 - (timeProgress * 314.16 / 100)"
                                class="timer-progress-circle"
                                transform="rotate(-90 60 60)"
                            />
                        </svg>
                        <div class="timer-content">
                            <div class="timer-number">{{ countdown }}</div>
                            <div class="timer-label">giây</div>
                        </div>
                    </div>
                    <div class="timer-warning" v-if="countdown <= 10">
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
                        <div class="question-points">
                            <i class="bi bi-star-fill"></i>
                            <span>{{ currentQuestion?.point || 1 }} điểm</span>
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
                    <div 
                        class="answer-option" 
                        v-for="(answer, index) in currentQuestion.answers" 
                        :key="answer.id"
                        :class="{ 
                            'selected': selectedAnswers[currentQuestion.id] === answer.id,
                            'option-a': index === 0,
                            'option-b': index === 1,
                            'option-c': index === 2,
                            'option-d': index === 3
                        }"
                        @click="selectAnswer(currentQuestion.id, answer.id)"
                    >
                        <input 
                            type="radio" 
                            :id="`answer-${answer.id}`"
                            :name="`question-${currentQuestion.id}`" 
                            :value="answer.id"
                            :checked="selectedAnswers[currentQuestion.id] === answer.id"
                            style="display: none;"
                        />
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
                    <button 
                        class="nav-btn prev-btn" 
                        :disabled="currentQuestionIndex === 0"
                        @click="prevQuestion"
                    >
                        <i class="bi bi-arrow-left"></i>
                        <span>Câu trước</span>
                    </button>

                    <div class="question-dots">
                        <div 
                            class="question-dot" 
                            v-for="(question, index) in questions" 
                            :key="question.id"
                            :class="{ 
                                'active': index === currentQuestionIndex,
                                'answered': selectedAnswers[question.id]
                            }"
                            @click="currentQuestionIndex = index; startTimer()"
                        >
                            {{ index + 1 }}
                        </div>
                    </div>

                    <button 
                        class="nav-btn next-btn" 
                        v-if="currentQuestionIndex < questions.length - 1"
                        @click="nextQuestion"
                    >
                        <span>Câu tiếp</span>
                        <i class="bi bi-arrow-right"></i>
                    </button>

                    <button 
                        class="nav-btn submit-btn" 
                        v-else
                        @click="submitQuiz"
                    >
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
    </div>
</template>

<style scoped>
/* === CONTAINER === */
.quiz-play-container {
    min-height: 100vh;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
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
    0% { transform: translateX(-100%); }
    100% { transform: translateX(100%); }
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
    transition: stroke-dashoffset 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94), stroke 0.3s ease;
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
    0%, 100% { opacity: 1; }
    50% { opacity: 0.7; }
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

.question-badge, .question-points {
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