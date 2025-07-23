<script setup>
import { useRouter } from 'vue-router'
import { useLogin } from './useLogin'
import { useQuizCRUD } from './useQuizCRUD'
import ListQuizPublic from './ListQuizPublic.vue'
import ListUserQuiz from './ListUserQuiz.vue'

const router = useRouter()
const { username, userId, logout, getUserId } = useLogin()
const { toQuizCRUD } = useQuizCRUD()

function toQuizHistory() {
    if (!userId.value) {
        return getUserId().then(() => {
            if (userId.value) {
                router.push({ name: 'QuizHistory', params: { userId: userId.value } })
            } else {
                alert('Không thể lấy thông tin người dùng.')
            }
        })
    }
    router.push({ name: 'QuizHistory', params: { userId: userId.value } })
}

function logoutForClientDashboard() {
    logout()
    router.push('/login')
}
</script>

<template>
    <div class="content-container">
        <div class="dashboard container py-5">
            <div class="text-center mb-5">
                <h1 class="display-4 fw-bold">🎉 Chào mừng, {{ username }}!</h1>
                <p class="lead text-muted">Chọn một hành động để bắt đầu quản lý và theo dõi quiz của bạn.</p>
            </div>

            <div class="d-flex justify-content-center gap-4 flex-wrap mb-4">
                <button class="btn btn-lg btn-outline-primary shadow" @click="toQuizCRUD">
                    🛠️ Quản lý Quiz
                </button>
                <button class="btn btn-lg btn-outline-secondary shadow" @click="toQuizHistory">
                    📜 Lịch sử làm Quiz
                </button>
                <button class="btn btn-lg btn-outline-danger shadow" @click="logoutForClientDashboard">
                    🚪 Đăng xuất
                </button>
            </div>
        </div>

        <div class="container py-4">
            <ListUserQuiz />
            <ListQuizPublic />
        </div>
    </div>
</template>

<style scoped>
.dashboard {
    background: linear-gradient(135deg, #d88080, #d8e2e7);
    border-radius: 16px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
    padding: 2rem;
}

button {
    min-width: 200px;
    padding: 1rem 1.5rem;
    border-radius: 12px;
    font-size: 1.1rem;
    font-weight: 500;
    transition: all 0.2s ease-in-out;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

button:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
}

h2 {
    font-weight: 700;
    color: #343a40;
    margin-bottom: 2rem;
}
</style>
