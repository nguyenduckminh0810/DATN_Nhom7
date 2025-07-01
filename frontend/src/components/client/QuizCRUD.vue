<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useQuizCRUD } from './useQuizCRUD'
import { useLogin } from './useLogin'

// Sử dụng useQuizCRUD
const {
    toQuizCRUD,
    categories
} = useQuizCRUD()
const { getUserId, token } = useLogin()

// Danh mục
// const categories = ref([])
const title = ref('')
const selectedCategoryId = ref('')
const userId = ref('')
// Public status
const isPublic = ref("true")
onMounted(async () => {
    const user_id = await getUserId()
    userId.value = user_id
    try {
        const res = await axios.get('http://localhost:8080/api/categories')
        categories.value = res.data
    } catch (error) {
        console.error('Failed to load categories:', error)
    }
})


// Tạo quiz

const createQuiz = async () => {
    try {
        const response = await await axios.post('http://localhost:8080/api/quiz/create-quiz', {
            title: title.value,
            isPublic: isPublic.value === "true",
            user: { id: userId.value },
            category: { id: selectedCategoryId.value }
        });
    } catch (error) {
        console.error('Error creating quiz:', error);
    }
}

</script>

<template>
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <h1 class="text-center mb-4">Quiz Management</h1>
                <h1>{{ userId }}</h1>
                <!-- Thêm mới quiz -->
                <div class="card mb-4 shadow-sm">
                    <div class="card-header bg-primary text-white fw-semibold">Thêm mới Quiz</div>
                    <div class="card-body">
                        <form @submit.prevent="createQuiz">
                            <div class="mb-3">
                                <label for="category" class="form-label">Chọn loại quiz:</label>
                                <select id="category" v-model="selectedCategoryId" class="form-select" required>
                                    <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                                        {{ cat.name }} - {{ cat.description }}
                                    </option>
                                </select>
                            </div>
                            <div class="mb-3">
                                <label for="title" class="form-label">Tên của quiz:</label>
                                <input type="text" id="title" v-model="title" class="form-control"
                                    placeholder="Nhập tên quiz" required />
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Công khai quiz?</label>
                                <div>
                                    <div class="form-check form-check-inline">
                                        <input class="form-check-input" type="radio" id="publicYes" value="true"
                                            v-model="isPublic" />
                                        <label class="form-check-label" for="publicYes">Có</label>
                                    </div>
                                    <div class="form-check form-check-inline">
                                        <input class="form-check-input" type="radio" id="publicNo" value="false"
                                            v-model="isPublic" />
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

                <!-- Danh sách quiz -->
                <div class="card mb-4">
                    <div class="card-header">Quizzes</div>
                    <ul class="list-group list-group-flush">
                        <li v-for="quiz in quizzes" :key="quiz.id"
                            class="list-group-item d-flex justify-content-between align-items-center">
                            {{ quiz.title }}
                            <div>
                                <button class="btn btn-secondary btn-sm me-2" @click="editQuiz(quiz.id)">Edit</button>
                                <button class="btn btn-danger btn-sm" @click="deleteQuiz(quiz.id)">Delete</button>
                            </div>
                        </li>
                    </ul>
                </div>

                <!-- Chỉnh sửa quiz -->
                <div v-if="selectedQuiz" class="card mb-4">
                    <div class="card-header">Edit Quiz</div>
                    <div class="card-body">
                        <form @submit.prevent="updateQuiz">
                            <div class="mb-3">
                                <label for="editQuizTitle" class="form-label">Quiz Title</label>
                                <input type="text" id="editQuizTitle" v-model="selectedQuiz.title" class="form-control"
                                    required />
                            </div>
                            <button type="submit" class="btn btn-primary">Update Quiz</button>
                        </form>
                    </div>
                </div>

                <!-- Thông báo -->
                <div v-if="message" class="alert alert-info mt-4">
                    {{ message }}
                </div>
            </div>
        </div>
    </div>
</template>
