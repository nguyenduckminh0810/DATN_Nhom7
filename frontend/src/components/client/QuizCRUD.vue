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
const isLoading = ref(false)
const isCreating = ref(false)
const loadingQuizzes = ref(true)

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
    isLoading.value = true
    try {
    userId.value = await getUserId()
        await Promise.all([fetchCategories(), fetchQuizzes()])
    } finally {
        isLoading.value = false
        loadingQuizzes.value = false
    }
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
    if (isCreating.value) return
    
    isCreating.value = true
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
        previewUrl.value = null

        await fetchQuizzes()
        
        // Clear message after 3 seconds
        setTimeout(() => {
            message.value = ''
        }, 3000)
    } catch (error) {
        console.error('Lỗi khi tạo quiz:', error)
        message.value = 'Tạo quiz thất bại!'
        setTimeout(() => {
            message.value = ''
        }, 3000)
    } finally {
        isCreating.value = false
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
            
            setTimeout(() => {
                message.value = ''
            }, 3000)
        } catch (error) {
            if (error.response && error.response.status === 404) {
                message.value = 'Quiz không tồn tại!'
            } else {
                console.error('Lỗi khi xóa quiz:', error)
                message.value = 'Xóa quiz thất bại!'
            }
            setTimeout(() => {
                message.value = ''
            }, 3000)
        }
    }
}

function playQuiz(quizId) {
    router.push({ name: 'PlayQuiz', params: { quizId, userId: userId.value } })
}
</script>

<template>
    <div class="quiz-crud-container">
        <!-- Animated Background Elements -->
        <div class="background-decorations">
            <div class="floating-orb orb-1"></div>
            <div class="floating-orb orb-2"></div>
            <div class="floating-orb orb-3"></div>
            <div class="floating-orb orb-4"></div>
        </div>

        <!-- Enhanced Hero Section -->
        <div class="hero-section">
            <div class="container">
            <div class="row justify-content-center">
                    <div class="col-lg-8 text-center">
                        <div class="hero-content">
                            <div class="hero-icon">
                                <i class="bi bi-puzzle"></i>
                            </div>
                            <h1 class="hero-title">
                                Quản lý Quiz của bạn
                            </h1>
                            <p class="hero-subtitle">Tạo, chỉnh sửa và quản lý bộ sưu tập quiz một cách dễ dàng với giao diện hiện đại</p>
                            <div class="hero-stats">
                                <div class="stat-item">
                                    <span class="stat-number">{{ quizzes.length }}</span>
                                    <span class="stat-label">Quiz</span>
                                </div>
                                <div class="stat-divider"></div>
                                <div class="stat-item">
                                    <span class="stat-number">{{ categories.length }}</span>
                                    <span class="stat-label">Danh mục</span>
                                </div>
                                <div class="stat-divider"></div>
                                <div class="stat-item">
                                    <span class="stat-number">∞</span>
                                    <span class="stat-label">Khả năng</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="container py-5">
            <!-- Loading Spinner -->
            <div v-if="isLoading" class="loading-section">
                <div class="loading-spinner-container">
                    <div class="modern-spinner">
                        <div class="spinner-ring"></div>
                        <div class="spinner-ring"></div>
                        <div class="spinner-ring"></div>
                    </div>
                    <h3 class="loading-text">Đang tải dữ liệu...</h3>
                    <p class="loading-subtitle">Vui lòng chờ trong giây lát</p>
                </div>
            </div>

            <div v-else>
                <!-- Enhanced Create Quiz Section -->
                <div class="row justify-content-center mb-5">
                    <div class="col-xl-8 col-lg-10">
                        <div class="create-quiz-card">
                            <div class="card-glow"></div>
                            <div class="card-header-custom">
                                <div class="header-icon">
                                    <i class="bi bi-plus-circle-fill"></i>
                                </div>
                                <h3 class="header-title">Tạo Quiz Mới</h3>
                                <p class="header-subtitle">Điền thông tin để tạo quiz mới của bạn</p>
                            </div>
                            
                            <div class="card-body-custom">
                            <form @submit.prevent="createQuiz">
                                    <div class="row">
                                        <!-- Enhanced Image Upload Section -->
                                        <div class="col-md-12 mb-4">
                                            <label class="form-label-custom">
                                                <i class="bi bi-image me-2"></i>Ảnh đại diện quiz
                                                <span class="label-optional">(Tùy chọn)</span>
                                            </label>
                                            <div class="image-upload-area-enhanced">
                                                <input 
                                                    type="file" 
                                                    class="form-control d-none" 
                                                    id="imageInput"
                                                    @change="handleImageUpload"
                                                    accept="image/*" 
                                                />
                                                <label for="imageInput" class="image-upload-label-enhanced">
                                                    <div v-if="!previewUrl" class="upload-placeholder-enhanced">
                                                        <div class="upload-icon-container">
                                                            <i class="bi bi-cloud-arrow-up"></i>
                                                        </div>
                                                        <h4 class="upload-title">Chọn ảnh cho quiz</h4>
                                                        <p class="upload-text">Kéo thả hoặc nhấp để chọn ảnh</p>
                                                        <div class="upload-formats">
                                                            <span class="format-tag">JPG</span>
                                                            <span class="format-tag">PNG</span>
                                                            <span class="format-tag">WEBP</span>
                                                        </div>
                                                    </div>
                                                    <div v-else class="preview-container-enhanced">
                                                        <img :src="previewUrl" alt="Preview" class="preview-image-enhanced" />
                                                        <div class="preview-overlay-enhanced">
                                                            <i class="bi bi-pencil-square"></i>
                                                            <span>Thay đổi ảnh</span>
                                                        </div>
                                                    </div>
                                                </label>
                                                <button 
                                                    v-if="previewUrl" 
                                                    type="button" 
                                                    class="btn-remove-image-enhanced"
                                                    @click="clearImage"
                                                >
                                                    <i class="bi bi-x-lg"></i>
                                        </button>
                                    </div>
                                </div>

                                        <!-- Enhanced Quiz Info -->
                                        <div class="col-md-6 mb-4">
                                            <label class="form-label-custom">
                                                <i class="bi bi-bookmark-fill me-2"></i>Danh mục quiz
                                            </label>
                                            <div class="input-container">
                                                <select v-model="selectedCategoryId" class="form-select-enhanced" required>
                                                    <option value="" disabled>🎯 Chọn danh mục...</option>
                                        <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                                            {{ cat.name }} - {{ cat.description }}
                                        </option>
                                    </select>
                                                <div class="input-border"></div>
                                            </div>
                                </div>

                                        <div class="col-md-6 mb-4">
                                            <label class="form-label-custom">
                                                <i class="bi bi-type me-2"></i>Tên quiz
                                            </label>
                                            <div class="input-container">
                                                <input 
                                                    type="text" 
                                                    v-model="title" 
                                                    class="form-control-enhanced"
                                                    placeholder="💡 Nhập tên quiz thú vị..." 
                                                    required 
                                                />
                                                <div class="input-border"></div>
                                            </div>
                                </div>

                                        <!-- Enhanced Privacy Setting -->
                                        <div class="col-12 mb-4">
                                            <label class="form-label-custom">
                                                <i class="bi bi-shield-check me-2"></i>Quyền riêng tư
                                            </label>
                                            <div class="privacy-options-enhanced">
                                                <div class="privacy-option-enhanced">
                                                    <input 
                                                        class="privacy-radio" 
                                                        type="radio" 
                                                        :value="true"
                                                        v-model="isPublic" 
                                                        id="publicYes" 
                                                    />
                                                    <label class="privacy-label-enhanced" for="publicYes">
                                                        <div class="privacy-icon public-icon">
                                                            <i class="bi bi-globe2"></i>
                                        </div>
                                                        <div class="privacy-content">
                                                            <strong>Công khai</strong>
                                                            <small>Mọi người có thể xem và chơi</small>
                                        </div>
                                                        <div class="privacy-checkmark">
                                                            <i class="bi bi-check-lg"></i>
                                                        </div>
                                                    </label>
                                                </div>
                                                <div class="privacy-option-enhanced">
                                                    <input 
                                                        class="privacy-radio" 
                                                        type="radio" 
                                                        :value="false"
                                                        v-model="isPublic" 
                                                        id="publicNo" 
                                                    />
                                                    <label class="privacy-label-enhanced" for="publicNo">
                                                        <div class="privacy-icon private-icon">
                                                            <i class="bi bi-lock"></i>
                                                        </div>
                                                        <div class="privacy-content">
                                                            <strong>Riêng tư</strong>
                                                            <small>Chỉ bạn có thể xem</small>
                                                        </div>
                                                        <div class="privacy-checkmark">
                                                            <i class="bi bi-check-lg"></i>
                                                        </div>
                                                    </label>
                                                </div>
                                    </div>
                                </div>

                                        <!-- Enhanced Submit Button -->
                                        <div class="col-12 text-center">
                                            <button 
                                                type="submit" 
                                                class="btn-create-quiz-enhanced"
                                                :disabled="isCreating"
                                            >
                                                <div class="btn-content">
                                                    <div v-if="isCreating" class="btn-spinner">
                                                        <div class="spinner-dots">
                                                            <div class="dot1"></div>
                                                            <div class="dot2"></div>
                                                            <div class="dot3"></div>
                                                        </div>
                                                        <span>Đang tạo quiz...</span>
                                                    </div>
                                                    <div v-else class="btn-normal">
                                                        <i class="bi bi-magic"></i>
                                                        <span>Tạo Quiz Ngay</span>
                                                        <i class="bi bi-arrow-right"></i>
                                                    </div>
                                                </div>
                                                <div class="btn-ripple"></div>
                                    </button>
                                        </div>
                                </div>
                            </form>
                            </div>
                        </div>
                        </div>
                    </div>

                <!-- Enhanced Quiz List Section -->
                <div class="row justify-content-center">
                    <div class="col-xl-12">
                        <div class="quiz-list-section">
                            <div class="section-header">
                                <div class="section-icon">
                                    <i class="bi bi-collection"></i>
                                </div>
                                <h3 class="section-title">
                                    Quiz của bạn
                                    <span class="quiz-count-enhanced">{{ quizzes.length }}</span>
                                </h3>
                                <p class="section-subtitle">Quản lý và chỉnh sửa các quiz đã tạo</p>
                            </div>

                            <!-- Loading Quiz List -->
                            <div v-if="loadingQuizzes" class="loading-quiz-section">
                                <div class="quiz-skeleton" v-for="n in 6" :key="n">
                                    <div class="skeleton-image"></div>
                                    <div class="skeleton-content">
                                        <div class="skeleton-line large"></div>
                                        <div class="skeleton-line medium"></div>
                                        <div class="skeleton-line small"></div>
                                    </div>
                                </div>
                            </div>

                            <!-- Enhanced Empty State -->
                            <div v-else-if="quizzes.length === 0" class="empty-state-enhanced">
                                <div class="empty-icon">
                                    <i class="bi bi-inbox"></i>
                                </div>
                                <h4 class="empty-title">Chưa có quiz nào</h4>
                                <p class="empty-text">Hãy tạo quiz đầu tiên để bắt đầu hành trình của bạn!</p>
                                <div class="empty-decoration">
                                    <div class="decoration-dot dot-1"></div>
                                    <div class="decoration-dot dot-2"></div>
                                    <div class="decoration-dot dot-3"></div>
                                </div>
                            </div>

                            <!-- Enhanced Quiz Grid -->
                            <div v-else class="quiz-grid-enhanced">
                                <div 
                                    v-for="quiz in quizzes" 
                                    :key="quiz.id"
                                    class="quiz-card-enhanced"
                                >
                                    <div class="card-inner">
                                        <div class="quiz-image-container-enhanced">
                                            <img 
                                                :src="getQuizImageUrl(quiz.id)" 
                                                alt="Quiz Image" 
                                                class="quiz-image-enhanced"
                                                loading="lazy"
                                            />
                                            <div class="image-overlay"></div>
                                            <div class="quiz-status-enhanced">
                                                <span :class="['status-badge-enhanced', quiz.public ? 'public' : 'private']">
                                                    <i :class="quiz.public ? 'bi bi-globe2' : 'bi bi-lock'"></i>
                                                    {{ quiz.public ? 'Công khai' : 'Riêng tư' }}
                                                </span>
                                            </div>
                                        </div>
                                        
                                        <div class="quiz-content-enhanced">
                                            <h5 class="quiz-title-enhanced">{{ quiz.title }}</h5>
                                            <p class="quiz-category-enhanced" v-if="quiz.category">
                                                <i class="bi bi-tag-fill me-1"></i>
                                                {{ quiz.category.name }}
                                            </p>
                                            
                                            <div class="quiz-actions-enhanced">
                                                <button 
                                                    class="action-btn-enhanced play-btn-enhanced" 
                                                    @click="playQuiz(quiz.id)"
                                                    title="Chơi quiz"
                                                >
                                                    <i class="bi bi-play-fill"></i>
                                                    <span>Chơi</span>
                                    </button>
                                                <button 
                                                    class="action-btn-enhanced edit-btn-enhanced" 
                                                    @click="editQuiz(quiz.id)"
                                                    title="Chỉnh sửa"
                                                >
                                                    <i class="bi bi-pencil-square"></i>
                                                    <span>Sửa</span>
                                    </button>
                                                <button 
                                                    class="action-btn-enhanced delete-btn-enhanced" 
                                                    @click="deleteQuiz(quiz.id)"
                                                    title="Xóa quiz"
                                                >
                                                    <i class="bi bi-trash3"></i>
                                                    <span>Xóa</span>
                                    </button>
                                </div>
                    </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
                    </div>

        <!-- Enhanced Toast Notification -->
        <div 
            v-if="message" 
            :class="['toast-notification-enhanced', message.includes('thành công') ? 'success' : 'error']"
        >
            <div class="toast-icon">
                <i :class="message.includes('thành công') ? 'bi bi-check-circle-fill' : 'bi bi-exclamation-triangle-fill'"></i>
                </div>
            <div class="toast-content">
                <strong class="toast-title">
                    {{ message.includes('thành công') ? 'Thành công!' : 'Lỗi!' }}
                </strong>
                <span class="toast-message">{{ message }}</span>
            </div>
            <button class="toast-close" @click="message = ''">
                <i class="bi bi-x-lg"></i>
            </button>
        </div>
    </div>
</template>

<style scoped>
/* === ENHANCED BASE STYLES === */
.quiz-crud-container {
    min-height: 100vh;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    position: relative;
    overflow: hidden;
}

/* === ANIMATED BACKGROUND === */
.background-decorations {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 0;
}

.floating-orb {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(20px);
    animation: float 6s ease-in-out infinite;
}

.orb-1 {
    width: 200px;
    height: 200px;
    top: 10%;
    left: -5%;
    animation-delay: 0s;
}

.orb-2 {
    width: 150px;
    height: 150px;
    top: 60%;
    right: -5%;
    animation-delay: 2s;
}

.orb-3 {
    width: 100px;
    height: 100px;
    top: 80%;
    left: 20%;
    animation-delay: 4s;
}

.orb-4 {
    width: 80px;
    height: 80px;
    top: 30%;
    right: 30%;
    animation-delay: 1s;
}

@keyframes float {
    0%, 100% { transform: translateY(0px) rotate(0deg); }
    33% { transform: translateY(-20px) rotate(5deg); }
    66% { transform: translateY(20px) rotate(-5deg); }
}

/* === ENHANCED HERO SECTION === */
.hero-section {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(20px);
    padding: 5rem 0 3rem;
    position: relative;
    z-index: 1;
}

.hero-content {
    position: relative;
}

.hero-icon {
    width: 100px;
    height: 100px;
    background: linear-gradient(45deg, #00d4ff, #5f27cd);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 2rem;
    box-shadow: 0 20px 60px rgba(95, 39, 205, 0.4);
    animation: pulse 2s infinite;
}

.hero-icon i {
    font-size: 3rem;
    color: white;
}

.hero-title {
    font-size: 3.5rem;
    font-weight: 800;
    background: linear-gradient(45deg, #fff, #f8f9fa, #00d4ff);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin-bottom: 1.5rem;
    text-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.hero-subtitle {
    font-size: 1.3rem;
    color: rgba(255, 255, 255, 0.9);
    margin-bottom: 2rem;
    line-height: 1.6;
}

.hero-stats {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 2rem;
    margin-top: 2rem;
}

.stat-item {
    text-align: center;
    color: white;
}

.stat-number {
    display: block;
    font-size: 2rem;
    font-weight: 700;
    color: #00d4ff;
    text-shadow: 0 2px 10px rgba(0, 212, 255, 0.3);
}

.stat-label {
    font-size: 0.9rem;
    opacity: 0.8;
    text-transform: uppercase;
    letter-spacing: 1px;
}

.stat-divider {
    width: 2px;
    height: 40px;
    background: rgba(255, 255, 255, 0.3);
    border-radius: 1px;
}

/* === ENHANCED LOADING === */
.loading-section {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 400px;
    position: relative;
    z-index: 1;
}

.loading-spinner-container {
    text-align: center;
    color: white;
}

.modern-spinner {
    position: relative;
    width: 100px;
    height: 100px;
    margin: 0 auto 2rem;
}

.spinner-ring {
    position: absolute;
    width: 100%;
    height: 100%;
    border: 3px solid transparent;
    border-top-color: #00d4ff;
    border-radius: 50%;
    animation: spin 1.5s linear infinite;
}

.spinner-ring:nth-child(2) {
    width: 80%;
    height: 80%;
    top: 10%;
    left: 10%;
    border-top-color: #5f27cd;
    animation-delay: 0.5s;
}

.spinner-ring:nth-child(3) {
    width: 60%;
    height: 60%;
    top: 20%;
    left: 20%;
    border-top-color: #fff;
    animation-delay: 1s;
}

.loading-text {
    font-size: 1.5rem;
    font-weight: 600;
    margin-bottom: 0.5rem;
}

.loading-subtitle {
    opacity: 0.8;
    font-size: 1rem;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

/* === ENHANCED CREATE QUIZ CARD === */
.create-quiz-card {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(30px);
    border-radius: 30px;
    box-shadow: 0 30px 80px rgba(0, 0, 0, 0.15);
    border: 2px solid rgba(255, 255, 255, 0.3);
    overflow: hidden;
    position: relative;
    z-index: 1;
    transition: all 0.3s ease;
}

.create-quiz-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 40px 100px rgba(0, 0, 0, 0.2);
}

.card-glow {
    position: absolute;
    top: -2px;
    left: -2px;
    right: -2px;
    bottom: -2px;
    background: linear-gradient(45deg, #00d4ff, #5f27cd, #764ba2, #667eea);
    border-radius: 30px;
    z-index: -1;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.create-quiz-card:hover .card-glow {
    opacity: 0.6;
}

.card-header-custom {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #5f27cd 100%);
    color: white;
    padding: 3rem 2rem;
    text-align: center;
    position: relative;
    overflow: hidden;
}

.card-header-custom::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: radial-gradient(circle at 30% 20%, rgba(255, 255, 255, 0.2) 0%, transparent 50%);
}

.header-icon {
    width: 80px;
    height: 80px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 1.5rem;
    backdrop-filter: blur(10px);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.header-icon i {
    font-size: 2rem;
    color: white;
}

.header-title {
    font-size: 2.2rem;
    font-weight: 700;
    margin-bottom: 0.5rem;
    text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.header-subtitle {
    opacity: 0.9;
    font-size: 1.1rem;
    margin: 0;
}

.card-body-custom {
    padding: 3rem;
}

/* === ENHANCED FORM ELEMENTS === */
.form-label-custom {
    font-weight: 700;
    color: #1a202c;
    margin-bottom: 1rem;
    display: flex;
    align-items: center;
    font-size: 1.1rem;
    text-shadow: 0 1px 3px rgba(255, 255, 255, 0.8);
}

.label-optional {
    font-size: 0.85rem;
    color: #4a5568;
    font-weight: 500;
    margin-left: auto;
}

.input-container {
    position: relative;
}

.form-control-enhanced,
.form-select-enhanced {
    border: 2px solid #e2e8f0;
    border-radius: 15px;
    padding: 1rem 1.25rem;
    font-size: 1.1rem;
    transition: all 0.3s ease;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    width: 100%;
    color: #1a202c;
    font-weight: 500;
}

.form-control-enhanced::placeholder,
.form-select-enhanced::placeholder {
    color: #718096;
    font-weight: 400;
}

.form-control-enhanced:focus,
.form-select-enhanced:focus {
    border-color: #00d4ff;
    box-shadow: 0 0 0 0.3rem rgba(0, 212, 255, 0.2);
    outline: none;
    background: white;
    transform: translateY(-2px);
    color: #1a202c;
}

.input-border {
    position: absolute;
    bottom: 0;
    left: 50%;
    width: 0;
    height: 3px;
    background: linear-gradient(45deg, #00d4ff, #5f27cd);
    transition: all 0.3s ease;
    transform: translateX(-50%);
    border-radius: 2px;
}

.form-control-enhanced:focus + .input-border,
.form-select-enhanced:focus + .input-border {
    width: 100%;
}

/* === ENHANCED IMAGE UPLOAD === */
.image-upload-area-enhanced {
    position: relative;
    border: 3px dashed #cbd5e0;
    border-radius: 20px;
    overflow: hidden;
    transition: all 0.4s ease;
    background: rgba(255, 255, 255, 0.5);
}

.image-upload-area-enhanced:hover {
    border-color: #00d4ff;
    background: rgba(0, 212, 255, 0.05);
    transform: translateY(-2px);
    box-shadow: 0 10px 30px rgba(0, 212, 255, 0.2);
}

.image-upload-label-enhanced {
    display: block;
    cursor: pointer;
    margin: 0;
}

.upload-placeholder-enhanced {
    padding: 4rem 2rem;
    text-align: center;
}

.upload-icon-container {
    width: 80px;
    height: 80px;
    background: linear-gradient(45deg, #00d4ff, #5f27cd);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 1.5rem;
    box-shadow: 0 15px 40px rgba(95, 39, 205, 0.3);
}

.upload-icon-container i {
    font-size: 2rem;
    color: white;
}

.upload-title {
    font-size: 1.3rem;
    font-weight: 700;
    color: #1a202c;
    margin-bottom: 0.5rem;
}

.upload-text {
    color: #4a5568;
    margin-bottom: 1.5rem;
    font-weight: 500;
}

.upload-formats {
    display: flex;
    justify-content: center;
    gap: 0.5rem;
}

.format-tag {
    background: rgba(0, 212, 255, 0.1);
    color: #00d4ff;
    padding: 0.25rem 0.75rem;
    border-radius: 20px;
    font-size: 0.8rem;
    font-weight: 600;
}

.preview-container-enhanced {
    position: relative;
    height: 250px;
}

.preview-image-enhanced {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.preview-overlay-enhanced {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.6);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: white;
    font-weight: 600;
    opacity: 0;
    transition: opacity 0.3s ease;
    backdrop-filter: blur(5px);
}

.preview-container-enhanced:hover .preview-overlay-enhanced {
    opacity: 1;
}

.preview-overlay-enhanced i {
    font-size: 2rem;
    margin-bottom: 0.5rem;
}

.btn-remove-image-enhanced {
    position: absolute;
    top: 15px;
    right: 15px;
    width: 40px;
    height: 40px;
    border: none;
    border-radius: 50%;
    background: rgba(220, 53, 69, 0.9);
    color: white;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    backdrop-filter: blur(10px);
    box-shadow: 0 5px 20px rgba(220, 53, 69, 0.4);
}

.btn-remove-image-enhanced:hover {
    background: #dc3545;
    transform: scale(1.1);
    box-shadow: 0 8px 25px rgba(220, 53, 69, 0.6);
}

/* === ENHANCED PRIVACY OPTIONS === */
.privacy-options-enhanced {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
}

.privacy-option-enhanced {
    position: relative;
}

.privacy-radio {
    position: absolute;
    opacity: 0;
}

.privacy-label-enhanced {
    display: flex;
    align-items: center;
    padding: 1.5rem;
    border: 2px solid #e2e8f0;
    border-radius: 20px;
    cursor: pointer;
    transition: all 0.3s ease;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    position: relative;
    overflow: hidden;
}

.privacy-label-enhanced::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
    transition: left 0.5s ease;
}

.privacy-label-enhanced:hover::before {
    left: 100%;
}

.privacy-label-enhanced:hover {
    border-color: #00d4ff;
    background: rgba(0, 212, 255, 0.05);
    transform: translateY(-2px);
    box-shadow: 0 10px 30px rgba(0, 212, 255, 0.15);
}

.privacy-radio:checked + .privacy-label-enhanced {
    border-color: #00d4ff;
    background: rgba(0, 212, 255, 0.1);
    box-shadow: 0 10px 30px rgba(0, 212, 255, 0.2);
}

.privacy-icon {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 1rem;
    flex-shrink: 0;
}

.public-icon {
    background: linear-gradient(45deg, #48bb78, #38a169);
    color: white;
}

.private-icon {
    background: linear-gradient(45deg, #a0aec0, #718096);
    color: white;
}

.privacy-content {
    flex: 1;
}

.privacy-content strong {
    display: block;
    color: #1a202c;
    margin-bottom: 0.25rem;
    font-size: 1.1rem;
    font-weight: 700;
}

.privacy-content small {
    color: #4a5568;
    font-weight: 500;
}

.privacy-checkmark {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background: #00d4ff;
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transform: scale(0);
    transition: all 0.3s ease;
}

.privacy-radio:checked + .privacy-label-enhanced .privacy-checkmark {
    opacity: 1;
    transform: scale(1);
}

/* === ENHANCED SUBMIT BUTTON === */
.btn-create-quiz-enhanced {
    background: linear-gradient(135deg, #00d4ff 0%, #5f27cd 50%, #764ba2 100%);
    color: white;
    border: none;
    padding: 1.25rem 3rem;
    border-radius: 50px;
    font-weight: 700;
    font-size: 1.1rem;
    cursor: pointer;
    transition: all 0.4s ease;
    box-shadow: 0 15px 40px rgba(95, 39, 205, 0.4);
    position: relative;
    overflow: hidden;
    min-width: 200px;
}

.btn-create-quiz-enhanced:hover:not(:disabled) {
    transform: translateY(-3px);
    box-shadow: 0 20px 60px rgba(95, 39, 205, 0.6);
    background: linear-gradient(135deg, #00d4ff 0%, #5f27cd 30%, #764ba2 100%);
}

.btn-create-quiz-enhanced:active {
    transform: translateY(-1px);
}

.btn-create-quiz-enhanced:disabled {
    opacity: 0.8;
    cursor: not-allowed;
}

.btn-content {
    position: relative;
    z-index: 2;
}

.btn-normal {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.75rem;
}

.btn-spinner {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 1rem;
}

.spinner-dots {
    display: flex;
    gap: 0.25rem;
}

.spinner-dots div {
    width: 8px;
    height: 8px;
    background: white;
    border-radius: 50%;
    animation: bounce 1.4s ease-in-out infinite both;
}

.dot2 { animation-delay: 0.16s; }
.dot3 { animation-delay: 0.32s; }

@keyframes bounce {
    0%, 80%, 100% { transform: scale(0); }
    40% { transform: scale(1); }
}

.btn-ripple {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.3);
    transform: translate(-50%, -50%);
    transition: width 0.6s, height 0.6s;
}

.btn-create-quiz-enhanced:active .btn-ripple {
    width: 300px;
    height: 300px;
}

/* === ENHANCED QUIZ LIST SECTION === */
.quiz-list-section {
    position: relative;
    z-index: 1;
}

.section-header {
    text-align: center;
    margin-bottom: 3rem;
    color: white;
}

.section-icon {
    width: 80px;
    height: 80px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 1.5rem;
    backdrop-filter: blur(20px);
    box-shadow: 0 15px 40px rgba(0, 0, 0, 0.2);
}

.section-icon i {
    font-size: 2rem;
    color: white;
}

.section-title {
    font-size: 2.5rem;
    font-weight: 700;
    margin-bottom: 0.5rem;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 1rem;
}

.quiz-count-enhanced {
    background: rgba(0, 212, 255, 0.2);
    color: #00d4ff;
    padding: 0.5rem 1rem;
    border-radius: 25px;
    font-size: 1rem;
    backdrop-filter: blur(10px);
    box-shadow: 0 5px 20px rgba(0, 212, 255, 0.2);
}

.section-subtitle {
    opacity: 0.9;
    font-size: 1.1rem;
    margin: 0;
}

/* === ENHANCED LOADING SKELETON === */
.loading-quiz-section {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 2rem;
    margin-top: 2rem;
}

.quiz-skeleton {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 20px;
    overflow: hidden;
    backdrop-filter: blur(20px);
}

.skeleton-image {
    height: 200px;
    background: linear-gradient(90deg, rgba(255, 255, 255, 0.1) 25%, rgba(255, 255, 255, 0.2) 50%, rgba(255, 255, 255, 0.1) 75%);
    background-size: 200% 100%;
    animation: shimmer 2s infinite;
}

.skeleton-content {
    padding: 1.5rem;
}

.skeleton-line {
    height: 1rem;
    background: linear-gradient(90deg, rgba(255, 255, 255, 0.1) 25%, rgba(255, 255, 255, 0.2) 50%, rgba(255, 255, 255, 0.1) 75%);
    background-size: 200% 100%;
    animation: shimmer 2s infinite;
    border-radius: 0.5rem;
    margin-bottom: 1rem;
}

.skeleton-line.large { width: 80%; }
.skeleton-line.medium { width: 60%; }
.skeleton-line.small { width: 40%; }

@keyframes shimmer {
    0% { background-position: -200% 0; }
    100% { background-position: 200% 0; }
}

/* === ENHANCED EMPTY STATE === */
.empty-state-enhanced {
    text-align: center;
    padding: 5rem 2rem;
    color: white;
    position: relative;
}

.empty-icon {
    width: 120px;
    height: 120px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 2rem;
    backdrop-filter: blur(20px);
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.empty-icon i {
    font-size: 3rem;
    opacity: 0.7;
}

.empty-title {
    font-size: 2rem;
    font-weight: 700;
    margin-bottom: 1rem;
}

.empty-text {
    font-size: 1.1rem;
    opacity: 0.8;
    max-width: 400px;
    margin: 0 auto 2rem;
    line-height: 1.6;
}

.empty-decoration {
    display: flex;
    justify-content: center;
    gap: 1rem;
}

.decoration-dot {
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.3);
    animation: pulse 2s infinite;
}

.dot-2 { animation-delay: 0.5s; }
.dot-3 { animation-delay: 1s; }

@keyframes pulse {
    0%, 100% { opacity: 0.3; transform: scale(1); }
    50% { opacity: 1; transform: scale(1.2); }
}

/* === ENHANCED QUIZ GRID === */
.quiz-grid-enhanced {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 2.5rem;
    margin-top: 3rem;
}

/* === ENHANCED QUIZ CARD === */
.quiz-card-enhanced {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(30px);
    border-radius: 25px;
    overflow: hidden;
    box-shadow: 0 15px 50px rgba(0, 0, 0, 0.1);
    transition: all 0.4s ease;
    border: 2px solid rgba(255, 255, 255, 0.3);
    position: relative;
}

.quiz-card-enhanced:hover {
    transform: translateY(-10px) scale(1.02);
    box-shadow: 0 30px 80px rgba(0, 0, 0, 0.2);
    border-color: rgba(0, 212, 255, 0.5);
}

.card-inner {
    position: relative;
    height: 100%;
}

.quiz-image-container-enhanced {
    position: relative;
    height: 220px;
    overflow: hidden;
}

.quiz-image-enhanced {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.4s ease;
}

.quiz-card-enhanced:hover .quiz-image-enhanced {
    transform: scale(1.1);
}

.image-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 50%;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
    opacity: 0;
    transition: opacity 0.3s ease;
}

.quiz-card-enhanced:hover .image-overlay {
    opacity: 1;
}

.quiz-status-enhanced {
    position: absolute;
    top: 1rem;
    right: 1rem;
    z-index: 2;
}

.status-badge-enhanced {
    padding: 0.75rem 1.25rem;
    border-radius: 25px;
    font-size: 0.8rem;
    font-weight: 700;
    backdrop-filter: blur(20px);
    display: flex;
    align-items: center;
    gap: 0.5rem;
    box-shadow: 0 5px 20px rgba(0, 0, 0, 0.2);
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.status-badge-enhanced.public {
    background: rgba(72, 187, 120, 0.9);
    color: white;
    border: 1px solid rgba(255, 255, 255, 0.3);
}

.status-badge-enhanced.private {
    background: rgba(160, 174, 192, 0.9);
    color: white;
    border: 1px solid rgba(255, 255, 255, 0.3);
}

.quiz-content-enhanced {
    padding: 2rem;
    height: calc(100% - 220px);
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.quiz-title-enhanced {
    margin-bottom: 1rem;
    color: #1a202c;
    font-weight: 800;
    font-size: 1.3rem;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.quiz-category-enhanced {
    color: #4a5568;
    font-size: 0.95rem;
    margin-bottom: 1.5rem;
    font-weight: 600;
}

/* === ENHANCED QUIZ ACTIONS === */
.quiz-actions-enhanced {
    display: flex;
    gap: 0.75rem;
    margin-top: auto;
}

.action-btn-enhanced {
    flex: 1;
    padding: 0.875rem 1rem;
    border: none;
    border-radius: 15px;
    font-size: 0.9rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    position: relative;
    overflow: hidden;
}

.action-btn-enhanced::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    transition: left 0.5s ease;
}

.action-btn-enhanced:hover::before {
    left: 100%;
}

.play-btn-enhanced {
    background: linear-gradient(135deg, #48bb78, #38a169);
    color: white;
    box-shadow: 0 5px 20px rgba(72, 187, 120, 0.3);
}

.play-btn-enhanced:hover {
    background: linear-gradient(135deg, #38a169, #2f855a);
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(72, 187, 120, 0.5);
}

.edit-btn-enhanced {
    background: linear-gradient(135deg, #4299e1, #3182ce);
    color: white;
    box-shadow: 0 5px 20px rgba(66, 153, 225, 0.3);
}

.edit-btn-enhanced:hover {
    background: linear-gradient(135deg, #3182ce, #2b77cb);
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(66, 153, 225, 0.5);
}

.delete-btn-enhanced {
    background: linear-gradient(135deg, #f56565, #e53e3e);
    color: white;
    box-shadow: 0 5px 20px rgba(245, 101, 101, 0.3);
}

.delete-btn-enhanced:hover {
    background: linear-gradient(135deg, #e53e3e, #c53030);
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(245, 101, 101, 0.5);
}

/* === ENHANCED TOAST NOTIFICATION === */
.toast-notification-enhanced {
    position: fixed;
    top: 2rem;
    right: 2rem;
    background: rgba(255, 255, 255, 0.98);
    backdrop-filter: blur(20px);
    border-radius: 20px;
    padding: 1.5rem;
    color: #1a202c;
    font-weight: 500;
    z-index: 1000;
    animation: slideInRight 0.4s ease;
    display: flex;
    align-items: center;
    gap: 1rem;
    max-width: 450px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
    border: 2px solid rgba(255, 255, 255, 0.3);
}

.toast-notification-enhanced.success {
    border-left: 5px solid #48bb78;
}

.toast-notification-enhanced.error {
    border-left: 5px solid #f56565;
}

.toast-icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.success .toast-icon {
    background: rgba(72, 187, 120, 0.1);
    color: #48bb78;
}

.error .toast-icon {
    background: rgba(245, 101, 101, 0.1);
    color: #f56565;
}

.toast-content {
    flex: 1;
}

.toast-title {
    display: block;
    font-size: 1rem;
    margin-bottom: 0.25rem;
}

.toast-message {
    font-size: 0.9rem;
    opacity: 0.8;
}

.toast-close {
    width: 32px;
    height: 32px;
    border: none;
    background: rgba(0, 0, 0, 0.1);
    border-radius: 50%;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
}

.toast-close:hover {
    background: rgba(0, 0, 0, 0.2);
    transform: scale(1.1);
}

/* === ANIMATIONS === */
@keyframes slideInRight {
    from {
        transform: translateX(100%);
        opacity: 0;
    }
    to {
        transform: translateX(0);
        opacity: 1;
    }
}

/* === RESPONSIVE DESIGN === */
@media (max-width: 992px) {
    .hero-title {
        font-size: 2.5rem;
    }
    
    .hero-stats {
        gap: 1rem;
    }
    
    .quiz-grid-enhanced {
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
        gap: 2rem;
    }
}

@media (max-width: 768px) {
    .hero-title {
        font-size: 2rem;
    }
    
    .hero-subtitle {
        font-size: 1.1rem;
    }
    
    .hero-stats {
        flex-direction: column;
        gap: 1rem;
    }
    
    .stat-divider {
        width: 40px;
        height: 2px;
    }
    
    .card-header-custom,
    .card-body-custom {
        padding: 2rem 1.5rem;
    }
    
    .privacy-options-enhanced {
        grid-template-columns: 1fr;
    }
    
    .quiz-grid-enhanced {
        grid-template-columns: 1fr;
        gap: 1.5rem;
    }
    
    .loading-quiz-section {
        grid-template-columns: 1fr;
    }
    
    .quiz-actions-enhanced {
        flex-direction: column;
    }
    
    .action-btn-enhanced {
        flex: none;
        width: 100%;
    }
    
    .toast-notification-enhanced {
        top: 1rem;
        right: 1rem;
        left: 1rem;
        max-width: none;
    }
}

@media (max-width: 480px) {
    .hero-section {
        padding: 3rem 0 2rem;
    }
    
    .hero-icon {
        width: 70px;
        height: 70px;
    }
    
    .hero-icon i {
        font-size: 2rem;
    }
    
    .container {
        padding: 0 1rem;
    }
    
    .card-body-custom {
        padding: 1.5rem;
    }
    
    .btn-create-quiz-enhanced {
        padding: 1rem 2rem;
        font-size: 1rem;
    }
}

/* === ACCESSIBILITY === */
@media (prefers-reduced-motion: reduce) {
    * {
        animation-duration: 0.01ms !important;
        animation-iteration-count: 1 !important;
        transition-duration: 0.01ms !important;
    }
}

/* === PRINT STYLES === */
@media print {
    .quiz-crud-container {
        background: white !important;
        color: black !important;
    }
    
    .floating-orb,
    .background-decorations {
        display: none !important;
    }
}
</style>