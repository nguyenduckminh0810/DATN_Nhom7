<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useQuizCRUD } from './useQuizCRUD'
import { useLogin } from './useLogin'
import { useRouter } from 'vue-router'

// Hooks
const { toQuizCRUD, categories } = useQuizCRUD()
const { getUserId, username } = useLogin()
const router = useRouter()

// State
const title = ref('')
const selectedCategoryId = ref('')
const userId = ref('')
const isPublic = ref("true")
const userName = ref('') // Default to 'unknown' if username is not set
const quizzes = ref([])
const message = ref('')

// Lifecycle
onMounted(async () => {
    userId.value = await getUserId()
    await fetchCategories()
    await fetchQuizzes()
})

// Fetch categories
async function fetchCategories() {
    try {
        const res = await axios.get('http://localhost:8080/api/categories')
        categories.value = res.data
    } catch (error) {
        console.error('Failed to load categories:', error)
    }
}

// Fetch quizzes
async function fetchQuizzes() {
    try {
        const response = await axios.get('http://localhost:8080/api/quiz')
        quizzes.value = response.data
    } catch (error) {
        console.error('Error fetching quizzes:', error)
    }
}

// Tạo quiz
async function createQuiz() {
    try {
        await axios.post('http://localhost:8080/api/quiz/create-quiz', {
            title: title.value,
            isPublic: isPublic.value === "true",
            user: { id: userId.value },
            category: { id: selectedCategoryId.value }
        })
        message.value = 'Tạo quiz thành công!'
        title.value = ''
        selectedCategoryId.value = ''
        isPublic.value = "true"
        await fetchQuizzes()
    } catch (error) {
        console.error('Error creating quiz:', error)
        message.value = 'Tạo quiz thất bại!'
    }
}

// Chỉnh sửa quiz
// Chuyển hướng đến trang chỉnh sửa quiz
function editQuiz(quizId) {
    const quiz = quizzes.value.find(q => q.id === quizId)
    if (quiz && quiz.user) {
        const userId = quiz.user.id
        const username = quiz.user.username || 'unknown'
        router.push({
            name: 'EditQuiz',
            params: {
                userId,
                quizId
            }
        })
    }
}

// Xóa quiz
async function deleteQuiz(quizId) {
    if (confirm('Bạn có chắc chắn muốn xóa quiz này?')) {
        try {
            const response = await axios.delete(`http://localhost:8080/api/quiz/${quizId}`)

            if (response.status === 204) {
                message.value = 'Xóa quiz thành công!'
            } else {
                message.value = 'Xóa quiz thất bại: Quiz không tồn tại!'
            }

            await fetchQuizzes()
        } catch (error) {
            if (error.response && error.response.status === 404) {
                message.value = 'Quiz không tồn tại!'
            } else {
                console.error('Lỗi khi xóa quiz:', error)
                message.value = 'Xóa quiz thất bại!'
            }
        }
    }
}
function playQuiz(quizId) {
    console.log(userId.value)
    router.push({ name: 'PlayQuiz', params: { quizId, userId: userId.value } })
}
</script>

<template>
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <h1 class="text-center mb-4">Quiz Management</h1>

                <!-- Tạo mới Quiz -->
                <div class="card mb-4 shadow-sm">
                    <div class="card-header bg-primary text-white fw-semibold">Thêm mới Quiz</div>
                    <div class="card-body">
                        <form @submit.prevent="createQuiz">
                            <div class="mb-3">
                                <label class="form-label">Chọn loại quiz:</label>
                                <select v-model="selectedCategoryId" class="form-select" required>
                                    <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                                        {{ cat.name }} - {{ cat.description }}
                                    </option>
                                </select>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Tên của quiz:</label>
                                <input type="text" v-model="title" class="form-control" placeholder="Nhập tên quiz"
                                    required />
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Công khai quiz?</label>
                                <div>
                                    <div class="form-check form-check-inline">
                                        <input class="form-check-input" type="radio" value="true" v-model="isPublic"
                                            id="publicYes" />
                                        <label class="form-check-label" for="publicYes">Có</label>
                                    </div>
                                    <div class="form-check form-check-inline">
                                        <input class="form-check-input" type="radio" value="false" v-model="isPublic"
                                            id="publicNo" />
                                        <label class="form-check-label" for="publicNo">Không</label>
                                    </div>
                                </div>
                            </div>
                            <button type="submit" class="btn btn-success">
                                <i class="bi bi-plus-circle me-1"></i> Tạo Quiz
                            </button>
                        </form>
                    </div>
                </div>

                <!-- Danh sách Quiz -->
                <div class="card mb-4">
                    <div class="card-header">Danh sách Quiz</div>
                    <ul class="list-group list-group-flush">
                        <li v-for="quiz in quizzes" :key="quiz.id"
                            class="list-group-item d-flex justify-content-between align-items-center">
                            {{ quiz.title }}
                            <div>
                                <button class="btn btn-secondary btn-sm me-2" @click="editQuiz(quiz.id)">Edit</button>
                                <button class="btn btn-danger btn-sm me-2" @click="deleteQuiz(quiz.id)">Delete</button>
                                <button class="btn btn-primary btn-sm" @click="playQuiz(quiz.id)">Play</button>
                            </div>
                        </li>
                    </ul>
                </div>

                <!-- Thông báo -->
                <div v-if="message" class="alert alert-info mt-4">
                    {{ message }}
                </div>
            </div>
        </div>
    </div>
</template>
