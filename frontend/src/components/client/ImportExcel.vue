<template>
  <div class="import-excel-container">
    <div class="import-header">
      <h2>📥 Import Quiz từ Excel</h2>
      <p class="subtitle">Tải lên file Excel để tạo quiz nhanh chóng</p>

      <!-- ✅ THÔNG BÁO TRẠNG THÁI BACKEND -->
      <div v-if="categories.length <= 3" class="backend-status warning">
        ⚠️ Backend có thể chưa chạy. Vui lòng khởi động backend trước!
      </div>
      <div v-else class="backend-status success">✅ Backend đã sẵn sàng</div>
    </div>

    <!-- Template Download -->
    <div class="template-section">
      <h3>📋 File mẫu</h3>
      <button @click="downloadTemplate" class="template-btn">📥 Tải file Excel mẫu</button>
      <div class="template-info">
        <p><strong>Cấu trúc file Excel:</strong></p>
        <ul>
          <li>Cột A: STT (1, 2, 3...)</li>
          <li>Cột B: Câu hỏi</li>
          <li>Cột C-F: Đáp án A, B, C, D</li>
          <li>Cột G: Đáp án đúng (A/B/C/D)</li>
          <li>Cột H: Thời gian (giây) - mặc định 30s, range 5-300s</li>
        </ul>
      </div>
    </div>

    <!-- Import Form -->
    <div class="import-form">
      <h3>📤 Import Quiz</h3>

      <!-- Quiz Info -->
      <div class="quiz-info">
        <div class="form-group">
          <label>Tên Quiz *</label>
          <input v-model="quizTitle" type="text" placeholder="Nhập tên quiz..." required />
        </div>

        <div class="form-group">
          <label>Mô tả</label>
          <textarea v-model="quizDescription" placeholder="Mô tả ngắn về quiz..." rows="3"></textarea>
        </div>

        <div class="form-group">
          <label>Danh mục *</label>
          <select v-model="selectedCategory" required>
            <option value="">Chọn danh mục...</option>
            <option v-for="category in categories" :key="category.id" :value="category.id">
              {{ category.name }}
            </option>
          </select>
        </div>

        <!-- ✅ THÊM IMAGE UPLOAD -->
        <div class="form-group">
          <label>Ảnh chủ đề (tùy chọn)</label>
          <div class="image-upload-area">
            <!-- Trạng thái chưa chọn ảnh -->
            <div v-if="!selectedImage" class="image-placeholder">
              <i class="bi bi-image"></i>
              <p>Chọn ảnh chủ đề</p>
              <button type="button" @click="$refs.imageInput.click()" class="select-image-btn">
                Chọn ảnh
              </button>
            </div>

            <!-- Trạng thái đã chọn ảnh -->
            <div v-else class="image-selected">
              <img :src="imagePreview" alt="Preview" class="image-preview" />
              <button @click="removeImage" class="remove-image">
                <i class="bi bi-x-circle"></i>
              </button>
            </div>

            <!-- Hidden file input -->
            <input ref="imageInput" type="file" @change="handleImageSelect" accept="image/*" style="display: none" />
          </div>
          <small class="image-help">Hỗ trợ: JPG, PNG, GIF, WebP. Tối đa 5MB</small>
        </div>
      </div>

      <!-- File Upload -->
      <div class="file-upload">
        <div class="upload-area" :class="{ 'drag-over': isDragOver }" @drop="handleDrop"
          @dragover.prevent="isDragOver = true" @dragleave="isDragOver = false">
          <div v-if="!selectedFile" class="upload-placeholder">
            <i class="bi bi-cloud-upload"></i>
            <p>Kéo thả file Excel vào đây</p>
            <p>hoặc</p>
            <button type="button" @click="$refs.fileInput.click()" class="select-file-btn">
              Chọn file
            </button>
          </div>

          <div v-else class="file-selected">
            <i class="bi bi-file-earmark-excel"></i>
            <div class="file-info">
              <p class="file-name">{{ selectedFile.name }}</p>
              <p class="file-size">{{ formatFileSize(selectedFile.size) }}</p>
            </div>
            <button @click="removeFile" class="remove-file">
              <i class="bi bi-x-circle"></i>
            </button>
          </div>
        </div>

        <input ref="fileInput" type="file" @change="handleFileSelect" accept=".xlsx,.xls" style="display: none" />
      </div>

      <!-- Import Button -->
      <div class="import-actions">
        <button @click="importQuiz" :disabled="!canImport || isImporting" class="import-btn">
          <span v-if="isImporting">
            <i class="bi bi-arrow-repeat spin"></i>
            Đang import...
          </span>
          <span v-else>
            <i class="bi bi-upload"></i>
            Import Quiz
          </span>
        </button>
      </div>
    </div>

    <!-- ✅ PREVIEW SECTION -->
    <div v-if="showPreview && previewData" class="preview-section">
      <h3>📋 Bản xem trước</h3>

      <!-- Thống kê -->
      <div class="preview-stats">
        <div class="stat-item">
          <span class="stat-label">Tổng câu hỏi:</span>
          <span class="stat-value">{{ previewData.totalQuestions }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">Thời gian trung bình:</span>
          <span class="stat-value">{{ getAverageTime() }}s</span>
        </div>
        <div v-if="selectedImage" class="stat-item">
          <span class="stat-label">Ảnh chủ đề:</span>
          <span class="stat-value">✅ Có</span>
        </div>
      </div>

      <!-- Preview ảnh chủ đề -->
      <div v-if="selectedImage" class="preview-topic-image">
        <h4>Ảnh chủ đề:</h4>
        <img :src="imagePreview" alt="Topic Image" class="topic-image-preview" />
      </div>

      <!-- Preview câu hỏi -->
      <div class="preview-questions">
        <h4>3 câu hỏi đầu tiên:</h4>
        <div v-for="(question, index) in previewData.previewQuestions" :key="index" class="question-preview">
          <div class="question-header">
            <span class="question-number">Câu {{ index + 1 }}:</span>
            <span class="question-time">
              <i class="bi bi-clock"></i>
              {{ question.timeLimit || 30 }}s
            </span>
          </div>
          <p class="question-content">{{ question.content }}</p>
          <div class="answers-preview">
            <div v-for="(answer, ansIndex) in question.answers" :key="ansIndex"
              :class="['answer-item', answer.correct ? 'correct' : '']">
              <span class="answer-label">{{ String.fromCharCode(65 + ansIndex) }}:</span>
              <span class="answer-content">{{ answer.content }}</span>
              <span v-if="answer.correct" class="correct-mark">✓</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="preview-actions">
        <button @click="confirmImport" class="btn-confirm">
          <i class="bi bi-check-circle"></i>
          Xác nhận Import
        </button>
        <button @click="cancelPreview" class="btn-cancel">
          <i class="bi bi-x-circle"></i>
          Hủy bỏ
        </button>
      </div>
    </div>

    <!-- Progress & Results -->
    <div v-if="importResult" class="import-result" :class="importResult.success ? 'success' : 'error'">
      <div class="result-icon">
        <i :class="importResult.success ? 'bi bi-check-circle' : 'bi bi-x-circle'"></i>
      </div>
      <div class="result-content">
        <h4>{{ importResult.success ? 'Import thành công!' : 'Import thất bại!' }}</h4>
        <p>{{ importResult.message }}</p>
        <div v-if="importResult.success && importResult.questionsCount" class="stats">
          <span>📊 Đã tạo {{ importResult.questionsCount }} câu hỏi</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import api from '@/utils/axios'
// State
const quizTitle = ref('')
const quizDescription = ref('')
const selectedCategory = ref('')
const categories = ref([])
const selectedFile = ref(null)
const isDragOver = ref(false)
const isImporting = ref(false)
const importResult = ref(null)

// ✅ THÊM STATE CHO IMAGE UPLOAD
const selectedImage = ref(null)
const imagePreview = ref(null)

// ✅ THÊM STATE CHO PREVIEW
const previewData = ref(null)
const showPreview = ref(false)

// ✅ DEBUG LOG
console.log('🔍 ImportExcel component loaded')
console.log('📸 selectedImage:', selectedImage.value)
console.log('🖼️ imagePreview:', imagePreview.value)

// Computed
const canImport = computed(() => {
  return quizTitle.value.trim() && selectedCategory.value && selectedFile.value
})

// Methods
const fetchCategories = async () => {
  try {
    const response = await api.get('/categories')
    categories.value = response.data
    console.log('✅ Categories loaded:', categories.value.length)
  } catch (error) {
    console.error('❌ Lỗi tải categories (Backend chưa chạy?):', error)
    // Thêm categories mặc định để test UI
    categories.value = [
      { id: 1, name: 'Toán học' },
      { id: 2, name: 'Tiếng Anh' },
      { id: 3, name: 'Lịch sử' },
    ]
  }
}

const downloadTemplate = () => {
  // Tạo file Excel mẫu
  const link = document.createElement('a')
  link.href = '/templates/quiz-template.xlsx'
  link.download = 'quiz-template.xlsx'
  link.click()
}

// ✅ THÊM METHODS CHO IMAGE UPLOAD
const handleImageSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    // Validate file type
    if (!file.type.startsWith('image/')) {
      alert('Vui lòng chọn file ảnh!')
      return
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      alert('File ảnh quá lớn! Tối đa 5MB.')
      return
    }

    selectedImage.value = file
    imagePreview.value = URL.createObjectURL(file)
    console.log('✅ Image selected:', file.name)
  }
}

const removeImage = () => {
  selectedImage.value = null
  imagePreview.value = null
  console.log('❌ Image removed')
}

// ✅ THÊM METHOD TÍNH THỜI GIAN TRUNG BÌNH
const getAverageTime = () => {
  if (!previewData.value || !previewData.value.previewQuestions) {
    return 30
  }

  const totalTime = previewData.value.previewQuestions.reduce((sum, question) => {
    return sum + (question.timeLimit || 30)
  }, 0)

  return Math.round(totalTime / previewData.value.previewQuestions.length)
}

// ✅ THÊM METHODS CHO PREVIEW
const previewFile = async () => {
  if (!selectedFile.value) return

  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('title', quizTitle.value.trim())
    formData.append('description', quizDescription.value.trim())
    formData.append('categoryId', selectedCategory.value)

    const response = await api.post('/quiz/preview-excel', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        Authorization: `Bearer ${localStorage.getItem('token')}`,
      },
    })

    if (response.data.success) {
      previewData.value = response.data
      showPreview.value = true
    } else {
      alert('Lỗi preview: ' + response.data.message)
    }
  } catch (error) {
    alert('Lỗi preview: ' + error.message)
  }
}

const confirmImport = async () => {
  await importQuiz()
  showPreview.value = false
  previewData.value = null
}

const cancelPreview = () => {
  showPreview.value = false
  previewData.value = null
}

// ✅ CẬP NHẬT HANDLEFILESELECT
const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    selectedFile.value = file
    importResult.value = null
    previewFile() // ✅ TỰ ĐỘNG PREVIEW
  }
}

const handleDrop = (event) => {
  event.preventDefault()
  isDragOver.value = false

  const files = event.dataTransfer.files
  if (files.length > 0) {
    selectedFile.value = files[0]
    importResult.value = null
    previewFile() // ✅ TỰ ĐỘNG PREVIEW
  }
}

const removeFile = () => {
  selectedFile.value = null
  importResult.value = null
  showPreview.value = false
  previewData.value = null
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// ✅ CẬP NHẬT IMPORTQUIZ ĐỂ THÊM IMAGE
const importQuiz = async () => {
  if (!canImport.value) return

  isImporting.value = true
  importResult.value = null

  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('title', quizTitle.value.trim())
    formData.append('description', quizDescription.value.trim())
    formData.append('categoryId', selectedCategory.value)
    formData.append('username', localStorage.getItem('username'))

    // ✅ THÊM IMAGE VÀO FORMDATA
    if (selectedImage.value) {
      formData.append('image', selectedImage.value)
      console.log('📸 Adding image to import:', selectedImage.value.name)
    }

    const response = await api.post(
      'http://localhost:8080/api/quiz/import-excel-with-image',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
      },
    )

    importResult.value = response.data

    if (response.data.success) {
      // Reset form sau khi thành công
      setTimeout(() => {
        resetForm()
      }, 3000)
    }
  } catch (error) {
    importResult.value = {
      success: false,
      message: error.response?.data?.message || 'Có lỗi xảy ra khi import',
    }
  } finally {
    isImporting.value = false
  }
}

const resetForm = () => {
  quizTitle.value = ''
  quizDescription.value = ''
  selectedCategory.value = ''
  selectedFile.value = null
  selectedImage.value = null
  imagePreview.value = null
  importResult.value = null
  showPreview.value = false
  previewData.value = null
}

// Lifecycle
onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.import-excel-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.import-header {
  text-align: center;
  margin-bottom: 30px;
}

.import-header h2 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.subtitle {
  color: #7f8c8d;
  font-size: 16px;
}

.template-section,
.import-form {
  background: white;
  border-radius: 12px;
  padding: 25px;
  margin-bottom: 25px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.template-section h3,
.import-form h3 {
  color: #2c3e50;
  margin-bottom: 20px;
  font-size: 18px;
}

.template-btn {
  background: #3498db;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
  margin-bottom: 15px;
  transition: background 0.3s;
}

.template-btn:hover {
  background: #2980b9;
}

.template-info {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid #3498db;
}

.template-info p {
  margin-bottom: 10px;
  font-weight: bold;
  color: #2c3e50;
}

.template-info ul {
  margin: 0;
  padding-left: 20px;
}

.template-info li {
  margin-bottom: 5px;
  color: #495057;
}

.quiz-info {
  margin-bottom: 25px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #2c3e50;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3498db;
}

/* ✅ THÊM CSS CHO IMAGE UPLOAD */
.image-upload-area {
  border: 2px dashed #bdc3c7;
  border-radius: 12px;
  padding: 30px;
  text-align: center;
  transition: all 0.3s;
  background: #f8f9fa;
  min-height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-upload-area:hover {
  border-color: #3498db;
  background: #ebf3fd;
}

.image-placeholder {
  color: #7f8c8d;
}

.image-placeholder i {
  font-size: 48px;
  margin-bottom: 15px;
  color: #bdc3c7;
}

.image-placeholder p {
  margin: 10px 0;
  font-size: 16px;
}

.select-image-btn {
  background: #3498db;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
  margin-top: 15px;
  transition: background 0.3s;
}

.select-image-btn:hover {
  background: #2980b9;
}

.image-selected {
  position: relative;
  display: inline-block;
}

.image-preview {
  max-width: 200px;
  max-height: 150px;
  border-radius: 8px;
  object-fit: cover;
}

.remove-image {
  position: absolute;
  top: -10px;
  right: -10px;
  background: #e74c3c;
  color: white;
  border: none;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: background 0.3s;
}

.remove-image:hover {
  background: #c0392b;
}

.image-help {
  display: block;
  margin-top: 8px;
  color: #6c757d;
  font-size: 12px;
}

.file-upload {
  margin-bottom: 25px;
}

.upload-area {
  border: 2px dashed #bdc3c7;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  transition: all 0.3s;
  background: #f8f9fa;
}

.upload-area.drag-over {
  border-color: #3498db;
  background: #ebf3fd;
}

.upload-placeholder {
  color: #7f8c8d;
}

.upload-placeholder i {
  font-size: 48px;
  margin-bottom: 15px;
  color: #bdc3c7;
}

.upload-placeholder p {
  margin: 10px 0;
  font-size: 16px;
}

.select-file-btn {
  background: #3498db;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
  margin-top: 15px;
  transition: background 0.3s;
}

.select-file-btn:hover {
  background: #2980b9;
}

.file-selected {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  padding: 20px;
  border-radius: 8px;
  border: 2px solid #27ae60;
}

.file-selected i {
  font-size: 32px;
  color: #27ae60;
}

.file-info {
  flex: 1;
  margin-left: 15px;
}

.file-name {
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 5px;
}

.file-size {
  color: #7f8c8d;
  font-size: 14px;
}

.remove-file {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.3s;
}

.remove-file:hover {
  background: #c0392b;
}

.import-actions {
  text-align: center;
}

.import-btn {
  background: #27ae60;
  color: white;
  border: none;
  padding: 15px 30px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.import-btn:hover:not(:disabled) {
  background: #229954;
  transform: translateY(-2px);
}

.import-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  transform: none;
}

.import-btn .spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

.import-result {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 12px;
  margin-top: 25px;
  animation: slideIn 0.3s ease-out;
}

.import-result.success {
  background: #d4edda;
  border: 2px solid #c3e6cb;
  color: #155724;
}

.import-result.error {
  background: #f8d7da;
  border: 2px solid #f5c6cb;
  color: #721c24;
}

.result-icon {
  margin-right: 15px;
  font-size: 24px;
}

.result-content h4 {
  margin: 0 0 8px 0;
  font-size: 18px;
}

.result-content p {
  margin: 0 0 10px 0;
}

.stats {
  font-size: 14px;
  opacity: 0.8;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ✅ THÊM CSS CHO PREVIEW */
.preview-section {
  background: white;
  border-radius: 12px;
  padding: 25px;
  margin-bottom: 25px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.preview-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-label {
  font-size: 12px;
  color: #6c757d;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #2c3e50;
}

/* ✅ THÊM CSS CHO PREVIEW IMAGE */
.preview-topic-image {
  margin-bottom: 20px;
}

.preview-topic-image h4 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 16px;
}

.topic-image-preview {
  max-width: 200px;
  max-height: 150px;
  border-radius: 8px;
  object-fit: cover;
  border: 2px solid #e9ecef;
}

.preview-questions {
  margin-bottom: 20px;
}

.preview-questions h4 {
  color: #2c3e50;
  margin-bottom: 15px;
  font-size: 16px;
}

.question-preview {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.question-number {
  font-weight: bold;
  color: #2c3e50;
}

.question-point {
  color: #6c757d;
  font-size: 14px;
}

.question-time {
  color: #007bff;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.question-time i {
  font-size: 12px;
  color: #6c757d;
}

.question-content {
  margin-bottom: 10px;
  color: #495057;
}

.answers-preview {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.answer-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  background: #f8f9fa;
}

.answer-item.correct {
  background: #d4edda;
  border-color: #c3e6cb;
}

.answer-label {
  font-weight: bold;
  margin-right: 8px;
  color: #495057;
}

.answer-content {
  flex: 1;
  color: #495057;
}

.correct-mark {
  color: #28a745;
  font-weight: bold;
  margin-left: 8px;
}

.preview-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.btn-confirm,
.btn-cancel {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-confirm {
  background: #28a745;
  color: white;
}

.btn-confirm:hover {
  background: #218838;
}

.btn-cancel {
  background: #6c757d;
  color: white;
}

.btn-cancel:hover {
  background: #5a6268;
}

/* ✅ THÊM CSS CHO THÔNG BÁO TRẠNG THÁI BACKEND */
.backend-status {
  margin-top: 15px;
  padding: 10px 15px;
  border-radius: 8px;
  font-weight: bold;
  font-size: 14px;
  text-align: center;
}

.backend-status.warning {
  background-color: #fff3cd;
  color: #856404;
  border: 1px solid #ffeeba;
}

.backend-status.success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}
</style>
