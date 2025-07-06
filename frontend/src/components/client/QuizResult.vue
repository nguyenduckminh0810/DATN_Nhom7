<script setup>
import { useRoute, useRouter } from 'vue-router'
import { computed, ref, onMounted } from 'vue'
import { useLogin } from './useLogin'
const { username } = useLogin()
const route = useRoute()
const router = useRouter()

const quizId = route.params.quizId
const userId = route.params.userId
const score = parseInt(route.query.score) || 0

const usernamez = ref('')

onMounted(() => {
    usernamez.value = username.value || 'Người dùng không xác định'
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

function goBack() {
    router.push({
        name: 'quizcrud',
        params: {
            quizId,
            userId
        }
    })
}

</script>


<template>
    <div class="container d-flex justify-content-center align-items-center mt-5">
        <div class="card shadow p-4" style="max-width: 500px; width: 100%;">
            <div class="card-body text-center">
                <h2 class="card-title text-primary mb-4">
                    <i class="bi bi-check-circle-fill text-success me-2"></i> Kết quả Quiz
                </h2>

                <div class="my-4 d-flex justify-content-center">
                    <svg width="160" height="160" class="progress-circle">
                        <circle cx="80" cy="80" r="70" fill="none" stroke="#e6e6e6" stroke-width="15" />
                        <circle cx="80" cy="80" r="70" fill="none" :stroke="scoreColor" stroke-width="15"
                            stroke-linecap="round" :stroke-dasharray="circumference" :stroke-dashoffset="dashOffset"
                            transform="rotate(-90 80 80)" />
                        <text x="50%" y="50%" text-anchor="middle" dominant-baseline="middle" font-size="24" fill="#333"
                            font-weight="bold">
                            {{ score }}
                        </text>
                    </svg>
                </div>
                <h5 class="text-muted mb-3">👤 Người dùng: <strong>{{ username }}</strong></h5>

                <p class="card-text"><strong>Quiz ID:</strong> {{ quizId }}</p>
                <p class="card-text"><strong>User ID:</strong> {{ userId }}</p>

                <button class="btn btn-outline-primary mt-3" @click="goBack">
                    <i class="bi bi-arrow-left-circle me-2"></i> Quay về trang chính
                </button>
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
</style>
