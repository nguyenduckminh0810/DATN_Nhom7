<template>
    <div class="import-excel-container">
      <div class="import-header">
        <h2>📥 Import Quiz từ Excel</h2>
        <p class="subtitle">Tải lên file Excel để tạo quiz nhanh chóng</p>
      </div>
  
      <!-- Template Download -->
      <div class="template-section">
        <h3>📋 File mẫu</h3>
        <button @click="downloadTemplate" class="template-btn">
          📥 Tải file Excel mẫu
        </button>
        <div class="template-info">
          <p><strong>Cấu trúc file Excel:</strong></p>
          <ul>
            <li>Cột A: STT (1, 2, 3...)</li>
            <li>Cột B: Câu hỏi</li>
            <li>Cột C-F: Đáp án A, B, C, D</li>
            <li>Cột G: Đáp án đúng (A/B/C/D)</li>
            <li>Cột H: Điểm số (mặc định 1)</li>
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
            <input 
              v-model="quizTitle" 
              type="text" 
              placeholder="Nhập tên quiz..."
              required
            />
          </div>
          
          <div class="form-group">
            <label>Mô tả</label>
            <textarea 
              v-model="quizDescription" 
              placeholder="Mô tả ngắn về quiz..."
              rows="3"
            ></textarea>
          </div>
          
          <div class="form-group">
            <label>Danh mục *</label>
            <select v-model="selectedCategory" required>
              <option value="">Chọn danh mục...</option>
              <option 
                v-for="category in categories" 
                :key="category.id" 
                :value="category.id"
              >
                {{ category.name }}
              </option>
            </select>
          </div>
        </div>
  
        <!-- File Upload -->
        <div class="file-upload">
          <div class="upload-area" :class="{ 'drag-over': isDragOver }" 
               @drop="handleDrop" 
               @dragover.prevent="isDragOver = true"
               @dragleave="isDragOver = false">
            
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
          
          <input 
            ref="fileInput"
            type="file" 
            @change="handleFileSelect" 
            accept=".xlsx,.xls"
            style="display: none"
          />
        </div>
  
        <!-- Import Button -->
        <div class="import-actions">
          <button 
            @click="importQuiz" 
            :disabled="!canImport || isImporting"
            class="import-btn"
          >
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
  
  // State
  const quizTitle = ref('')
  const quizDescription = ref('')
  const selectedCategory = ref('')
  const categories = ref([])
  const selectedFile = ref(null)
  const isDragOver = ref(false)
  const isImporting = ref(false)
  const importResult = ref(null)
  
  // Computed
  const canImport = computed(() => {
    return quizTitle.value.trim() && 
           selectedCategory.value && 
           selectedFile.value
  })
  
  // Methods
  const fetchCategories = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/categories')
      categories.value = response.data
    } catch (error) {
      console.error('Lỗi tải categories:', error)
    }
  }
  
  const downloadTemplate = () => {
    // Tạo file Excel mẫu
    const link = document.createElement('a')
    link.href = '/templates/quiz-template.xlsx'
    link.download = 'quiz-template.xlsx'
    link.click()
  }
  
  const handleFileSelect = (event) => {
    const file = event.target.files[0]
    if (file) {
      selectedFile.value = file
      importResult.value = null
    }
  }
  
  const handleDrop = (event) => {
    event.preventDefault()
    isDragOver.value = false
    
    const files = event.dataTransfer.files
    if (files.length > 0) {
      selectedFile.value = files[0]
      importResult.value = null
    }
  }
  
  const removeFile = () => {
    selectedFile.value = null
    importResult.value = null
  }
  
  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }
  
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
      
      const response = await axios.post(
        'http://localhost:8080/api/quiz/import-excel',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        }
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
        message: error.response?.data?.message || 'Có lỗi xảy ra khi import'
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
    importResult.value = null
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
  
  .template-section, .import-form {
    background: white;
    border-radius: 12px;
    padding: 25px;
    margin-bottom: 25px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  }
  
  .template-btn {
    background: #3498db;
    color: white;
    border: none;
    padding: 12px 24px;
    border-radius: 8px;
    cursor: pointer;
    font-size: 14px;
    margin-bottom: 15px;
  }
  
  .template-btn:hover {
    background: #2980b9;
  }
  
  .template-info ul {
    list-style-type: none;
    padding-left: 0;
  }
  
  .template-info li {
    padding: 5px 0;
    border-left: 3px solid #3498db;
    padding-left: 12px;
    margin-bottom: 5px;
  }
  
  .form-group {
    margin-bottom: 20px;
  }
  
  .form-group label {
    display: block;
    margin-bottom: 8px;
    font-weight: 600;
    color: #2c3e50;
  }
  
  .form-group input,
  .form-group textarea,
  .form-group select {
    width: 100%;
    padding: 12px;
    border: 2px solid #e0e0e0;
    border-radius: 8px;
    font-size: 14px;
    transition: border-color 0.3s;
  }
  
  .form-group input:focus,
  .form-group textarea:focus,
  .form-group select:focus {
    outline: none;
    border-color: #3498db;
  }
  
  .upload-area {
    border: 2px dashed #bdc3c7;
    border-radius: 12px;
    padding: 40px;
    text-align: center;
    transition: all 0.3s;
    cursor: pointer;
  }
  
  .upload-area.drag-over {
    border-color: #3498db;
    background-color: #f8f9fa;
  }
  
  .upload-placeholder i {
    font-size: 48px;
    color: #bdc3c7;
    margin-bottom: 15px;
  }
  
  .select-file-btn {
    background: #2ecc71;
    color: white;
    border: none;
    padding: 12px 24px;
    border-radius: 8px;
    cursor: pointer;
    font-size: 14px;
    margin-top: 10px;
  }
  
  .select-file-btn:hover {
    background: #27ae60;
  }
  
  .file-selected {
    display: flex;
    align-items: center;
    gap: 15px;
    padding: 15px;
    background: #f8f9fa;
    border-radius: 8px;
  }
  
  .file-selected i {
    font-size: 32px;
    color: #27ae60;
  }
  
  .file-info {
    flex: 1;
    text-align: left;
  }
  
  .file-name {
    font-weight: 600;
    margin-bottom: 5px;
  }
  
  .file-size {
    color: #7f8c8d;
    font-size: 14px;
  }
  
  .remove-file {
    background: none;
    border: none;
    color: #e74c3c;
    font-size: 20px;
    cursor: pointer;
    padding: 5px;
  }
  
  .remove-file:hover {
    color: #c0392b;
  }
  
  .import-btn {
    width: 100%;
    background: #e74c3c;
    color: white;
    border: none;
    padding: 15px;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: background-color 0.3s;
  }
  
  .import-btn:hover:not(:disabled) {
    background: #c0392b;
  }
  
  .import-btn:disabled {
    background: #bdc3c7;
    cursor: not-allowed;
  }
  
  .import-result {
    display: flex;
    align-items: center;
    gap: 15px;
    padding: 20px;
    border-radius: 12px;
    margin-top: 20px;
  }
  
  .import-result.success {
    background: #d4edda;
    border: 1px solid #c3e6cb;
    color: #155724;
  }
  
  .import-result.error {
    background: #f8d7da;
    border: 1px solid #f5c6cb;
    color: #721c24;
  }
  
  .result-icon i {
    font-size: 24px;
  }
  
  .result-content h4 {
    margin: 0 0 8px 0;
  }
  
  .result-content p {
    margin: 0 0 10px 0;
  }
  
  .stats {
    font-weight: 600;
  }
  
  .spin {
    animation: spin 1s linear infinite;
  }
  
  @keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }
  </style>