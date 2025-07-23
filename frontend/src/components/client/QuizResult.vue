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
onMounted(() => {
    correctAnswers.value = JSON.parse(localStorage.getItem('correctAnswers') || '[]')
    selectedAnswers.value = JSON.parse(localStorage.getItem('selectedAnswers') || '[]')

    // Sau khi dùng xong thì xóa
    localStorage.removeItem('correctAnswers')
    localStorage.removeItem('selectedAnswers')
})

const radius = 70
const circumference = 2 * Math.PI * radius

const dashOffset = computed(() => {
    const percent = Math.min(score, 100)
    return circumference - (percent / 100) * circumference
})

const scoreColor = computed(() => {
    if (score >= 80) return '#28a745' // green
    if (score >= 50) return '#ffc107' // yellow
    return '#dc3545' // red
})

const combinedResults = computed(() => {
    return correctAnswers.value.map(ca => {
        const userAns = selectedAnswers.value.find(sa => sa.questionId === ca.questionId)
        return {
            questionId: ca.questionId,
            correctAnswerId: ca.correctAnswerId,
            selectedAnswerId: userAns?.answerId ?? null,
            isCorrect: userAns?.answerId === ca.correctAnswerId
        }
    })
})

function goBack() {
    router.push({
        name: 'QuizCRUD',
        params: { quizId, userId }
    })
}
</script>

<template>
    <div class="content-container">
        <div class="container d-flex justify-content-center align-items-center mt-5">
            <div class="card shadow p-4" style="max-width: 500px; width: 100%;">
                <div class="card-body text-center">
                    <h2 class="card-title text-primary mb-4">
                        <i class="bi bi-check-circle-fill text-success me-2"></i> Kết quả Quiz
                    </h2>

                    <!-- Vòng tròn hiển thị điểm -->
                    <div class="my-4 d-flex justify-content-center">
                        <svg width="160" height="160" class="progress-circle">
                            <circle cx="80" cy="80" r="70" fill="none" stroke="#e6e6e6" stroke-width="15" />
                            <circle cx="80" cy="80" r="70" fill="none" :stroke="scoreColor" stroke-width="15"
                                stroke-linecap="round" :stroke-dasharray="circumference" :stroke-dashoffset="dashOffset"
                                transform="rotate(-90 80 80)" />
                            <text x="50%" y="50%" text-anchor="middle" dominant-baseline="middle" font-size="24"
                                fill="#333" font-weight="bold">
                                {{ score }}
                            </text>
                        </svg>
                    </div>

                    <h5 class="text-muted mb-3">👤 Người dùng: <strong>{{ username || 'Không xác định' }}</strong></h5>
                    <p class="card-text"><strong>Quiz ID:</strong> {{ quizId }}</p>
                    <p class="card-text"><strong>User ID:</strong> {{ userId }}</p>

                    <!-- Chi tiết kết quả -->
                    <div class="mt-4 text-start">
                        <h5 class="text-center text-secondary mb-3">🔍 Chi tiết kết quả</h5>
                        <ul class="list-group">
                            <li v-for="item in combinedResults" :key="item.questionId"
                                class="list-group-item d-flex justify-content-between align-items-center"
                                :class="item.isCorrect ? 'list-group-item-success' : 'list-group-item-danger'">
                                <div><strong>Câu {{ item.questionId }}</strong></div>
                                <div>
                                    <span>Chọn: <strong>{{ item.selectedAnswerId ?? 'Không chọn'
                                    }}</strong></span><br />
                                    <span>Đúng: <strong>{{ item.correctAnswerId }}</strong></span>
                                </div>
                            </li>
                        </ul>
                    </div>

                    <button class="btn btn-outline-primary mt-4" @click="goBack">
                        <i class="bi bi-arrow-left-circle me-2"></i> Quay về trang chính
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.card {
    border-radius: 16px;
}

.progress-circle {
    display: block;
    margin: 0 auto;
}

.list-group-item {
    font-size: 14px;
}
</style>
