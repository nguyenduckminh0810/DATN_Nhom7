<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const publicQuizzes = ref([])
const currentPage = ref(0)
const totalPages = ref(1)
const pageSize = 6
const isLoading = ref(true)
const error = ref('')
const hoveredQuiz = ref(null)
const router = useRouter()

async function fetchPublicQuizzes(page = 0) {
    isLoading.value = true
    try {
        const res = await axios.get(`http://localhost:8080/api/quiz/public`, {
            params: { page, size: pageSize }
        })

        publicQuizzes.value = res.data.content.map(q => ({
            quiz_id: q.id,
            title: q.title,
            fullName: q.user.fullName,
            categoryName: q.category.name,
            categoryDescription: q.category.description,
            publicQuiz: q.public
        }))

        currentPage.value = res.data.number
        totalPages.value = res.data.totalPages
    } catch (err) {
        error.value = 'Không thể tải quiz công khai.'
        console.error(err)
    } finally {
        isLoading.value = false
    }
}

onMounted(() => {
    fetchPublicQuizzes()
})

function goToPage(page) {
    if (page >= 0 && page < totalPages.value) fetchPublicQuizzes(page)
}

function playQuiz(quizId) {
    router.push({ name: 'PlayQuiz', params: { quizId } })
}

function goToQuizDetail(quizId) {
    router.push({ name: 'QuizDetail', params: { id: quizId } })
}
</script>

<template>
    <div class="container py-5">
        <h2 class="mb-4 text-center">🌐 Danh sách Quiz Công Khai</h2>

        <div v-if="isLoading" class="text-center">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Đang tải...</span>
            </div>
        </div>

        <div v-else-if="error" class="alert alert-danger text-center">
            {{ error }}
        </div>

        <div v-else class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
            <div class="col" v-for="quiz in publicQuizzes" :key="quiz.quiz_id">
                <div class="card h-100 shadow-sm quiz-card position-relative" @mouseenter="hoveredQuiz = quiz.quiz_id"
                    @mouseleave="hoveredQuiz = null">
                    <div class="card-body">
                        <h5 class="card-title text-primary">{{ quiz.title }}</h5>
                        <p class="card-text"><strong>Người tạo:</strong> {{ quiz.fullName }}</p>
                        <p class="card-text"><strong>Chủ đề:</strong> {{ quiz.categoryName }}</p>
                        <p class="card-text text-muted small">{{ quiz.categoryDescription }}</p>
                    </div>
                    <transition name="fade">
                        <div v-if="hoveredQuiz === quiz.quiz_id"
                            class="overlay d-flex justify-content-center align-items-center">
                            <div class="d-flex flex-column gap-2">
                                <button class="btn btn-sm btn-primary"
                                    @click.stop="playQuiz(quiz.quiz_id)">Chơi</button>
                                <button class="btn btn-sm btn-outline-light"
                                    @click.stop="goToQuizDetail(quiz.quiz_id)">Chi tiết</button>
                            </div>
                        </div>
                    </transition>
                </div>
            </div>
        </div>

        <div class="d-flex justify-content-center mt-4" v-if="totalPages > 1">
            <button class="btn btn-outline-secondary me-2" :disabled="currentPage === 0"
                @click="goToPage(currentPage - 1)">
                ← Trước
            </button>
            <button class="btn btn-outline-secondary" :disabled="currentPage === totalPages - 1"
                @click="goToPage(currentPage + 1)">
                Sau →
            </button>
        </div>
    </div>
</template>

<style scoped>
.dashboard {
    background: linear-gradient(135deg, #fdfbfb, #ebedee);
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

.quiz-card {
    cursor: pointer;
    transition: all 0.25s ease;
    border-radius: 16px;
    overflow: hidden;
    position: relative;
    background: white;
    border: 1px solid #e0e0e0;
}

.quiz-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.quiz-card .card-body {
    padding: 1.25rem;
}

.quiz-card .card-title {
    font-size: 1.25rem;
    margin-bottom: 0.75rem;
    color: #007bff;
}

.quiz-card .card-text {
    margin-bottom: 0.5rem;
    color: #495057;
}

.quiz-card .card-text small {
    color: #6c757d;
}

.overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.6);
    color: white;
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 10;
    transition: all 0.3s ease;
    backdrop-filter: blur(3px);
}

.overlay .btn {
    min-width: 100px;
    font-weight: 600;
    letter-spacing: 0.5px;
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

h2 {
    font-weight: 700;
    color: #343a40;
    margin-bottom: 2rem;
}
</style>
