<template>
  <div class="category-trash-container">
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
          <i class="bi bi-trash3-fill"></i>
        </div>
        <h1 class="hero-title">Thùng rác danh mục</h1>
        <p class="hero-subtitle">Quản lý các danh mục đã bị xóa và khôi phục khi cần thiết</p>
      </div>
    </div>

    <!-- Stats Overview -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card danger">
          <div class="stat-icon">
            <i class="bi bi-trash3"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ deletedCategories.length }}</div>
            <div class="stat-label">Danh mục đã xóa</div>
          </div>
        </div>

        <div class="stat-card warning">
          <div class="stat-icon">
            <i class="bi bi-clock-history"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ recentDeletions }}</div>
            <div class="stat-label">Xóa gần đây</div>
          </div>
        </div>

        <div class="stat-card info">
          <div class="stat-icon">
            <i class="bi bi-arrow-clockwise"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ restoredCount }}</div>
            <div class="stat-label">Đã khôi phục</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="main-content">
      <div class="table-section">
        <div class="table-card">
          <div class="table-header">
            <div class="header-content">
              <h2 class="table-title">
                <i class="bi bi-trash3"></i>
                Danh sách danh mục đã xóa
              </h2>
              <p class="table-subtitle">{{ deletedCategories.length }} danh mục</p>
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
                <option value="deletedAt">Xóa gần nhất</option>
                <option value="deletedAtOld">Xóa lâu nhất</option>
                <option value="name">Tên A-Z</option>
                <option value="nameDesc">Tên Z-A</option>
              </select>
            </div>
          </div>

          <div class="table-container">
            <div v-if="isLoading" class="loading-state">
              <div class="loading-spinner">
                <i class="bi bi-arrow-clockwise spin"></i>
              </div>
              <p>Đang tải danh mục đã xóa...</p>
            </div>

            <div v-else-if="filteredCategories.length === 0" class="empty-state">
              <div class="empty-icon">
                <i class="bi bi-trash3"></i>
              </div>
              <h3>{{ searchTerm ? 'Không tìm thấy danh mục' : 'Thùng rác trống' }}</h3>
              <p>{{ searchTerm ? 'Thử từ khóa khác' : 'Chưa có danh mục nào bị xóa' }}</p>
            </div>

            <div v-else class="categories-grid">
              <div
                v-for="(category, index) in filteredCategories"
                :key="category.id"
                class="category-card deleted"
                :style="{ 'animation-delay': `${index * 0.1}s` }"
              >
                <div class="card-header">
                  <div class="category-info">
                    <div class="category-name">
                      {{ category.name }}
                      <span class="deleted-badge">
                        <i class="bi bi-trash3"></i>
                        Đã xóa
                      </span>
                    </div>
                    <div class="category-meta">
                      <span class="date-badge">
                        <i class="bi bi-calendar3"></i>
                        {{ formatDate(category.deletedAt) }}
                      </span>
                      <span class="id-badge"> ID: {{ category.id }} </span>
                      <span v-if="category.deletedBy" class="deleted-by-badge">
                        <i class="bi bi-person"></i>
                        {{ category.deletedBy.username }}
                      </span>
                    </div>
                  </div>

                  <div class="card-actions">
                    <button
                      @click="restoreCategory(category)"
                      class="btn-restore"
                      title="Khôi phục"
                    >
                      <i class="bi bi-arrow-clockwise"></i>
                    </button>
                    <button
                      @click="confirmHardDelete(category)"
                      class="btn-hard-delete"
                      title="Xóa hoàn toàn"
                    >
                      <i class="bi bi-trash3-fill"></i>
                    </button>
                  </div>
                </div>

                <div class="card-body">
                  <div class="category-description">
                    {{ category.description || 'Chưa có mô tả' }}
                  </div>
                  <div class="deletion-info">
                    <small class="deletion-time">
                      <i class="bi bi-clock"></i>
                      Xóa lúc: {{ formatDateTime(category.deletedAt) }}
                    </small>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Hard Delete Confirmation Modal -->
    <div v-if="showHardDeleteModal" class="modal-overlay" @click="closeHardDeleteModal">
      <div class="delete-modal hard-delete" @click.stop>
        <div class="modal-header">
          <div class="modal-icon">
            <i class="bi bi-exclamation-triangle-fill"></i>
          </div>
          <h3 class="modal-title">Xóa hoàn toàn danh mục</h3>
        </div>

        <div class="modal-body">
          <div class="category-info">
            <h4>{{ categoryToHardDelete?.name }}</h4>
            <p class="category-description">
              {{ categoryToHardDelete?.description || 'Không có mô tả' }}
            </p>
            <div class="category-meta">
              <span class="date-badge">
                <i class="bi bi-calendar3"></i>
                {{ formatDate(categoryToHardDelete?.createdAt) }}
              </span>
              <span class="id-badge">ID: {{ categoryToHardDelete?.id }}</span>
            </div>
          </div>

          <div class="warning-section danger">
            <div class="warning-icon">
              <i class="bi bi-exclamation-triangle-fill"></i>
            </div>
            <div class="warning-content">
              <p><strong>Cảnh báo:</strong></p>
              <ul>
                <li>Hành động này sẽ xóa hoàn toàn danh mục khỏi database</li>
                <li>Không thể khôi phục lại sau khi xóa</li>
                <li>Tất cả dữ liệu liên quan sẽ bị mất vĩnh viễn</li>
              </ul>
            </div>
          </div>
        </div>

        <div class="modal-actions">
          <button @click="closeHardDeleteModal" class="btn-cancel-modal">
            <i class="bi bi-x-circle"></i>
            Hủy
          </button>
          <button
            @click="hardDeleteCategory"
            class="btn-confirm-hard-delete"
            :disabled="isHardDeleting"
          >
            <i v-if="isHardDeleting" class="bi bi-arrow-clockwise spin"></i>
            <i v-else class="bi bi-trash3-fill"></i>
            {{ isHardDeleting ? 'Đang xóa...' : 'Xóa hoàn toàn' }}
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
import { ref, onMounted, computed } from 'vue'
import api from '@/utils/axios'

const deletedCategories = ref([])
const searchTerm = ref('')
const sortBy = ref('deletedAt')
const isLoading = ref(true)
const isRestoring = ref(false)
const isHardDeleting = ref(false)
const showHardDeleteModal = ref(false)
const categoryToHardDelete = ref(null)
const restoredCount = ref(0)

const toast = ref({
  show: false,
  type: 'success',
  message: '',
  icon: '',
})

// Computed properties
const filteredCategories = computed(() => {
  let filtered = [...deletedCategories.value]

  // Search filter
  if (searchTerm.value) {
    filtered = filtered.filter(
      (cat) =>
        cat.name.toLowerCase().includes(searchTerm.value.toLowerCase()) ||
        cat.description?.toLowerCase().includes(searchTerm.value.toLowerCase()),
    )
  }

  // Sort
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'deletedAt':
        return new Date(b.deletedAt) - new Date(a.deletedAt)
      case 'deletedAtOld':
        return new Date(a.deletedAt) - new Date(b.deletedAt)
      case 'name':
        return a.name.localeCompare(b.name)
      case 'nameDesc':
        return b.name.localeCompare(a.name)
      default:
        return 0
    }
  })

  return filtered
})

const recentDeletions = computed(() => {
  const oneWeekAgo = new Date()
  oneWeekAgo.setDate(oneWeekAgo.getDate() - 7)
  return deletedCategories.value.filter((cat) => new Date(cat.deletedAt) > oneWeekAgo).length
})

// Methods
function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

function formatDateTime(dateStr) {
  return new Date(dateStr).toLocaleString('vi-VN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function showToast(message, type = 'success') {
  const icons = {
    success: 'bi bi-check-circle-fill',
    error: 'bi bi-exclamation-circle-fill',
    info: 'bi bi-info-circle-fill',
  }

  toast.value = {
    show: true,
    type,
    message,
    icon: icons[type],
  }

  setTimeout(() => {
    hideToast()
  }, 4000)
}

function hideToast() {
  toast.value.show = false
}

async function fetchDeletedCategories() {
  isLoading.value = true
  try {
    const res = await api.get('/admin/categories/deleted')
    deletedCategories.value = res.data
  } catch (error) {
    console.error('Failed to load deleted categories:', error)
    showToast('Không thể tải danh mục đã xóa', 'error')
  } finally {
    isLoading.value = false
  }
}

async function restoreCategory(category) {
  isRestoring.value = true
  try {
    const token = localStorage.getItem('token')
    await api.put(
      `/admin/categories/${category.id}/restore`,
      {},
      {
        headers: { Authorization: `Bearer ${token}` },
      },
    )

    showToast('Khôi phục danh mục thành công!')
    await fetchDeletedCategories()
    restoredCount.value++
  } catch (error) {
    console.error('Failed to restore category:', error)
    showToast('Không thể khôi phục danh mục', 'error')
  } finally {
    isRestoring.value = false
  }
}

function confirmHardDelete(category) {
  categoryToHardDelete.value = category
  showHardDeleteModal.value = true
}

function closeHardDeleteModal() {
  showHardDeleteModal.value = false
  categoryToHardDelete.value = null
}

async function hardDeleteCategory() {
  if (!categoryToHardDelete.value) return

  isHardDeleting.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await api.delete(`/admin/categories/${categoryToHardDelete.value.id}/hard`, {
      headers: { Authorization: `Bearer ${token}` },
    })

    const message = response.data || 'Xóa hoàn toàn danh mục thành công!'

    if (message.includes('Không thể xóa hoàn toàn danh mục vì có')) {
      showToast(message, 'error')
      closeHardDeleteModal()
      return
    }

    showToast(message, 'success')
    closeHardDeleteModal()
    await fetchDeletedCategories()
  } catch (error) {
    console.error('Failed to hard delete category:', error)
    const errorMessage = error.response?.data || 'Không thể xóa hoàn toàn danh mục'
    showToast(errorMessage, 'error')
  } finally {
    isHardDeleting.value = false
  }
}

onMounted(() => {
  fetchDeletedCategories()
})
</script>

<style scoped>
/* Background Elements */
.background-elements {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -1;
  overflow: hidden;
}

.floating-element {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.1), rgba(238, 90, 36, 0.1));
  animation: float 6s ease-in-out infinite;
}

.element-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.element-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.element-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

/* Hero Section */
.hero-section {
  background: var(--card-header-bg);
  padding: 3rem 0;
  text-align: center;
  margin-bottom: 2rem;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 2rem;
}

.hero-icon {
  font-size: 4rem;
  color: var(--danger-color);
  margin-bottom: 1rem;
}

.hero-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.hero-subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary);
  line-height: 1.6;
}

/* Stats Section */
.stats-section {
  margin-bottom: 2rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.stat-card {
  background: var(--bg-primary);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 10px 30px var(--shadow-color);
  border: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 1.5rem;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.stat-card.danger {
  border-left: 4px solid var(--danger-color);
}
.stat-card.warning {
  border-left: 4px solid var(--warning-color);
}
.stat-card.info {
  border-left: 4px solid var(--info-color);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
}

.stat-card.danger .stat-icon {
  background: linear-gradient(135deg, var(--danger-color), #ff6b9d);
}
.stat-card.warning .stat-icon {
  background: linear-gradient(135deg, var(--warning-color), #ffb142);
}
.stat-card.info .stat-icon {
  background: linear-gradient(135deg, var(--info-color), #5352ed);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}
.stat-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
  font-weight: 500;
}

/* Main Content */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

/* Table Section */
.table-section {
  position: relative;
  z-index: 1;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
}

.table-card {
  background: var(--bg-primary);
  color: var(--text-primary);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 30px var(--shadow-color);
  border: 1px solid var(--border-color);
}

.table-header {
  padding: 2rem;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-primary);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 2rem;
  flex-wrap: wrap;
}

.header-content h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.table-subtitle {
  color: var(--text-secondary);
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
  color: #999;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  border: 2px solid var(--border-color);
  border-radius: 12px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  background: var(--bg-primary);
  color: var(--text-primary);
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.sort-select {
  padding: 0.75rem 1rem;
  border: 2px solid var(--border-color);
  border-radius: 12px;
  font-size: 0.95rem;
  background: var(--bg-primary);
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.sort-select:focus {
  outline: none;
  border-color: var(--info-color);
}

.sort-select option {
  background: var(--bg-primary);
  color: var(--text-primary);
}

/* Table Container */
.table-container {
  padding: 2rem;
  background: var(--bg-primary);
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 3rem 2rem;
  background: var(--bg-primary);
}

.loading-spinner {
  width: 60px;
  height: 60px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.empty-icon {
  font-size: 4rem;
  color: var(--text-muted);
  margin-bottom: 1rem;
}
.empty-state h3 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}
.empty-state p {
  color: var(--text-secondary);
}

/* Categories Grid */
.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.category-card {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
  transition: all 0.3s ease;
  animation: slideIn 0.5s ease forwards;
  opacity: 0;
  transform: translateY(20px);
}

.category-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15);
}

.category-card.deleted {
  border-left: 4px solid var(--danger-color);
  background: var(--bg-primary);
}

@keyframes slideIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.category-info {
  flex: 1;
}

.category-name {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.deleted-badge {
  background: linear-gradient(135deg, #ff4757, #ff6b9d);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.category-meta {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.date-badge,
.id-badge,
.deleted-by-badge {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.deleted-by-badge {
  background: rgba(255, 71, 87, 0.1);
  color: var(--danger-color);
}

.card-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-restore,
.btn-hard-delete {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1rem;
}

.btn-restore {
  background: linear-gradient(135deg, #2ed573, #7bed9f);
  color: white;
}

.btn-restore:hover {
  transform: scale(1.1);
  box-shadow: 0 5px 15px rgba(46, 213, 115, 0.4);
}

.btn-hard-delete {
  background: linear-gradient(135deg, #ff4757, #ff6b9d);
  color: white;
}

.btn-hard-delete:hover {
  transform: scale(1.1);
  box-shadow: 0 5px 15px rgba(255, 71, 87, 0.4);
}

.card-body {
  border-top: 1px solid var(--border-color);
  padding-top: 1rem;
}

.category-description {
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 1rem;
}

.deletion-info {
  border-top: 1px solid var(--border-color);
  padding-top: 0.75rem;
}

.deletion-time {
  color: var(--text-muted);
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

/* Modal Styles */
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
  background: var(--bg-primary);
  border-radius: 16px;
  padding: 2rem;
  max-width: 600px;
  width: 90%;
  box-shadow: 0 20px 60px var(--shadow-color);
  animation: modalSlideIn 0.3s ease;
}

.delete-modal.hard-delete {
  border-left: 4px solid var(--danger-color);
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.modal-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--danger-color), #ff6b9d);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

.modal-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.modal-body {
  margin-bottom: 2rem;
}

/* Category Info Section */
.category-info {
  background: var(--bg-tertiary);
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  border-left: 4px solid var(--info-color);
}

.category-info h4 {
  color: #333;
  font-size: 1.3rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
}

.category-description {
  color: #666;
  font-size: 0.95rem;
  margin-bottom: 1rem;
  line-height: 1.5;
}

.category-meta {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

/* Warning Section */
.warning-section {
  background: rgba(255, 193, 7, 0.1);
  border-radius: 12px;
  padding: 1.5rem;
  border-left: 4px solid #ffc107;
}

.warning-section.danger {
  background: rgba(255, 71, 87, 0.1);
  border-left: 4px solid #ff4757;
}

.warning-section .warning-icon {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
  color: #856404;
  font-size: 1.1rem;
}

.warning-section.danger .warning-icon {
  color: #721c24;
}

.warning-content p {
  color: #856404;
  font-weight: 600;
  margin-bottom: 0.75rem;
}

.warning-section.danger .warning-content p {
  color: #721c24;
}

.warning-content ul {
  margin: 0;
  padding-left: 1.5rem;
  color: #856404;
}

.warning-section.danger .warning-content ul {
  color: #721c24;
}

.warning-content li {
  margin-bottom: 0.5rem;
  line-height: 1.4;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.btn-cancel-modal,
.btn-confirm-hard-delete {
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

.btn-confirm-hard-delete {
  background: linear-gradient(135deg, #ff4757, #ff6b9d);
  color: white;
}

.btn-confirm-hard-delete:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(255, 71, 87, 0.4);
}

.btn-confirm-hard-delete:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Toast */
.toast {
  position: fixed;
  top: 2rem;
  right: 2rem;
  background: var(--bg-primary);
  border-radius: 12px;
  padding: 1rem 1.5rem;
  box-shadow: 0 10px 30px var(--shadow-color);
  z-index: 1001;
  display: flex;
  align-items: center;
  gap: 1rem;
  min-width: 300px;
  animation: toastSlideIn 0.3s ease;
}

.toast.success {
  border-left: 4px solid #2ed573;
}

.toast.error {
  border-left: 4px solid var(--danger-color);
}

.toast.info {
  border-left: 4px solid var(--info-color);
}

.toast-content {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
}

.toast-content i {
  font-size: 1.2rem;
}

.toast.success .toast-content i {
  color: #2ed573;
}

.toast.error .toast-content i {
  color: #ff4757;
}

.toast.info .toast-content i {
  color: #3742fa;
}

.toast-close {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 1.2rem;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.toast-close:hover {
  background: #f0f0f0;
  color: #666;
}

@keyframes toastSlideIn {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .hero-title {
    font-size: 2rem;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .table-header {
    flex-direction: column;
    align-items: stretch;
  }

  .categories-grid {
    grid-template-columns: 1fr;
  }

  .modal-actions {
    flex-direction: column;
  }
}
</style>
