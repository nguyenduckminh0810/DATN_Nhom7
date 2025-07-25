<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const token = localStorage.getItem('token')
const userId = route.params.userId
const quizId = route.params.quizId

// State management
const questions = ref([])
const answersMap = ref({})
const quizInfo = ref({ id: quizId, title: '', description: '', category: '', image: '' })
const activeTab = ref('info')
const isLoading = ref(false)
const isSaving = ref(false)
const searchQuery = ref('')
const selectedQuestions = ref([])
const showPreview = ref(false)

// Image upload states
const imageUploadType = ref('url') // 'url' or 'file'
const selectedImageFile = ref(null)
const imagePreview = ref('')
const isUploadingImage = ref(false)

// Form states
const newQuestion = ref({
    content: '',
    point: 1,
    answers: Array(4).fill().map((_, i) => ({ content: '', correct: i === 0 }))
})

const editingQuestion = ref(null)
const validationErrors = ref({})

// Computed properties
const filteredQuestions = computed(() => {
    if (!searchQuery.value) return questions.value
    return questions.value.filter(q => 
        q.content.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
})

const totalPoints = computed(() => {
    return questions.value.reduce((sum, q) => sum + (q.point || 0), 0)
})

const quizStats = computed(() => ({
    totalQuestions: questions.value.length,
    totalPoints: totalPoints.value,
    avgPoints: questions.value.length ? Math.round(totalPoints.value / questions.value.length) : 0,
    hasAnswers: questions.value.every(q => answersMap.value[q.id]?.length >= 2)
}))

// Validation
const validateQuestion = (question) => {
    const errors = {}
    
    if (!question.content?.trim()) {
        errors.content = 'Nội dung câu hỏi không được để trống'
    }
    
    if (!question.point || question.point < 1) {
        errors.point = 'Điểm phải lớn hơn 0'
    }
    
    const answers = question.answers || []
    const correctCount = answers.filter(a => a.correct).length
    const emptyAnswers = answers.filter(a => !a.content?.trim()).length
    
    if (emptyAnswers > 0) {
        errors.answers = 'Tất cả câu trả lời phải có nội dung'
    } else if (correctCount !== 1) {
        errors.answers = 'Phải chọn đúng một câu trả lời đúng'
    }
    
    return errors
}

// Image handling functions
function handleImageUploadTypeChange(type) {
    imageUploadType.value = type
    if (type === 'url') {
        selectedImageFile.value = null
        imagePreview.value = ''
    } else {
        quizInfo.value.image = ''
    }
}

function handleImageFileSelect(event) {
    const file = event.target.files[0]
    if (file) {
        // Validate file type
        if (!file.type.startsWith('image/')) {
            showNotification('Vui lòng chọn file hình ảnh!', 'error')
            return
        }
        
        // Validate file size (max 5MB)
        if (file.size > 5 * 1024 * 1024) {
            showNotification('Kích thước file không được vượt quá 5MB!', 'error')
            return
        }
        
        selectedImageFile.value = file
        
        // Create preview
        const reader = new FileReader()
        reader.onload = (e) => {
            imagePreview.value = e.target.result
        }
        reader.readAsDataURL(file)
    }
}

async function uploadImageFile() {
    if (!selectedImageFile.value) return null
    
    isUploadingImage.value = true
    try {
        const formData = new FormData()
        formData.append('image', selectedImageFile.value)
        
      
        const response = await axios.post('http://localhost:8080/api/upload/image', formData, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'multipart/form-data'
            }
        })
        
        if (response.data.success) {
            // ✅ CHỈ TRẢ VỀ FILENAME, KHÔNG PHẢI FULL URL
            const filename = response.data.filename
            console.log('✅ Upload thành công, filename:', filename)
            return filename
        } else {
            throw new Error(response.data.message || 'Upload failed')
        }
    } catch (error) {
        console.error('Error uploading image:', error)
        showNotification('Lỗi khi upload hình ảnh: ' + (error.response?.data?.message || error.message), 'error')
        return null
    } finally {
        isUploadingImage.value = false
    }
}

function clearImage() {
    quizInfo.value.image = ''
    selectedImageFile.value = null
    imagePreview.value = ''
    const fileInput = document.getElementById('imageFileInput')
    if (fileInput) fileInput.value = ''
}

// API functions
async function fetchQuizInfo() {
    try {
        const res = await axios.get(`http://localhost:8080/api/quiz/${quizId}`, {
            headers: { Authorization: `Bearer ${token}` }
        })
        quizInfo.value = { ...quizInfo.value, ...res.data }
        // Set image preview if image exists
        if (quizInfo.value.image) {
            
            imagePreview.value = `http://localhost:8080/api/image/quiz/${quizId}`
            imageUploadType.value = 'url'
        }
        console.log('Quiz info loaded:', quizInfo.value)
    } catch (err) {
        console.error('Lỗi khi lấy thông tin quiz:', err)
        showNotification('Không thể tải thông tin quiz', 'error')
    }
}

async function fetchAnswersForQuestion(questionId) {
    try {
        const res = await axios.get(`http://localhost:8080/api/answer/${questionId}`, {
            headers: { Authorization: `Bearer ${token}` }
        })
        answersMap.value[questionId] = res.data
    } catch (error) {
        console.error('Lỗi khi lấy câu trả lời:', error)
    }
}

async function fetchQuestionsByQuizId() {
    isLoading.value = true
    try {
        const res = await axios.get(`http://localhost:8080/api/question/${quizId}`, {
            headers: { Authorization: `Bearer ${token}` }
        })
        questions.value = res.data
        await Promise.all(res.data.map(q => fetchAnswersForQuestion(q.id)))
        console.log('Questions loaded:', questions.value.length)
    } catch (error) {
        console.error('Lỗi khi lấy câu hỏi:', error)
        showNotification('Không thể tải danh sách câu hỏi', 'error')
    } finally {
        isLoading.value = false
    }
}

// CRUD operations
async function updateQuizInfo() {
    const errors = {}
    if (!quizInfo.value.title?.trim()) {
        errors.title = 'Tiêu đề không được để trống'
    }
    
    if (Object.keys(errors).length > 0) {
        validationErrors.value = errors
        return
    }
    
    isSaving.value = true
    try {
        let imageUrl = quizInfo.value.image
        
        // Upload image file if selected
        if (imageUploadType.value === 'file' && selectedImageFile.value) {
            const uploadedFilename = await uploadImageFile()
            if (uploadedFilename) {
                imageUrl = uploadedFilename  // ✅ LƯU FILENAME THAY VÌ FULL URL
                console.log('🖼️ Image filename sẽ được lưu:', imageUrl)
            }
        }
        
     
        if (imageUploadType.value === 'url' && quizInfo.value.image && quizInfo.value.image.startsWith('http')) {
            try {
                const response = await axios.post('http://localhost:8080/api/upload/url', 
                    new URLSearchParams({ imageUrl: quizInfo.value.image }), {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/x-www-form-urlencoded'
                    }
                })
                if (response.data.success) {
                    imageUrl = response.data.filename
                    console.log('🌐 URL converted to filename:', imageUrl)
                }
            } catch (err) {
                console.error('Lỗi khi download URL:', err)
                showNotification('Không thể download ảnh từ URL', 'error')
                return
            }
        }

        const payload = {
            ...quizInfo.value,
            image: imageUrl || null,
            category: quizInfo.value.category || null
        }
        
        await axios.put(`http://localhost:8080/api/quiz/${quizId}`, payload, {
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        })
        
        // Update local state
        quizInfo.value.image = imageUrl
        
        
        if (imageUrl) {
            // ✅ SỬ DỤNG ENDPOINT CHUẨN CHO PREVIEW
            imagePreview.value = `http://localhost:8080/api/image/quiz/${quizId}`
            console.log('🎯 Preview updated với endpoint chuẩn')
        }
        
        showNotification('Cập nhật thông tin quiz thành công!', 'success')
        validationErrors.value = {}
    } catch (err) {
        console.error('Lỗi khi cập nhật quiz:', err)
        showNotification('Cập nhật thất bại!', 'error')
    } finally {
        isSaving.value = false
    }
}

// ... (keep all other existing functions unchanged)

async function createQuestion() {
    const errors = validateQuestion(newQuestion.value)
    if (Object.keys(errors).length > 0) {
        validationErrors.value = errors
        return
    }
    
    isSaving.value = true
    try {
        const questionPayload = {
            content: newQuestion.value.content.trim(),
            point: newQuestion.value.point,
            quiz: { id: quizId },
            image: null
        }

        const questionRes = await axios.post(`http://localhost:8080/api/question/create`, questionPayload, {
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        })

        const createdQuestion = questionRes.data

        const answersPayload = newQuestion.value.answers.map(a => ({
            content: a.content.trim(),
            correct: a.correct,
            question: { id: createdQuestion.id }
        }))

        const answerRes = await axios.post(`http://localhost:8080/api/answer/create-multiple`, answersPayload, {
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        })

        questions.value.push(createdQuestion)
        answersMap.value[createdQuestion.id] = answerRes.data

        // Reset form
        newQuestion.value = {
            content: '',
            point: 1,
            answers: Array(4).fill().map((_, i) => ({ content: '', correct: i === 0 }))
        }

        validationErrors.value = {}
        showNotification('Thêm câu hỏi thành công!', 'success')
        activeTab.value = 'questions'
    } catch (err) {
        console.error('Lỗi khi tạo câu hỏi:', err)
        showNotification('Tạo câu hỏi thất bại!', 'error')
    } finally {
        isSaving.value = false
    }
}

async function updateQuestion(question) {
    const errors = validateQuestion(question)
    if (Object.keys(errors).length > 0) {
        validationErrors.value = errors
        return
    }
    
    isSaving.value = true
    try {
        const payload = {
            id: question.id,
            content: question.content,
            point: question.point,
            quiz: { id: quizId },
            image: null
        }

        await axios.put(`http://localhost:8080/api/question/update/${question.id}`, payload, {
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        })

        showNotification('Cập nhật câu hỏi thành công!', 'success')
        editingQuestion.value = null
        validationErrors.value = {}
    } catch (err) {
        console.error('Lỗi khi cập nhật câu hỏi:', err)
        showNotification('Cập nhật thất bại!', 'error')
    } finally {
        isSaving.value = false
    }
}

async function updateAnswers(questionId) {
    isSaving.value = true
    try {
        const answers = answersMap.value[questionId]
        const payload = answers.map((a) => ({
            id: a.id,
            content: a.content,
            correct: a.correct,
            question: { id: questionId }
        }))

        await axios.put(`http://localhost:8080/api/answer/update`, payload, {
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        })

        showNotification('Cập nhật câu trả lời thành công!', 'success')
    } catch (error) {
        console.error('Lỗi khi cập nhật câu trả lời:', error)
        showNotification('Cập nhật câu trả lời thất bại!', 'error')
    } finally {
        isSaving.value = false
    }
}

async function deleteQuestion(questionId) {
    if (!confirm('Bạn có chắc chắn muốn xoá câu hỏi này?')) return

    isSaving.value = true
    try {
        await axios.delete(`http://localhost:8080/api/question/delete/${questionId}`, {
            headers: { Authorization: `Bearer ${token}` }
        })

        questions.value = questions.value.filter(q => q.id !== questionId)
        delete answersMap.value[questionId]
        selectedQuestions.value = selectedQuestions.value.filter(id => id !== questionId)

        showNotification('Xoá câu hỏi thành công!', 'success')
    } catch (err) {
        console.error('Lỗi khi xoá câu hỏi:', err)
        showNotification('Xoá câu hỏi thất bại!', 'error')
    } finally {
        isSaving.value = false
    }
}

// Helper functions
function setNewCorrectAnswer(index) {
    newQuestion.value.answers.forEach((a, i) => a.correct = i === index)
}

function setCorrectAnswer(questionId, answerId) {
    if (answersMap.value[questionId]) {
        answersMap.value[questionId].forEach((a) => {
            a.correct = a.id === answerId
        })
    }
}

function startEditQuestion(question) {
    editingQuestion.value = { ...question }
    validationErrors.value = {}
}

function cancelEdit() {
    editingQuestion.value = null
    validationErrors.value = {}
}

function toggleQuestionSelection(questionId) {
    const index = selectedQuestions.value.indexOf(questionId)
    if (index > -1) {
        selectedQuestions.value.splice(index, 1)
    } else {
        selectedQuestions.value.push(questionId)
    }
}

function selectAllQuestions() {
    if (selectedQuestions.value.length === questions.value.length) {
        selectedQuestions.value = []
    } else {
        selectedQuestions.value = questions.value.map(q => q.id)
    }
}

async function deleteSelectedQuestions() {
    if (!confirm(`Bạn có chắc chắn muốn xoá ${selectedQuestions.value.length} câu hỏi đã chọn?`)) return
    
    isSaving.value = true
    try {
        await Promise.all(
            selectedQuestions.value.map(id => 
                axios.delete(`http://localhost:8080/api/question/delete/${id}`, {
                    headers: { Authorization: `Bearer ${token}` }
                })
            )
        )
        
        questions.value = questions.value.filter(q => !selectedQuestions.value.includes(q.id))
        selectedQuestions.value.forEach(id => delete answersMap.value[id])
        selectedQuestions.value = []
        
        showNotification('Xoá các câu hỏi thành công!', 'success')
    } catch (err) {
        console.error('Lỗi khi xoá câu hỏi:', err)
        showNotification('Xoá câu hỏi thất bại!', 'error')
    } finally {
        isSaving.value = false
    }
}

function duplicateQuestion(question) {
    const duplicated = {
        content: question.content + ' (Copy)',
        point: question.point,
        answers: answersMap.value[question.id]?.map(a => ({
            content: a.content,
            correct: a.correct
        })) || []
    }
    
    newQuestion.value = duplicated
    activeTab.value = 'add-question'
}

function showNotification(message, type = 'info') {
    if (type === 'success') {
        alert('✅ ' + message)
    } else if (type === 'error') {
        alert('❌ ' + message)
    } else {
        alert('ℹ️ ' + message)
    }
}

function goBack() {
    router.push({ name: 'QuizCRUD', params: { quizId, userId } })
}

function previewQuiz() {
    router.push({ name: 'PlayQuiz', params: { quizId, userId } })
}

// Tab switching function
function switchTab(tabName) {
    activeTab.value = tabName
    validationErrors.value = {}
    console.log('Switched to tab:', tabName)
}

// Lifecycle
onMounted(async () => {
    console.log('Component mounted, loading data...')
    await Promise.all([fetchQuizInfo(), fetchQuestionsByQuizId()])
})

// Watch for validation errors cleanup
watch(newQuestion, () => {
    if (Object.keys(validationErrors.value).length > 0) {
        validationErrors.value = {}
    }
}, { deep: true })

// Watch image URL changes for preview
watch(() => quizInfo.value.image, (newUrl) => {
    if (newUrl && imageUploadType.value === 'url') {
        imagePreview.value = newUrl
    }
})
</script>

<template>
  <div class="edit-quiz-container">
    <!-- Header -->
    <div class="quiz-header">
      <div class="container">
        <div class="header-content">
          <div class="header-left">
            <button @click="goBack" class="back-btn">
              <i class="bi bi-arrow-left"></i>
            </button>
            <div class="header-info">
              <h1 class="quiz-title">{{ quizInfo.title || 'Chỉnh sửa Quiz' }}</h1>
              <div class="quiz-meta">
                <span class="meta-item">
                  <i class="bi bi-question-circle"></i>
                  {{ quizStats.totalQuestions }} câu hỏi
                </span>
                <span class="meta-item">
                  <i class="bi bi-star"></i>
                  {{ quizStats.totalPoints }} điểm
                </span>
                <span class="meta-item" :class="{ 'text-success': quizStats.hasAnswers, 'text-warning': !quizStats.hasAnswers }">
                  <i :class="quizStats.hasAnswers ? 'bi bi-check-circle' : 'bi bi-exclamation-triangle'"></i>
                  {{ quizStats.hasAnswers ? 'Hoàn tất' : 'Chưa hoàn tất' }}
                </span>
                    </div>
            </div>
          </div>
          <div class="header-actions">
            <button @click="previewQuiz" class="action-btn preview-btn">
              <i class="bi bi-play-circle"></i>
              <span>Xem trước</span>
            </button>
          </div>
        </div>
            </div>
        </div>

    <div class="container">
      <div class="row">
        <!-- Main Content -->
        <div class="col-lg-8">
          <!-- Navigation Tabs -->
          <div class="nav-tabs-container">
            <nav class="nav nav-tabs">
              <button 
                class="nav-link" 
                :class="{ active: activeTab === 'info' }"
                @click="switchTab('info')"
              >
                <i class="bi bi-info-circle"></i>
                <span>Thông tin Quiz</span>
              </button>
              <button 
                class="nav-link" 
                :class="{ active: activeTab === 'questions' }"
                @click="switchTab('questions')"
              >
                <i class="bi bi-list-ul"></i>
                <span>Câu hỏi ({{ questions.length }})</span>
              </button>
              <button 
                class="nav-link" 
                :class="{ active: activeTab === 'add-question' }"
                @click="switchTab('add-question')"
              >
                <i class="bi bi-plus-circle"></i>
                <span>Thêm câu hỏi</span>
              </button>
            </nav>
          </div>

          <!-- Tab Content -->
          <div class="tab-content">
            <!-- Quiz Info Tab -->
            <div v-if="activeTab === 'info'" class="tab-pane active">
              <div class="content-card">
                <div class="card-header">
                  <h3 class="card-title">
                    <i class="bi bi-gear"></i>
                    Cài đặt Quiz
                  </h3>
                </div>
            <div class="card-body">
                  <form @submit.prevent="updateQuizInfo" class="quiz-form">
                    <div class="form-group">
                      <label class="form-label">Tiêu đề Quiz *</label>
                      <input 
                        type="text" 
                        class="form-control"
                        :class="{ 'is-invalid': validationErrors.title }"
                        v-model="quizInfo.title" 
                        placeholder="Nhập tiêu đề quiz..."
                      />
                      <div v-if="validationErrors.title" class="invalid-feedback">
                        {{ validationErrors.title }}
                    </div>
                            </div>

                    <div class="form-group">
                      <label class="form-label">Mô tả</label>
                      <textarea 
                        class="form-control" 
                        v-model="quizInfo.description" 
                        rows="3"
                        placeholder="Mô tả ngắn về quiz này..."
                      ></textarea>
                        </div>

                    <div class="row">
                      <div class="col-md-6">
                        <div class="form-group">
                          <label class="form-label">Danh mục</label>
                          <select class="form-control" v-model="quizInfo.category">
                            <option value="">Chọn danh mục</option>
                            <option value="education">Giáo dục</option>
                            <option value="entertainment">Giải trí</option>
                            <option value="science">Khoa học</option>
                            <option value="history">Lịch sử</option>
                            <option value="technology">Công nghệ</option>
                          </select>
                    </div>
                      </div>
                      <div class="col-md-6">
                        <div class="form-group">
                          <label class="form-label">Hình ảnh Quiz</label>
                          
                          <!-- Image Upload Type Selector -->
                          <div class="image-upload-selector">
                            <div class="upload-type-tabs">
                              <button 
                                type="button"
                                class="upload-type-btn"
                                :class="{ active: imageUploadType === 'url' }"
                                @click="handleImageUploadTypeChange('url')"
                              >
                                <i class="bi bi-link-45deg"></i>
                                URL
                              </button>
                              <button 
                                type="button"
                                class="upload-type-btn"
                                :class="{ active: imageUploadType === 'file' }"
                                @click="handleImageUploadTypeChange('file')"
                              >
                                <i class="bi bi-upload"></i>
                                Upload
                              </button>
                            </div>
                    </div>

                          <!-- URL Input -->
                          <div v-if="imageUploadType === 'url'" class="image-url-input">
                            <input 
                              type="url" 
                              class="form-control" 
                              v-model="quizInfo.image"
                              placeholder="https://example.com/image.jpg"
                            />
                          </div>

                          <!-- File Upload -->
                          <div v-if="imageUploadType === 'file'" class="image-file-input">
                            <input 
                              type="file" 
                              id="imageFileInput"
                              class="form-control"
                              accept="image/*"
                              @change="handleImageFileSelect"
                            />
                            <small class="form-text">Chọn file hình ảnh (tối đa 5MB)</small>
                          </div>

                          <!-- Image Preview -->
                          <div v-if="imagePreview" class="image-preview-container">
                            <div class="image-preview">
                              <img :src="imagePreview" alt="Quiz image preview" />
                              <button 
                                type="button" 
                                class="remove-image-btn"
                                @click="clearImage"
                              >
                                <i class="bi bi-x-lg"></i>
                              </button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>

                    <div class="form-actions">
                      <button type="submit" class="btn btn-primary" :disabled="isSaving || isUploadingImage">
                        <i class="bi bi-check-lg" v-if="!isSaving && !isUploadingImage"></i>
                        <i class="bi bi-arrow-clockwise spin" v-else></i>
                        <span>
                          {{ isUploadingImage ? 'Đang upload...' : (isSaving ? 'Đang lưu...' : 'Lưu thay đổi') }}
                        </span>
                      </button>
                    </div>
                </form>
                </div>
            </div>
        </div>

            <!-- Questions List Tab -->
            <div v-if="activeTab === 'questions'" class="tab-pane active">
              <div class="content-card">
                <div class="card-header">
                  <div class="header-left">
                    <h3 class="card-title">
                      <i class="bi bi-list-ul"></i>
                      Danh sách câu hỏi
                    </h3>
                  </div>
                  <div class="header-actions">
                    <div class="search-box">
                      <i class="bi bi-search"></i>
                      <input 
                        type="text" 
                        placeholder="Tìm kiếm câu hỏi..." 
                        v-model="searchQuery"
                      />
                    </div>
                    <div class="bulk-actions" v-if="selectedQuestions.length > 0">
                      <button @click="deleteSelectedQuestions" class="btn btn-danger btn-sm">
                        <i class="bi bi-trash"></i>
                        Xoá ({{ selectedQuestions.length }})
                      </button>
                    </div>
                  </div>
                </div>
                <div class="card-body">
                  <div v-if="isLoading" class="loading-state">
                    <div class="spinner"></div>
                    <p>Đang tải câu hỏi...</p>
                  </div>

                  <div v-else-if="filteredQuestions.length === 0" class="empty-state">
                    <div class="empty-icon">
                      <i class="bi bi-question-circle"></i>
                    </div>
                    <h4>Chưa có câu hỏi nào</h4>
                    <p>Hãy thêm câu hỏi đầu tiên cho quiz của bạn</p>
                    <button @click="switchTab('add-question')" class="btn btn-primary">
                      <i class="bi bi-plus-circle"></i>
                      Thêm câu hỏi
                    </button>
                  </div>

                  <div v-else class="questions-list">
                    <!-- Bulk Select Header -->
                    <div class="bulk-select-header" v-if="questions.length > 1">
                      <label class="checkbox-container">
                        <input 
                          type="checkbox" 
                          :checked="selectedQuestions.length === questions.length"
                          @change="selectAllQuestions"
                        />
                        <span class="checkmark"></span>
                        <span class="label">Chọn tất cả</span>
                      </label>
                    </div>

                    <!-- Question Items -->
                    <div 
                      v-for="(question, index) in filteredQuestions" 
                      :key="question.id"
                      class="question-item"
                      :class="{ 'editing': editingQuestion?.id === question.id }"
                    >
                      <div class="question-header">
                        <div class="question-meta">
                          <label class="checkbox-container">
                            <input 
                              type="checkbox" 
                              :checked="selectedQuestions.includes(question.id)"
                              @change="toggleQuestionSelection(question.id)"
                            />
                            <span class="checkmark"></span>
                          </label>
                          <span class="question-number">Câu {{ index + 1 }}</span>
                          <span class="question-points">{{ question.point }} điểm</span>
                        </div>
                        <div class="question-actions">
                          <button 
                            @click="duplicateQuestion(question)" 
                            class="action-btn"
                            title="Nhân bản"
                          >
                            <i class="bi bi-copy"></i>
                          </button>
                          <button 
                            @click="startEditQuestion(question)" 
                            class="action-btn"
                            title="Chỉnh sửa"
                            v-if="editingQuestion?.id !== question.id"
                          >
                            <i class="bi bi-pencil"></i>
                          </button>
                          <button 
                            @click="cancelEdit" 
                            class="action-btn"
                            title="Huỷ"
                            v-if="editingQuestion?.id === question.id"
                          >
                            <i class="bi bi-x-lg"></i>
                          </button>
                          <button 
                            @click="deleteQuestion(question.id)" 
                            class="action-btn danger"
                            title="Xoá"
                          >
                            <i class="bi bi-trash"></i>
                          </button>
                        </div>
                      </div>

                      <!-- Question Content (Read Mode) -->
                      <div v-if="editingQuestion?.id !== question.id" class="question-content">
                        <div class="question-text">{{ question.content }}</div>
                        <div class="answers-list" v-if="answersMap[question.id]">
                          <div 
                            v-for="answer in answersMap[question.id]" 
                            :key="answer.id"
                            class="answer-item"
                            :class="{ 'correct': answer.correct }"
                          >
                            <div class="answer-indicator">
                              <i v-if="answer.correct" class="bi bi-check-circle-fill"></i>
                              <i v-else class="bi bi-circle"></i>
                            </div>
                            <span class="answer-text">{{ answer.content }}</span>
                          </div>
                        </div>
                      </div>

                      <!-- Question Content (Edit Mode) -->
                      <div v-else class="question-edit-form">
                        <form @submit.prevent="updateQuestion(editingQuestion)">
                          <div class="form-group">
                            <label class="form-label">Nội dung câu hỏi</label>
                            <textarea 
                              class="form-control"
                              :class="{ 'is-invalid': validationErrors.content }"
                              v-model="editingQuestion.content" 
                              rows="3"
                            ></textarea>
                            <div v-if="validationErrors.content" class="invalid-feedback">
                              {{ validationErrors.content }}
                            </div>
                        </div>

                          <div class="form-group">
                            <label class="form-label">Câu trả lời</label>
                            <div 
                              v-for="(answer, i) in answersMap[question.id]" 
                              :key="answer.id"
                              class="answer-input-group"
                            >
                              <div class="input-group">
                                <div class="input-group-text">
                                  <input 
                                    class="form-check-input" 
                                    type="radio" 
                                    :name="'correct-' + question.id"
                                    :checked="answer.correct" 
                                    @change="setCorrectAnswer(question.id, answer.id)" 
                                  />
                                </div>
                                <input 
                                  type="text" 
                                  class="form-control" 
                                  v-model="answer.content"
                                />
                            </div>
                            </div>
                            <div v-if="validationErrors.answers" class="invalid-feedback d-block">
                              {{ validationErrors.answers }}
                            </div>
                          </div>

                          <div class="form-group">
                            <label class="form-label">Điểm</label>
                            <input 
                              type="number" 
                              class="form-control"
                              :class="{ 'is-invalid': validationErrors.point }"
                              v-model="editingQuestion.point" 
                              min="1"
                            />
                            <div v-if="validationErrors.point" class="invalid-feedback">
                              {{ validationErrors.point }}
                            </div>
                          </div>

                          <div class="form-actions">
                            <button type="submit" class="btn btn-primary" :disabled="isSaving">
                              <i class="bi bi-check-lg"></i>
                              Lưu câu hỏi
                            </button>
                            <button 
                              type="button" 
                              @click="updateAnswers(question.id)" 
                              class="btn btn-secondary"
                              :disabled="isSaving"
                            >
                              <i class="bi bi-check-lg"></i>
                                    Lưu câu trả lời
                                </button>
                            <button type="button" @click="cancelEdit" class="btn btn-outline-secondary">
                              <i class="bi bi-x-lg"></i>
                              Huỷ
                            </button>
                          </div>
                        </form>
                      </div>
                    </div>
                  </div>
                </div>
                            </div>
                        </div>

            <!-- Add Question Tab -->
            <div v-if="activeTab === 'add-question'" class="tab-pane active">
              <div class="content-card">
                <div class="card-header">
                  <h3 class="card-title">
                    <i class="bi bi-plus-circle"></i>
                    Thêm câu hỏi mới
                  </h3>
                </div>
                <div class="card-body">
                  <form @submit.prevent="createQuestion" class="question-form">
                    <div class="form-group">
                      <label class="form-label">Nội dung câu hỏi *</label>
                      <textarea 
                        class="form-control"
                        :class="{ 'is-invalid': validationErrors.content }"
                        v-model="newQuestion.content" 
                        rows="4"
                        placeholder="Nhập nội dung câu hỏi..."
                      ></textarea>
                      <div v-if="validationErrors.content" class="invalid-feedback">
                        {{ validationErrors.content }}
                      </div>
                    </div>

                    <div class="form-group">
                      <label class="form-label">Câu trả lời *</label>
                      <div class="answers-input">
                        <div 
                          v-for="(answer, i) in newQuestion.answers" 
                          :key="i" 
                          class="answer-input-group"
                        >
                          <div class="input-group">
                            <div class="input-group-text">
                              <input 
                                class="form-check-input" 
                                type="radio" 
                                name="new-question-correct"
                                :checked="answer.correct" 
                                @change="setNewCorrectAnswer(i)" 
                              />
                            </div>
                            <input 
                              type="text" 
                              class="form-control" 
                              v-model="answer.content"
                              :placeholder="'Câu trả lời ' + (i + 1)"
                            />
                          </div>
                        </div>
                        <div v-if="validationErrors.answers" class="invalid-feedback d-block">
                          {{ validationErrors.answers }}
                        </div>
                        <small class="form-text">Chọn một câu trả lời đúng bằng cách click vào radio button</small>
                      </div>
                        </div>

                    <div class="form-group">
                      <label class="form-label">Điểm *</label>
                      <input 
                        type="number" 
                        class="form-control"
                        :class="{ 'is-invalid': validationErrors.point }"
                        v-model="newQuestion.point" 
                        min="1"
                        placeholder="1"
                      />
                      <div v-if="validationErrors.point" class="invalid-feedback">
                        {{ validationErrors.point }}
                      </div>
                    </div>

                    <div class="form-actions">
                      <button type="submit" class="btn btn-success" :disabled="isSaving">
                        <i class="bi bi-plus-lg" v-if="!isSaving"></i>
                        <i class="bi bi-arrow-clockwise spin" v-else></i>
                        <span>{{ isSaving ? 'Đang thêm...' : 'Thêm câu hỏi' }}</span>
                      </button>
                      <button 
                        type="button" 
                        @click="switchTab('questions')" 
                        class="btn btn-outline-secondary"
                        v-if="questions.length > 0"
                      >
                        <i class="bi bi-list-ul"></i>
                        Xem danh sách
                      </button>
                    </div>
                    </form>
                </div>
              </div>
                </div>
            </div>
        </div>

        <!-- Sidebar -->
        <div class="col-lg-4">
          <div class="sidebar">
            <!-- Quiz Stats -->
            <div class="stats-card">
              <div class="stats-header">
                <h4>Thống kê Quiz</h4>
              </div>
              <div class="stats-body">
                <div class="stat-item">
                  <div class="stat-icon">
                    <i class="bi bi-question-circle"></i>
                  </div>
                  <div class="stat-content">
                    <div class="stat-number">{{ quizStats.totalQuestions }}</div>
                    <div class="stat-label">Câu hỏi</div>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-icon">
                    <i class="bi bi-star"></i>
                  </div>
                  <div class="stat-content">
                    <div class="stat-number">{{ quizStats.totalPoints }}</div>
                    <div class="stat-label">Tổng điểm</div>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-icon">
                    <i class="bi bi-graph-up"></i>
                  </div>
                  <div class="stat-content">
                    <div class="stat-number">{{ quizStats.avgPoints }}</div>
                    <div class="stat-label">Điểm TB/câu</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Quick Actions -->
            <div class="quick-actions-card">
              <div class="card-header">
                <h4>Thao tác nhanh</h4>
              </div>
              <div class="card-body">
                <button @click="switchTab('add-question')" class="quick-action-btn">
                  <i class="bi bi-plus-circle"></i>
                  <span>Thêm câu hỏi</span>
                </button>
                <button @click="previewQuiz" class="quick-action-btn">
                  <i class="bi bi-play-circle"></i>
                  <span>Xem trước Quiz</span>
                </button>
                <button @click="goBack" class="quick-action-btn">
                  <i class="bi bi-arrow-left-circle"></i>
                  <span>Quay lại</span>
                </button>
              </div>
            </div>

            <!-- Tips -->
            <div class="tips-card">
              <div class="card-header">
                <h4>💡 Mẹo hay</h4>
              </div>
              <div class="card-body">
                <ul class="tips-list">
                  <li>Câu hỏi nên rõ ràng, dễ hiểu</li>
                  <li>Mỗi câu hỏi chỉ có 1 đáp án đúng</li>
                  <li>Điểm số phản ánh độ khó của câu hỏi</li>
                  <li>Sử dụng "Xem trước" để kiểm tra quiz</li>
                  <li>Hình ảnh giúp quiz thú vị hơn</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
        </div>
    </div>
</template>

<style scoped>
.edit-quiz-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding-bottom: 40px;
}

/* Header */
.quiz-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  padding: 20px 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.back-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: #667eea;
  color: white;
  transform: translateX(-3px);
}

.quiz-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.quiz-meta {
  display: flex;
  gap: 20px;
  margin-top: 5px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 0.9rem;
  color: #666;
}

.meta-item i {
  font-size: 0.8rem;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 8px;
  border: none;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
}

.action-btn.preview-btn {
  background: linear-gradient(135deg, #28a745, #20c997);
  color: white;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

/* Navigation Tabs */
.nav-tabs-container {
  margin: 30px 0 20px 0;
}

.nav-tabs {
  border: none;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 5px;
  display: flex;
  gap: 5px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: none;
  background: transparent;
  color: #666;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  flex: 1;
  justify-content: center;
}

.nav-link:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.nav-link.active {
  background: #667eea;
  color: white;
  box-shadow: 0 2px 10px rgba(102, 126, 234, 0.3);
}

/* Tab Content */
.tab-content {
  position: relative;
}

.tab-pane {
  display: block;
  opacity: 1;
  transition: all 0.3s ease;
}

.tab-pane.active {
  display: block;
}

/* Content Cards */
.content-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
  margin-bottom: 20px;
}

.card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 25px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-box i {
  position: absolute;
  left: 12px;
  color: #999;
  z-index: 2;
}

.search-box input {
  padding: 8px 12px 8px 35px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  width: 200px;
}

.search-box input::placeholder {
  color: rgba(255, 255, 255, 0.7);
}

.card-body {
  padding: 25px;
}

/* Image Upload Styles */
.image-upload-selector {
  margin-bottom: 15px;
}

.upload-type-tabs {
  display: flex;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 8px;
  padding: 4px;
  gap: 4px;
}

.upload-type-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  border: none;
  background: transparent;
  color: #667eea;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.upload-type-btn.active {
  background: #667eea;
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.upload-type-btn:hover:not(.active) {
  background: rgba(102, 126, 234, 0.2);
}

.image-url-input,
.image-file-input {
  margin-bottom: 15px;
}

.image-preview-container {
  margin-top: 15px;
}

.image-preview {
  position: relative;
  display: inline-block;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.image-preview img {
  width: 200px;
  height: 120px;
  object-fit: cover;
  display: block;
}

.remove-image-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  background: rgba(220, 53, 69, 0.9);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  transition: all 0.3s ease;
}

.remove-image-btn:hover {
  background: #dc3545;
  transform: scale(1.1);
}

/* Forms */
.form-group {
  margin-bottom: 20px;
}

.form-label {
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  display: block;
}

.form-control {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  background: white;
}

.form-control:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-control.is-invalid {
  border-color: #dc3545;
}

.invalid-feedback {
  color: #dc3545;
  font-size: 0.875rem;
  margin-top: 5px;
}

.form-text {
  color: #6c757d;
  font-size: 0.875rem;
  margin-top: 5px;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 25px;
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 8px;
  border: none;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  font-size: 0.95rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.btn-success {
  background: linear-gradient(135deg, #28a745, #20c997);
  color: white;
}

.btn-secondary {
  background: linear-gradient(135deg, #6c757d, #5a6268);
  color: white;
}

.btn-danger {
  background: linear-gradient(135deg, #dc3545, #c82333);
  color: white;
}

.btn-outline-secondary {
  background: transparent;
  color: #6c757d;
  border: 2px solid #6c757d;
}

.btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

/* Questions List */
.loading-state, .empty-state {
  text-align: center;
  padding: 60px 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 4rem;
  color: #ccc;
  margin-bottom: 20px;
}

.bulk-select-header {
  padding: 15px 20px;
  background: rgba(102, 126, 234, 0.05);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.checkbox-container {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}

.checkbox-container input[type="checkbox"] {
  display: none;
}

.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid #ddd;
  border-radius: 4px;
  position: relative;
  transition: all 0.3s ease;
}

.checkbox-container input:checked + .checkmark {
  background: #667eea;
  border-color: #667eea;
}

.checkbox-container input:checked + .checkmark::after {
  content: '✓';
  position: absolute;
  top: -2px;
  left: 2px;
  color: white;
  font-size: 12px;
  font-weight: bold;
}

.question-item {
  border: 2px solid transparent;
  border-radius: 12px;
  margin-bottom: 15px;
  background: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

.question-item:hover {
  border-color: rgba(102, 126, 234, 0.3);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
}

.question-item.editing {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.question-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
  background: rgba(102, 126, 234, 0.05);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.question-meta {
  display: flex;
  align-items: center;
  gap: 15px;
}

.question-number {
  font-weight: 600;
  color: #667eea;
}

.question-points {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 500;
}

.question-actions {
  display: flex;
  gap: 5px;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  border: none;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn.danger {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.action-btn:hover {
  transform: scale(1.1);
}

.question-content {
  padding: 20px;
}

.question-text {
  font-size: 1.1rem;
  font-weight: 500;
  color: #333;
  margin-bottom: 15px;
  line-height: 1.5;
}

.answers-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.answer-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(102, 126, 234, 0.03);
  border-radius: 8px;
  border-left: 3px solid transparent;
}

.answer-item.correct {
  background: rgba(40, 167, 69, 0.1);
  border-left-color: #28a745;
}

.answer-indicator {
  color: #999;
}

.answer-item.correct .answer-indicator {
  color: #28a745;
}

.answer-text {
  flex: 1;
  color: #333;
}

/* Edit Form */
.question-edit-form {
  padding: 20px;
  background: rgba(102, 126, 234, 0.02);
}

.answer-input-group {
  margin-bottom: 10px;
}

.input-group {
  display: flex;
}

.input-group-text {
  background: rgba(102, 126, 234, 0.1);
  border: 2px solid #e1e5e9;
  border-right: none;
  border-radius: 8px 0 0 8px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
}

.input-group .form-control {
  border-radius: 0 8px 8px 0;
  border-left: none;
}

.input-group .form-control:focus {
  border-left: none;
}

/* Sidebar */
.sidebar {
  position: sticky;
  top: 120px;
}

.stats-card, .quick-actions-card, .tips-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 20px;
  overflow: hidden;
}

.stats-header, .quick-actions-card .card-header, .tips-card .card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 15px 20px;
  font-size: 1.1rem;
  font-weight: 600;
}

.stats-body, .quick-actions-card .card-body, .tips-card .card-body {
  padding: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.stat-item:last-child {
  margin-bottom: 0;
}

.stat-icon {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: #333;
  line-height: 1;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
  margin-top: 2px;
}

.quick-action-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px 16px;
  border: none;
  background: rgba(102, 126, 234, 0.05);
  color: #667eea;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 10px;
  font-weight: 500;
}

.quick-action-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: translateX(5px);
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tips-list li {
  padding: 8px 0;
  color: #666;
  font-size: 0.9rem;
  position: relative;
  padding-left: 20px;
}

.tips-list li::before {
  content: '•';
  color: #667eea;
  position: absolute;
  left: 0;
  font-weight: bold;
}

/* Animations */
.spin {
  animation: spin 1s linear infinite;
}

/* Responsive */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .quiz-meta {
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .nav-tabs {
    flex-direction: column;
  }
  
  .nav-link {
    justify-content: flex-start;
  }
  
  .question-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .search-box input {
    width: 150px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .btn {
    justify-content: center;
  }
  
  .upload-type-tabs {
    flex-direction: column;
  }
  
  .image-preview img {
    width: 100%;
    max-width: 300px;
  }
}

@media (max-width: 576px) {
  .quiz-header {
    padding: 15px 0;
  }
  
  .quiz-title {
    font-size: 1.4rem;
  }
  
  .card-body {
    padding: 20px 15px;
  }
  
  .question-content, .question-edit-form {
    padding: 15px;
  }
}
</style>