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
const isPublic = ref(true)
const userName = ref('')
const quizzes = ref([])
const message = ref('')
const selectedImage = ref(null)

const previewUrl = ref(null)

function handleImageUpload(event) {
    const file = event.target.files[0]
    if (file) {
        selectedImage.value = file
        previewUrl.value = URL.createObjectURL(file)
    }
}


function getQuizImageUrl(quizId) {
    return `http://localhost:8080/api/image/quiz/${quizId}`
}

// Lifecycle
onMounted(async () => {
    userId.value = await getUserId()
    await fetchCategories()
    await fetchQuizzes()
})
function clearImage() {
    selectedImage.value = null
    previewUrl.value = null
}

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
        const formData = new FormData()

        const quizData = {
            title: title.value,
            public: isPublic.value,
            user: { id: userId.value },
            category: { id: selectedCategoryId.value }
        }

        formData.append('quiz', JSON.stringify(quizData))

        if (selectedImage.value) {
            formData.append('image', selectedImage.value)
        }

        await axios.post('http://localhost:8080/api/quiz/create-quiz-with-image', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        })

        message.value = 'Tạo quiz thành công!'
        title.value = ''
        selectedCategoryId.value = ''
        isPublic.value = true
        selectedImage.value = null

        await fetchQuizzes()
    } catch (error) {
        console.error('Lỗi khi tạo quiz:', error)
        message.value = 'Tạo quiz thất bại!'
    }
}

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
    router.push({ name: 'PlayQuiz', params: { quizId, userId: userId.value } })
}
</script>

<template>
    <div class="content-container">
        <div class="container py-4">
            <div class="row justify-content-center">
                <div class="col-lg-8">

                    <!-- Tiêu đề -->
                    <h2 class="text-center mb-4 fw-bold text-primary">Quản lý Quiz</h2>

                    <!-- Form tạo quiz -->
                    <div class="card shadow-sm mb-5">
                        <div class="card-header bg-success text-white">Tạo Quiz mới</div>
                        <div class="card-body">
                            <form @submit.prevent="createQuiz">
                                <!-- Upload ảnh -->
                                <div class="mb-3">
                                    <label class="form-label fw-semibold">Ảnh đại diện:</label>
                                    <input type="file" class="form-control" @change="handleImageUpload"
                                        accept="image/*" />
                                    <div v-if="previewUrl" class="mt-3 text-center">
                                        <img :src="previewUrl" alt="Preview" class="img-thumbnail"
                                            style="max-height: 160px;" />
                                        <button type="button" class="btn btn-sm btn-outline-danger mt-2"
                                            @click="clearImage">
                                            <i class="bi bi-x-circle"></i> Xóa ảnh
                                        </button>
                                    </div>
                                </div>

                                <!-- Danh mục -->
                                <div class="mb-3">
                                    <label class="form-label fw-semibold">Chọn danh mục:</label>
                                    <select v-model="selectedCategoryId" class="form-select" required>
                                        <option value="" disabled>-- Chọn loại quiz --</option>
                                        <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                                            {{ cat.name }} - {{ cat.description }}
                                        </option>
                                    </select>
                                </div>

                                <!-- Tên quiz -->
                                <div class="mb-3">
                                    <label class="form-label fw-semibold">Tên quiz:</label>
                                    <input type="text" v-model="title" class="form-control"
                                        placeholder="VD: Lịch sử Việt Nam" required />
                                </div>

                                <!-- Công khai -->
                                <div class="mb-3">
                                    <label class="form-label fw-semibold">Quiz công khai?</label>
                                    <div>
                                        <div class="form-check form-check-inline">
                                            <input class="form-check-input" type="radio" :value="true"
                                                v-model="isPublic" id="publicYes" />
                                            <label class="form-check-label" for="publicYes">Có</label>
                                        </div>
                                        <div class="form-check form-check-inline">
                                            <input class="form-check-input" type="radio" :value="false"
                                                v-model="isPublic" id="publicNo" />
                                            <label class="form-check-label" for="publicNo">Không</label>
                                        </div>
                                    </div>
                                </div>

                                <!-- Nút tạo -->
                                <div class="text-end">
                                    <button type="submit" class="btn btn-success">
                                        <i class="bi bi-plus-circle me-1"></i> Tạo Quiz
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Danh sách Quiz -->
                    <div class="card shadow-sm">
                        <div class="card-header bg-secondary text-white">Danh sách Quiz đã tạo</div>
                        <ul class="list-group list-group-flush">
                            <li v-for="quiz in quizzes" :key="quiz.id"
                                class="list-group-item d-flex align-items-center justify-content-between flex-wrap">
                                <div class="d-flex align-items-center">
                                    <img :src="getQuizImageUrl(quiz.id)" alt="Quiz Image" class="rounded-circle me-3"
                                        width="60" height="60" style="object-fit: cover;" />
                                    <strong>{{ quiz.title }}</strong>
                                </div>
                                <div class="btn-group mt-2 mt-md-0">
                                    <button class="btn btn-sm btn-outline-primary" @click="editQuiz(quiz.id)">
                                        <i class="bi bi-pencil-square"></i> Sửa
                                    </button>
                                    <button class="btn btn-sm btn-outline-danger" @click="deleteQuiz(quiz.id)">
                                        <i class="bi bi-trash"></i> Xóa
                                    </button>
                                    <button class="btn btn-sm btn-outline-success" @click="playQuiz(quiz.id)">
                                        <i class="bi bi-play-circle"></i> Chơi
                                    </button>
                                </div>
                            </li>
                        </ul>
                    </div>

                    <!-- Thông báo -->
                    <div v-if="message" class="alert alert-info mt-4 text-center">
                        {{ message }}
                    </div>

                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* Ảnh đại diện quiz */
img.rounded-circle {
    object-fit: cover;
    border: 3px solid #fff;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
    transition: transform 0.3s ease;
}

img.rounded-circle:hover {
    transform: scale(1.05);
}

/* Hiệu ứng Gradient cho card */
.card {
    border-radius: 15px;
    border: none;
    background: linear-gradient(135deg, #ffffff, #f1f9ff);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

/* Header với gradient */
.card-header {
    font-weight: 600;
    font-size: 1.05rem;
    background: linear-gradient(90deg, #00b09b, #96c93d) !important;
    color: #fff !important;
    border-top-left-radius: 15px;
    border-top-right-radius: 15px;
}

/* Form Body Gradient */
.card-body {
    background: linear-gradient(to right, #fdfbfb, #ebedee);
    border-top: 1px solid #eee;
}

/* Tiêu đề */
h2 {
    background: linear-gradient(90deg, #007cf0, #00dfd8);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

/* Label */
.form-label {
    font-size: 0.95rem;
    font-weight: 600;
    color: #343a40;
}

/* Input, select */
.form-control,
.form-select {
    border-radius: 8px;
    border: 1px solid #ced4da;
    transition: border-color 0.3s;
}

.form-control:focus,
.form-select:focus {
    border-color: #00b09b;
    box-shadow: 0 0 0 0.2rem rgba(0, 176, 155, 0.25);
}

/* Nút chính */
.btn-success {
    background: linear-gradient(to right, #00c9ff, #92fe9d);
    border: none;
    color: #000;
    font-weight: 600;
}

.btn-success:hover {
    background: linear-gradient(to right, #00b4db, #00d2ff);
}

/* Các nút nhỏ */
.btn-outline-primary,
.btn-outline-danger,
.btn-outline-success {
    font-weight: 500;
    border-radius: 6px;
    border-width: 2px;
}

/* Danh sách quiz */
.list-group-item {
    transition: background-color 0.3s ease;
    border: none;
    border-bottom: 1px solid #eee;
    background: linear-gradient(to right, #f9f9f9, #e9f0f5);
}

.list-group-item:hover {
    background: linear-gradient(to right, #e3ffe7, #d9e7ff);
}

/* Nhóm nút trong danh sách */
.btn-group>.btn {
    margin-left: 0.5rem;
}

/* Căn giữa ảnh & nội dung quiz */
.list-group-item .d-flex.align-items-center {
    flex: 1;
    min-width: 200px;
}

/* Thông báo */
.alert-info {
    background: linear-gradient(90deg, #c9ffbf, #ffafbd);
    color: #333;
    font-weight: 500;
    border-radius: 8px;
}

/* Responsive */
@media (max-width: 768px) {
    .btn-group {
        flex-direction: column;
        gap: 0.5rem;
        width: 100%;
    }
}
</style>