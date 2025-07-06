<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
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
let timer = null

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
        const res = await axios.get(`http://localhost:8080/api/question/${quizId}`)
        const questionList = res.data
        console.log('Danh sách câu hỏi:', questionList)

        const enrichedQuestions = await Promise.all(
            questionList.map(async (question) => {
                try {
                    const ansRes = await axios.get(`http://localhost:8080/api/answer/${question.id}`)
                    console.log(`Đáp án cho câu hỏi ${question.id}:`, ansRes.data)
                    return { ...question, answers: ansRes.data || [] }
                } catch (err) {
                    console.error(`Lỗi khi lấy answers cho câu hỏi ID ${question.id}:`, err)
                    return { ...question, answers: [] }
                }
            })
        )

        questions.value = enrichedQuestions
        startTimer() // Bắt đầu đếm thời gian khi có câu hỏi
    } catch (err) {
        console.error('Lỗi khi tải câu hỏi:', err)
    }
})

onBeforeUnmount(() => {
    clearInterval(timer)
})

function selectAnswer(questionId, answerId) {
    selectedAnswers.value[questionId] = answerId
}

function nextQuestion() {
    if (currentQuestionIndex.value < questions.value.length - 1) {
        currentQuestionIndex.value++
        startTimer()
    } else {
        clearInterval(timer)
        submitQuiz()
    }
}

function prevQuestion() {
    if (currentQuestionIndex.value > 0) {
        currentQuestionIndex.value--
        startTimer()
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

        const score = res.data.score
        alert('Nộp bài thành công! Điểm của bạn: ' + score)

        router.push({
            name: 'QuizResult',
            params: {
                quizId,
                userId
            },
            query: {
                score
            }
        })
    } catch (err) {
        console.error('Lỗi khi gửi kết quả:', err)
        if (err.response) {
            alert('Lỗi: ' + err.response.status + ' - ' + (err.response.data.message || 'Không rõ nguyên nhân.'))
        } else {
            alert('Đã có lỗi xảy ra khi nộp bài.')
        }
    }
}
</script>
<template>
    <div class="container my-5">
        <div class="text-center mb-4">
            <h1 class="fw-bold text-primary">🧠 Chơi Quiz</h1>
            <p class="text-muted">Mã Quiz: <strong>{{ quizId }}</strong></p>
        </div>

        <div v-if="questions.length > 0">
            <div class="card shadow-sm p-4">
                <!-- Thời gian -->
                <div class="mb-4">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <small class="text-muted">⏳ Thời gian còn lại:</small>
                        <small class="fw-bold text-dark">{{ countdown }} giây</small>
                    </div>
                    <div class="progress" style="height: 20px;">
                        <div class="progress-bar progress-bar-striped progress-bar-animated" :class="{
                            'bg-success': countdown > 20,
                            'bg-warning': countdown <= 20 && countdown > 10,
                            'bg-danger': countdown <= 10
                        }" role="progressbar" :style="{ width: (countdown / 30 * 100) + '%' }"
                            :aria-valuenow="countdown" aria-valuemin="0" aria-valuemax="30">
                        </div>
                    </div>
                </div>

                <!-- Câu hỏi -->
                <h4 class="question-title text-center mb-4">
                    Câu {{ currentQuestionIndex + 1 }}: {{ questions[currentQuestionIndex].content }}
                </h4>

                <!-- Đáp án -->
                <div class="row row-cols-2 g-3 justify-content-center"
                    v-if="questions[currentQuestionIndex].answers?.length">
                    <div class="col-auto" v-for="answer in questions[currentQuestionIndex].answers" :key="answer.id">
                        <input type="radio" class="btn-check" :id="'answer-' + answer.id"
                            :name="'question-' + questions[currentQuestionIndex].id" :value="answer.id"
                            autocomplete="off"
                            :checked="selectedAnswers[questions[currentQuestionIndex].id] === answer.id"
                            @change="selectAnswer(questions[currentQuestionIndex].id, answer.id)" />
                        <label class="btn btn-gradient w-100 text-wrap" :for="'answer-' + answer.id">
                            {{ answer.content }}
                        </label>
                    </div>
                </div>

                <!-- Không có đáp án -->
                <div v-else class="text-danger text-center mt-3">
                    <p>❗ Không có đáp án cho câu hỏi này.</p>
                </div>

                <!-- Điều hướng -->
                <div class="mt-5 d-flex justify-content-between">
                    <button class="btn btn-outline-secondary" :disabled="currentQuestionIndex === 0"
                        @click="prevQuestion">
                        ⬅️ Quay lại
                    </button>
                    <button class="btn btn-outline-primary" v-if="currentQuestionIndex < questions.length - 1"
                        @click="nextQuestion">
                        Câu tiếp ➡️
                    </button>
                    <button class="btn btn-success" v-else @click="submitQuiz">
                        ✅ Nộp bài
                    </button>
                </div>
            </div>
        </div>

        <div v-else class="text-center">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Đang tải...</span>
            </div>
            <p class="mt-3">Đang tải câu hỏi...</p>
        </div>
    </div>
</template>

<style scoped>
.question-title {
    font-weight: 600;
    font-size: 1.5rem;
}

.btn-gradient {
    background: linear-gradient(to right, #6a11cb, #2575fc);
    color: white;
    border: none;
    min-width: 140px;
    min-height: 100px;
    border-radius: 12px;
    transition: transform 0.15s ease, box-shadow 0.15s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 10px;
    text-align: center;
    white-space: normal;
    word-break: break-word;
    font-weight: 500;
}

.btn-gradient:hover {
    color: white;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    transform: translateY(-2px);
}

.btn-check:checked+.btn-gradient {
    background: linear-gradient(to right, #43e97b, #38f9d7);
    color: #000;
    font-weight: bold;
    box-shadow: 0 0 0 3px rgba(0, 255, 170, 0.5);
}
</style>
