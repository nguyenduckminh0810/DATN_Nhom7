<template>
  <div class="category-manager-container">
      <!-- Background Elements -->
      <div class="background-elements">
          <div class="floating-element element-1"></div>
          <div class="floating-element element-2"></div>
          <div class="floating-element element-3"></div>
      </div>

      <!-- Hero Section -->
      <div class="hero-section">
          <div class="hero-content">
              <div class="hero-icon">
                  <i class="bi bi-tags-fill"></i>
              </div>
              <h1 class="hero-title">Quản lý danh mục</h1>
              <p class="hero-subtitle">Tổ chức và quản lý các danh mục quiz một cách hiệu quả</p>
          </div>
      </div>

      <!-- Stats Overview -->
      <div class="stats-section">
          <div class="stats-grid">
              <div class="stat-card primary">
                  <div class="stat-icon">
                      <i class="bi bi-collection"></i>
                  </div>
                  <div class="stat-content">
                      <div class="stat-value">{{ categories.length }}</div>
                      <div class="stat-label">Tổng danh mục</div>
                  </div>
              </div>
              
              <div class="stat-card success">
                  <div class="stat-icon">
                      <i class="bi bi-plus-circle"></i>
                  </div>
                  <div class="stat-content">
                      <div class="stat-value">{{ recentCategories }}</div>
                      <div class="stat-label">Thêm tuần này</div>
                  </div>
              </div>
              
              <div class="stat-card info">
                  <div class="stat-icon">
                      <i class="bi bi-graph-up"></i>
                  </div>
                  <div class="stat-content">
                      <div class="stat-value">{{ mostPopularCategory }}</div>
                      <div class="stat-label">Phổ biến nhất</div>
                  </div>
              </div>
          </div>
      </div>

      <!-- Main Content -->
      <div class="main-content">
          <!-- Add Category Form (Admin Only) -->
          <div v-if="isAdmin" class="add-category-section">
              <div class="form-card">
                  <div class="form-header">
                      <h2 class="form-title">
                          <i class="bi bi-plus-circle"></i>
                          Thêm danh mục mới
                      </h2>
                      <p class="form-subtitle">Tạo danh mục mới để phân loại quiz</p>
                  </div>
                  
                  <form @submit.prevent="addCategory" class="category-form">
                      <div class="form-row">
                          <div class="form-group">
                              <label for="categoryName" class="form-label">
                                  <i class="bi bi-tag"></i>
                                  <span>Tên danh mục</span>
                                  <span class="required">*</span>
                              </label>
                              <input 
                                  type="text" 
                                  id="categoryName"
                                  v-model="newCategory.name" 
                                  class="form-input"
                                  :class="{ 'error': nameError, 'success': newCategory.name && !nameError }"
                                  placeholder="Nhập tên danh mục..."
                                  maxlength="50"
                                  required
                              />
                              <div v-if="nameError" class="error-message">{{ nameError }}</div>
                              <div class="char-count">{{ newCategory.name.length }}/50</div>
                          </div>
                          
                          <div class="form-group">
                              <label for="categoryDesc" class="form-label">
                                  <i class="bi bi-text-paragraph"></i>
                                  <span>Mô tả</span>
                              </label>
                              <textarea 
                                  id="categoryDesc"
                                  v-model="newCategory.description" 
                                  class="form-textarea"
                                  :class="{ 'success': newCategory.description }"
                                  placeholder="Mô tả chi tiết về danh mục..."
                                  rows="3"
                                  maxlength="200"
                              ></textarea>
                              <div class="char-count">{{ newCategory.description.length }}/200</div>
                          </div>
                      </div>
                      
                      <div class="form-actions">
                          <button type="button" @click="clearForm" class="btn-secondary">
                              <i class="bi bi-x-circle"></i>
                              Xóa form
                          </button>
                          <button type="submit" class="btn-primary" :disabled="isAdding || nameError">
                              <div v-if="isAdding" class="loading-spinner">
                                  <i class="bi bi-arrow-clockwise spin"></i>
                              </div>
                              <i v-else class="bi bi-plus-lg"></i>
                              {{ isAdding ? 'Đang thêm...' : 'Thêm danh mục' }}
                          </button>
                      </div>
                  </form>
              </div>
          </div>

          <!-- Categories Table -->
          <div class="table-section">
              <div class="table-card">
                  <div class="table-header">
                      <div class="header-content">
                          <h2 class="table-title">
                              <i class="bi bi-list-ul"></i>
                              Danh sách danh mục
                          </h2>
                          <p class="table-subtitle">{{ filteredCategories.length }} danh mục</p>
                      </div>
                      
                      <div class="table-controls">
                          <div class="search-box">
                              <i class="bi bi-search"></i>
                              <input 
                                  v-model="searchTerm" 
                                  type="text" 
                                  placeholder="Tìm kiếm danh mục..."
                                  class="search-input"
                              />
                          </div>
                          <select v-model="sortBy" class="sort-select">
                              <option value="name">Tên A-Z</option>
                              <option value="nameDesc">Tên Z-A</option>
                              <option value="newest">Mới nhất</option>
                              <option value="oldest">Cũ nhất</option>
                          </select>
                      </div>
                  </div>
                  
                  <div class="table-container">
                      <div v-if="isLoading" class="loading-state">
                          <div class="loading-spinner">
                              <i class="bi bi-arrow-clockwise spin"></i>
                          </div>
                          <p>Đang tải danh mục...</p>
                      </div>
                      
                      <div v-else-if="filteredCategories.length === 0" class="empty-state">
                          <div class="empty-icon">
                              <i class="bi bi-inbox"></i>
                          </div>
                          <h3>{{ searchTerm ? 'Không tìm thấy danh mục' : 'Chưa có danh mục nào' }}</h3>
                          <p>{{ searchTerm ? 'Thử từ khóa khác' : 'Hãy thêm danh mục đầu tiên' }}</p>
                      </div>
                      
                      <div v-else class="categories-grid">
                          <div 
                              v-for="(category, index) in filteredCategories" 
                              :key="category.id"
                              class="category-card"
                              :class="{ 'editing': editId === category.id }"
                              :style="{ 'animation-delay': `${index * 0.1}s` }"
                          >
                              <div class="card-header">
                                  <div class="category-info">
                                      <div v-if="editId !== category.id" class="category-name">
                                          {{ category.name }}
                                      </div>
                                      <input 
                                          v-else 
                                          v-model="editCategory.name" 
                                          class="edit-input"
                                          placeholder="Tên danh mục..."
                                          maxlength="50"
                                      />
                                      
                                      <div class="category-meta">
                                          <span class="date-badge">
                                              <i class="bi bi-calendar3"></i>
                                              {{ formatDate(category.createdAt) }}
                                          </span>
                                          <span class="id-badge">
                                              ID: {{ category.id }}
                                          </span>
                                      </div>
                                  </div>
                                  
                                  <div v-if="isAdmin" class="card-actions">
                                      <button 
                                          v-if="editId !== category.id" 
                                          @click="startEdit(category)"
                                          class="btn-edit"
                                          title="Chỉnh sửa"
                                      >
                                          <i class="bi bi-pencil"></i>
                                      </button>
                                      <button 
                                          v-else 
                                          @click="saveEdit(category.id)"
                                          class="btn-save"
                                          title="Lưu"
                                          :disabled="isSaving"
                                      >
                                          <i v-if="isSaving" class="bi bi-arrow-clockwise spin"></i>
                                          <i v-else class="bi bi-check-lg"></i>
                                      </button>
                                      
                                      <button 
                                          v-if="editId !== category.id"
                                          @click="confirmDelete(category)"
                                          class="btn-delete"
                                          title="Xóa"
                                      >
                                          <i class="bi bi-trash"></i>
                                      </button>
                                      <button 
                                          v-else
                                          @click="cancelEdit"
                                          class="btn-cancel"
                                          title="Hủy"
                                      >
                                          <i class="bi bi-x-lg"></i>
                                      </button>
                                  </div>
                              </div>
                              
                              <div class="card-body">
                                  <div v-if="editId !== category.id" class="category-description">
                                      {{ category.description || 'Chưa có mô tả' }}
                                  </div>
                                  <textarea 
                                      v-else 
                                      v-model="editCategory.description" 
                                      class="edit-textarea"
                                      placeholder="Mô tả danh mục..."
                                      rows="2"
                                      maxlength="200"
                                  ></textarea>
                              </div>
                          </div>
                      </div>
                  </div>
              </div>
          </div>
      </div>

      <!-- Delete Confirmation Modal -->
      <div v-if="showDeleteModal" class="modal-overlay" @click="closeDeleteModal">
          <div class="delete-modal" @click.stop>
              <div class="modal-header">
                  <div class="modal-icon">
                      <i class="bi bi-exclamation-triangle"></i>
                  </div>
                  <h3 class="modal-title">Xác nhận xóa</h3>
              </div>
              
              <div class="modal-body">
                  <p>Bạn có chắc chắn muốn xóa danh mục <strong>"{{ categoryToDelete?.name }}"</strong>?</p>
                  <p class="warning-text">Hành động này không thể hoàn tác!</p>
              </div>
              
              <div class="modal-actions">
                  <button @click="closeDeleteModal" class="btn-cancel-modal">
                      <i class="bi bi-x-circle"></i>
                      Hủy
                  </button>
                  <button @click="deleteCategory" class="btn-confirm-delete" :disabled="isDeleting">
                      <i v-if="isDeleting" class="bi bi-arrow-clockwise spin"></i>
                      <i v-else class="bi bi-trash"></i>
                      {{ isDeleting ? 'Đang xóa...' : 'Xóa' }}
                  </button>
              </div>
          </div>
      </div>

      <!-- Success Toast -->
      <div v-if="toast.show" class="toast" :class="toast.type">
          <div class="toast-content">
              <i :class="toast.icon"></i>
              <span>{{ toast.message }}</span>
          </div>
          <button @click="hideToast" class="toast-close">
              <i class="bi bi-x"></i>
          </button>
      </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import axios from 'axios'
import { useUserStore } from '@/stores/user'

const categories = ref([])
const newCategory = ref({ name: '', description: '' })
const editId = ref(null)
const editCategory = ref({ name: '', description: '' })
const searchTerm = ref('')
const sortBy = ref('name')
const isLoading = ref(true)
const isAdding = ref(false)
const isSaving = ref(false)
const isDeleting = ref(false)
const showDeleteModal = ref(false)
const categoryToDelete = ref(null)

const userStore = useUserStore()
const isAdmin = userStore.isAdmin()

const toast = ref({
  show: false,
  type: 'success',
  message: '',
  icon: ''
})

// Validation
const nameError = computed(() => {
  if (!newCategory.value.name) return ''
  if (newCategory.value.name.length < 2) return 'Tên danh mục phải có ít nhất 2 ký tự'
  if (categories.value.some(cat => cat.name.toLowerCase() === newCategory.value.name.toLowerCase())) {
      return 'Tên danh mục đã tồn tại'
  }
  return ''
})

// Computed properties
const filteredCategories = computed(() => {
  let filtered = [...categories.value]
  
  // Search filter
  if (searchTerm.value) {
      filtered = filtered.filter(cat => 
          cat.name.toLowerCase().includes(searchTerm.value.toLowerCase()) ||
          cat.description?.toLowerCase().includes(searchTerm.value.toLowerCase())
      )
  }
  
  // Sort
  filtered.sort((a, b) => {
      switch(sortBy.value) {
          case 'name':
              return a.name.localeCompare(b.name)
          case 'nameDesc':
              return b.name.localeCompare(a.name)
          case 'newest':
              return new Date(b.createdAt) - new Date(a.createdAt)
          case 'oldest':
              return new Date(a.createdAt) - new Date(b.createdAt)
          default:
              return 0
      }
  })
  
  return filtered
})

const recentCategories = computed(() => {
  const oneWeekAgo = new Date()
  oneWeekAgo.setDate(oneWeekAgo.getDate() - 7)
  return categories.value.filter(cat => new Date(cat.createdAt) > oneWeekAgo).length
})

const mostPopularCategory = computed(() => {
  if (categories.value.length === 0) return 'N/A'
  // For now, just return the first category name
  // In real app, this would be based on quiz count per category
  return categories.value[0]?.name || 'N/A'
})

// Methods
function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
  })
}

function showToast(message, type = 'success') {
  const icons = {
      success: 'bi bi-check-circle-fill',
      error: 'bi bi-exclamation-circle-fill',
      info: 'bi bi-info-circle-fill'
  }
  
  toast.value = {
      show: true,
      type,
      message,
      icon: icons[type]
  }
  
  setTimeout(() => {
      hideToast()
  }, 4000)
}

function hideToast() {
  toast.value.show = false
}

async function fetchCategories() {
  isLoading.value = true
  try {
      const res = await axios.get('http://localhost:8080/api/categories')
      categories.value = res.data
  } catch (error) {
      console.error('Failed to load categories:', error)
      showToast('Không thể tải danh mục', 'error')
  } finally {
      isLoading.value = false
  }
}

async function addCategory() {
  if (!newCategory.value.name.trim() || nameError.value) return
  
  isAdding.value = true
  try {
      await axios.post('http://localhost:8080/api/categories', newCategory.value, {
          headers: { Authorization: `Bearer ${userStore.token}` }
      })
      
      clearForm()
      await fetchCategories()
      showToast('Thêm danh mục thành công!')
  } catch (error) {
      console.error('Failed to add category:', error)
      showToast('Không thể thêm danh mục', 'error')
  } finally {
      isAdding.value = false
  }
}

function clearForm() {
  newCategory.value = { name: '', description: '' }
}

function startEdit(category) {
  editId.value = category.id
  editCategory.value = { 
      name: category.name, 
      description: category.description || '' 
  }
}

function cancelEdit() {
  editId.value = null
  editCategory.value = { name: '', description: '' }
}

async function saveEdit(id) {
  if (!editCategory.value.name.trim()) return
  
  isSaving.value = true
  try {
      await axios.put(`http://localhost:8080/api/categories/${id}`, editCategory.value, {
          headers: { Authorization: `Bearer ${userStore.token}` }
      })
      
      cancelEdit()
      await fetchCategories()
      showToast('Cập nhật danh mục thành công!')
  } catch (error) {
      console.error('Failed to update category:', error)
      showToast('Không thể cập nhật danh mục', 'error')
  } finally {
      isSaving.value = false
  }
}

function confirmDelete(category) {
  categoryToDelete.value = category
  showDeleteModal.value = true
}

function closeDeleteModal() {
  showDeleteModal.value = false
  categoryToDelete.value = null
}

async function deleteCategory() {
  if (!categoryToDelete.value) return
  
  isDeleting.value = true
  try {
      await axios.delete(`http://localhost:8080/api/categories/${categoryToDelete.value.id}`, {
          headers: { Authorization: `Bearer ${userStore.token}` }
      })
      
      closeDeleteModal()
      await fetchCategories()
      showToast('Xóa danh mục thành công!')
  } catch (error) {
      console.error('Failed to delete category:', error)
      showToast('Không thể xóa danh mục', 'error')
  } finally {
      isDeleting.value = false
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.category-manager-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow-x: hidden;
  padding: 2rem 1rem;
}

/* Background Elements */
.background-elements {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.floating-element {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.element-1 {
  width: 80px;
  height: 80px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.element-2 {
  width: 120px;
  height: 120px;
  top: 20%;
  right: 10%;
  animation-delay: 2s;
}

.element-3 {
  width: 60px;
  height: 60px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

/* Hero Section */
.hero-section {
  text-align: center;
  margin-bottom: 3rem;
  position: relative;
  z-index: 1;
}

.hero-content {
  max-width: 600px;
  margin: 0 auto;
}

.hero-icon {
  font-size: 4rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 1rem;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-10px); }
  60% { transform: translateY(-5px); }
}

.hero-title {
  font-size: 3rem;
  font-weight: 800;
  color: white;
  margin-bottom: 1rem;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.hero-subtitle {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 2rem;
}

/* Stats Section */
.stats-section {
  margin-bottom: 3rem;
  position: relative;
  z-index: 1;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 2rem;
  display: flex;
  align-items: center;
  gap: 1.5rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
}

.stat-card.primary .stat-icon { background: linear-gradient(45deg, #667eea, #764ba2); }
.stat-card.success .stat-icon { background: linear-gradient(45deg, #2ed573, #1e90ff); }
.stat-card.info .stat-icon { background: linear-gradient(45deg, #ffa502, #ff6b9d); }

.stat-value {
  font-size: 2.5rem;
  font-weight: 800;
  color: #333;
  line-height: 1;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
}

/* Main Content */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

/* Add Category Form */
.add-category-section {
  margin-bottom: 3rem;
}

.form-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.form-header {
  text-align: center;
  margin-bottom: 2rem;
}

.form-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.form-subtitle {
  color: #666;
  font-size: 1rem;
}

.category-form {
  max-width: 600px;
  margin: 0 auto;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.form-group {
  position: relative;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.required {
  color: #ff4757;
}

.form-input, .form-textarea {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: white;
  resize: vertical;
}

.form-input:focus, .form-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-input.error, .form-textarea.error {
  border-color: #ff4757;
  box-shadow: 0 0 0 3px rgba(255, 71, 87, 0.1);
}

.form-input.success, .form-textarea.success {
  border-color: #2ed573;
  box-shadow: 0 0 0 3px rgba(46, 213, 115, 0.1);
}

.error-message {
  color: #ff4757;
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

.char-count {
  position: absolute;
  bottom: -1.5rem;
  right: 0;
  font-size: 0.75rem;
  color: #999;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.btn-primary, .btn-secondary {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
}

.btn-primary {
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f8f9fa;
  color: #666;
  border: 2px solid #e0e0e0;
}

.btn-secondary:hover {
  background: #e9ecef;
  border-color: #ccc;
}

/* Table Section */
.table-section {
  position: relative;
  z-index: 1;
}

.table-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.table-header {
  padding: 2rem;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 2rem;
  flex-wrap: wrap;
}

.header-content h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.table-subtitle {
  color: #666;
  font-size: 0.9rem;
}

.table-controls {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}

.search-box {
  position: relative;
  min-width: 250px;
}

.search-box i {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 3rem;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.sort-select {
  padding: 0.75rem 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-size: 1rem;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.sort-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* Table Container */
.table-container {
  padding: 2rem;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 4rem 2rem;
}

.loading-state .loading-spinner {
  font-size: 2rem;
  color: #667eea;
  margin-bottom: 1rem;
}

.empty-state .empty-icon {
  font-size: 4rem;
  color: #ccc;
  margin-bottom: 1rem;
}

.empty-state h3 {
  color: #333;
  margin-bottom: 0.5rem;
}

.empty-state p {
  color: #666;
}

/* Categories Grid */
.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.category-card {
  background: white;
  border-radius: 15px;
  padding: 1.5rem;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.6s ease forwards;
}

@keyframes fadeInUp {
  to {
      opacity: 1;
      transform: translateY(0);
  }
}

.category-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.12);
}

.category-card.editing {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.category-name {
  font-size: 1.2rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 0.5rem;
}

.category-meta {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.date-badge, .id-badge {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  padding: 0.25rem 0.5rem;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.card-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-edit, .btn-save, .btn-delete, .btn-cancel {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
}

.btn-edit {
  background: #ffa502;
  color: white;
}

.btn-edit:hover {
  background: #ff9500;
  transform: scale(1.05);
}

.btn-save {
  background: #2ed573;
  color: white;
}

.btn-save:hover:not(:disabled) {
  background: #26d167;
  transform: scale(1.05);
}

.btn-delete {
  background: #ff4757;
  color: white;
}

.btn-delete:hover {
  background: #ff3838;
  transform: scale(1.05);
}

.btn-cancel {
  background: #747d8c;
  color: white;
}

.btn-cancel:hover {
  background: #5f6a79;
  transform: scale(1.05);
}

.category-description {
  color: #666;
  line-height: 1.5;
  font-size: 0.9rem;
}

.edit-input, .edit-textarea {
  width: 100%;
  padding: 0.5rem;
  border: 2px solid #667eea;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  background: rgba(102, 126, 234, 0.05);
}

.edit-textarea {
  font-weight: normal;
  resize: vertical;
  min-height: 60px;
}

.edit-input:focus, .edit-textarea:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(5px);
}

.delete-modal {
  background: white;
  border-radius: 20px;
  padding: 2rem;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  transform: scale(0.9);
  animation: modalIn 0.3s ease forwards;
}

@keyframes modalIn {
  to {
      transform: scale(1);
  }
}

.modal-header {
  text-align: center;
  margin-bottom: 1.5rem;
}

.modal-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(45deg, #ff4757, #ff6b9d);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
  color: white;
  font-size: 1.5rem;
}

.modal-title {
  color: #333;
  font-size: 1.3rem;
  font-weight: 700;
}

.modal-body {
  margin-bottom: 2rem;
  text-align: center;
}

.modal-body p {
  color: #666;
  margin-bottom: 1rem;
}

.warning-text {
  color: #ff4757 !important;
  font-weight: 600;
  font-size: 0.9rem;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.btn-cancel-modal, .btn-confirm-delete {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-cancel-modal {
  background: #f8f9fa;
  color: #666;
  border: 2px solid #e0e0e0;
}

.btn-cancel-modal:hover {
  background: #e9ecef;
}

.btn-confirm-delete {
  background: linear-gradient(45deg, #ff4757, #ff6b9d);
  color: white;
}

.btn-confirm-delete:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
}

.btn-confirm-delete:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Toast */
.toast {
  position: fixed;
  top: 2rem;
  right: 2rem;
  background: white;
  border-radius: 12px;
  padding: 1rem 1.5rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  gap: 1rem;
  z-index: 1001;
  border-left: 4px solid;
  animation: toastIn 0.3s ease;
}

@keyframes toastIn {
  from {
      transform: translateX(100%);
      opacity: 0;
  }
  to {
      transform: translateX(0);
      opacity: 1;
  }
}

.toast.success {
  border-left-color: #2ed573;
}

.toast.error {
  border-left-color: #ff4757;
}

.toast.info {
  border-left-color: #1e90ff;
}

.toast-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex: 1;
}

.toast.success .toast-content i {
  color: #2ed573;
}

.toast.error .toast-content i {
  color: #ff4757;
}

.toast.info .toast-content i {
  color: #1e90ff;
}

.toast-close {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  padding: 0;
  font-size: 1.2rem;
}

.toast-close:hover {
  color: #666;
}

/* Animations */
.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Responsive Design */
@media (max-width: 768px) {
  .category-manager-container {
      padding: 1rem 0.5rem;
  }
  
  .hero-title {
      font-size: 2rem;
  }
  
  .hero-subtitle {
      font-size: 1rem;
  }
  
  .stats-grid {
      grid-template-columns: 1fr;
      gap: 1rem;
  }
  
  .form-row {
      grid-template-columns: 1fr;
      gap: 1rem;
  }
  
  .table-header {
      flex-direction: column;
      align-items: stretch;
      gap: 1rem;
  }
  
  .table-controls {
      flex-direction: column;
      align-items: stretch;
  }
  
  .search-box {
      min-width: auto;
  }
  
  .categories-grid {
      grid-template-columns: 1fr;
  }
  
  .card-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 1rem;
  }
  
  .card-actions {
      align-self: flex-end;
  }
  
  .modal-actions {
      flex-direction: column;
  }
  
  .toast {
      top: 1rem;
      right: 1rem;
      left: 1rem;
  }
}

@media (max-width: 480px) {
  .hero-icon {
      font-size: 3rem;
  }
  
  .hero-title {
      font-size: 1.5rem;
  }
  
  .form-card, .table-card {
      padding: 1rem;
  }
  
  .table-container {
      padding: 1rem;
  }
  
  .delete-modal {
      padding: 1.5rem;
  }
}
</style>