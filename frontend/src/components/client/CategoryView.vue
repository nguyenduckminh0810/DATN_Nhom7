<template>
  <div class="category-view-container">
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
        <h1 class="hero-title">Danh mục Quiz</h1>
        <p class="hero-subtitle">Khám phá các danh mục quiz đa dạng</p>
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
            <i class="bi bi-graph-up"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ mostPopularCategory }}</div>
            <div class="stat-label">Phổ biến nhất</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Categories List -->
    <div class="main-content">
      <div class="categories-section">
        <div class="section-header">
          <h2 class="section-title">
            <i class="bi bi-list-ul"></i>
            Danh sách danh mục
          </h2>
          <p class="section-subtitle">{{ categories.length }} danh mục</p>
        </div>

        <!-- Search and Filter -->
        <div class="search-filter-section">
          <div class="search-box">
            <i class="bi bi-search"></i>
            <input type="text" v-model="searchQuery" placeholder="Tìm kiếm danh mục..." class="search-input" />
          </div>

          <div class="filter-dropdown">
            <select v-model="sortBy" class="filter-select">
              <option value="name">Tên A-Z</option>
              <option value="name-desc">Tên Z-A</option>
              <option value="date">Ngày tạo</option>
            </select>
          </div>
        </div>

        <!-- Categories Grid -->
        <div class="categories-grid">
          <div v-for="category in filteredCategories" :key="category.id" class="category-card"
            @click="viewCategoryQuizzes(category)">
            <div class="category-header">
              <div class="category-icon">
                <i class="bi bi-tag-fill"></i>
              </div>
              <div class="category-info">
                <h3 class="category-name">{{ category.name }}</h3>
                <p class="category-description">{{ category.description || 'Không có mô tả' }}</p>
              </div>
            </div>

            <div class="category-footer">
              <div class="category-date">
                <i class="bi bi-calendar3"></i>
                {{ formatDate(category.createdAt) }}
              </div>
              <div class="category-id">ID: {{ category.id }}</div>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-if="filteredCategories.length === 0" class="empty-state">
          <div class="empty-icon">
            <i class="bi bi-search"></i>
          </div>
          <h3>Không tìm thấy danh mục</h3>
          <p>Thử thay đổi từ khóa tìm kiếm hoặc bộ lọc</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/utils/axios'

const router = useRouter()

// Reactive data
const categories = ref([])
const searchQuery = ref('')
const sortBy = ref('name')
const isLoading = ref(false)

// Computed properties
const filteredCategories = computed(() => {
  let filtered = categories.value

  // Search filter
  if (searchQuery.value) {
    filtered = filtered.filter(category =>
      category.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      category.description?.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }

  // Sort
  switch (sortBy.value) {
    case 'name':
      filtered.sort((a, b) => a.name.localeCompare(b.name))
      break
    case 'name-desc':
      filtered.sort((a, b) => b.name.localeCompare(a.name))
      break
    case 'date':
      filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      break
  }

  return filtered
})

const mostPopularCategory = computed(() => {
  if (categories.value.length === 0) return 'N/A'
  // Logic để tìm danh mục phổ biến nhất (có thể dựa trên số quiz)
  return categories.value[0]?.name || 'N/A'
})

// Methods
const fetchCategories = async () => {
  try {
    isLoading.value = true
    const response = await api.get('/categories')
    categories.value = response.data
    console.log('Categories loaded:', categories.value)
  } catch (error) {
    console.error('Error fetching categories:', error)
  } finally {
    isLoading.value = false
  }
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  })
}

const viewCategoryQuizzes = (category) => {
  router.push({
    name: 'CategoryQuizzes',
    params: { id: category.id },
    query: { name: category.name }
  })
}
// Lifecycle
onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.category-view-container {
  min-height: 100vh;
  background: var(--app-background);
  position: relative;
  overflow: hidden;
}

.background-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.floating-element {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.element-1 {
  width: 100px;
  height: 100px;
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
  width: 80px;
  height: 80px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0px);
  }

  50% {
    transform: translateY(-20px);
  }
}

.hero-section {
  padding: 4rem 2rem 2rem;
  text-align: center;
  position: relative;
  z-index: 2;
}

.hero-content {
  max-width: 600px;
  margin: 0 auto;
}

.hero-icon {
  font-size: 4rem;
  color: white;
  margin-bottom: 1rem;
}

.hero-title {
  font-size: 3rem;
  font-weight: 700;
  color: white;
  margin-bottom: 1rem;
}

.hero-subtitle {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.9);
}

.stats-section {
  padding: 0 2rem 2rem;
  position: relative;
  z-index: 2;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
}

.stat-card {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-card.primary {
  border-left: 4px solid #007bff;
}

.stat-card.success {
  border-left: 4px solid #28a745;
}

.stat-icon {
  font-size: 2rem;
  color: #007bff;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #343a40;
  margin-bottom: 0.25rem;
}

.stat-label {
  color: #6c757d;
  font-size: 0.9rem;
}

.main-content {
  padding: 2rem;
  position: relative;
  z-index: 2;
}

.categories-section {
  max-width: 1200px;
  margin: 0 auto;
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.section-header {
  text-align: center;
  margin-bottom: 2rem;
}

.section-title {
  font-size: 2rem;
  font-weight: 700;
  color: #343a40;
  margin-bottom: 0.5rem;
}

.section-subtitle {
  color: #6c757d;
  font-size: 1.1rem;
}

.search-filter-section {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.search-box {
  flex: 1;
  min-width: 300px;
  position: relative;
}

.search-box i {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #6c757d;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  border: 2px solid #e9ecef;
  border-radius: 0.5rem;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #007bff;
}

.filter-select {
  padding: 0.75rem 1rem;
  border: 2px solid #e9ecef;
  border-radius: 0.5rem;
  font-size: 1rem;
  background: white;
  cursor: pointer;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.category-card {
  background: #f8f9fa;
  border-radius: 1rem;
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.category-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border-color: #007bff;
}

.category-header {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1rem;
}

.category-icon {
  font-size: 2rem;
  color: #007bff;
  flex-shrink: 0;
}

.category-info {
  flex: 1;
}

.category-name {
  font-size: 1.25rem;
  font-weight: 600;
  color: #343a40;
  margin-bottom: 0.5rem;
}

.category-description {
  color: #6c757d;
  line-height: 1.5;
  margin: 0;
}

.category-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
  border-top: 1px solid #e9ecef;
  font-size: 0.9rem;
  color: #6c757d;
}

.category-date {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.empty-state {
  text-align: center;
  padding: 3rem 1rem;
  color: #6c757d;
}

.empty-icon {
  font-size: 4rem;
  color: #dee2e6;
  margin-bottom: 1rem;
}

.empty-state h3 {
  margin-bottom: 0.5rem;
  color: #343a40;
}

/* Responsive */
@media (max-width: 768px) {
  .hero-title {
    font-size: 2rem;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .search-filter-section {
    flex-direction: column;
  }

  .search-box {
    min-width: auto;
  }

  .categories-grid {
    grid-template-columns: 1fr;
  }
}
</style>