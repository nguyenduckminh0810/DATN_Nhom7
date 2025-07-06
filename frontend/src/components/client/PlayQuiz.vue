<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter() // ✅ Thêm dòng này

const quizId = route.params.quizId
const userId = route.params.userId
const questions = ref([])
const currentQuestionIndex = ref(0)
const token = localStorage.getItem('token')
const selectedAnswers = ref({})

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
        console.log('Questions sau khi thêm answers:', questions.value)
    } catch (err) {
        console.error('Lỗi khi tải câu hỏi:', err)
    }
})

function selectAnswer(questionId, answerId) {
    selectedAnswers.value[questionId] = answerId
}

function nextQuestion() {
    if (currentQuestionIndex.value < questions.value.length - 1) {
        currentQuestionIndex.value++
    }
}

function prevQuestion() {
    if (currentQuestionIndex.value > 0) {
        currentQuestionIndex.value--
    }
}

async function submitQuiz() {
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

        // ✅ Redirect sang trang kết quả
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
    <div class="container mt-4">
        <h1>Chơi Quiz</h1>
        <p class="text-muted">Quiz ID: {{ quizId }}</p>

        <div v-if="questions.length > 0">
            <div class="card p-4 shadow-sm">
                <h4 class="text-center question-title">
                    Câu {{ currentQuestionIndex + 1 }}: {{ questions[currentQuestionIndex].content }}
                </h4>
                <div class="d-flex flex-wrap justify-content-center gap-3 mt-3" role="group" aria-label="Answer choices"
                    v-if="questions[currentQuestionIndex].answers && questions[currentQuestionIndex].answers.length > 0">
                    <template v-for="answer in questions[currentQuestionIndex].answers" :key="answer.id">
                        <input type="radio" class="btn-check" :id="'answer-' + answer.id"
                            :name="'question-' + questions[currentQuestionIndex].id" :value="answer.id"
                            autocomplete="off"
                            :checked="selectedAnswers[questions[currentQuestionIndex].id] === answer.id"
                            @change="selectAnswer(questions[currentQuestionIndex].id, answer.id)" />
                        <label class="btn btn-gradient" :for="'answer-' + answer.id">
                            {{ answer.content }}
                        </label>
                    </template>
                </div>

                <div v-else class="text-danger text-center mt-2">
                    <p>Không có đáp án cho câu hỏi này.</p>
                </div>


                <div class="mt-4 d-flex justify-content-between">
                    <button class="btn btn-outline-secondary" :disabled="currentQuestionIndex === 0"
                        @click="prevQuestion">Quay
                        lại</button>
                    <button class="btn btn-outline-primary" v-if="currentQuestionIndex < questions.length - 1"
                        @click="nextQuestion">
                        Câu tiếp
                    </button>
                    <button class="btn btn-success" v-else @click="submitQuiz">Nộp bài</button>
                </div>
            </div>
        </div>

        <div v-else class="text-center">
            <p>Đang tải câu hỏi...</p>
        </div>
    </div>
</template>
<style scoped>
.btn-gradient {
    background: linear-gradient(to right, #6a11cb, #2575fc);
    color: white;
    border: none;
    width: 120px;
    height: 120px;
    border-radius: 12px;
    transition: transform 0.15s ease, box-shadow 0.15s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    white-space: normal;
    padding: 10px;
    word-break: break-word;
    text-wrap: wrap;
}


.btn-gradient:hover {
    color: white;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    transform: translateY(-1px);
}

.question-title {
    text-align: center;
    font-weight: 600;
    font-size: 1.5rem;
}

.btn-check:checked+.btn-gradient {
    background: linear-gradient(to right, #43e97b, #38f9d7);
    color: #000;
    font-weight: bold;
    box-shadow: 0 0 0 3px rgba(0, 255, 170, 0.5);
}
</style>
