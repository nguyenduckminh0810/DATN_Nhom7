<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import api from '@/utils/axios'

const route = useRoute()
const userId = route.params.userId

const history = ref([])
const loading = ref(true)
const error = ref(null)
const searchTerm = ref('')
const sortBy = ref('recent') // recent, score, title
const filterScore = ref('all') // all, excellent, good, average, poor

// Computed properties
const filteredHistory = computed(() => {
    let filtered = [...history.value]

    // Search filter
    if (searchTerm.value) {
        filtered = filtered.filter(item =>
            item.quizTitle.toLowerCase().includes(searchTerm.value.toLowerCase())
        )
    }

    // Score filter
    if (filterScore.value !== 'all') {
        filtered = filtered.filter(item => {
            const score = parseFloat(item.score)
            switch (filterScore.value) {
                case 'excellent': return score >= 90
                case 'good': return score >= 70 && score < 90
                case 'average': return score >= 50 && score < 70
                case 'poor': return score < 50
                default: return true
            }
        })
    }

    // Sort
    filtered.sort((a, b) => {
        switch (sortBy.value) {
            case 'recent':
                return new Date(b.completedAt) - new Date(a.completedAt)
            case 'score':
                return parseFloat(b.score) - parseFloat(a.score)
            case 'title':
                return a.quizTitle.localeCompare(b.quizTitle)
            default:
                return 0
        }
    })

    return filtered
})

const stats = computed(() => {
    if (history.value.length === 0) return null

    const scores = history.value.map(item => parseFloat(item.score))
    const avgScore = scores.reduce((a, b) => a + b, 0) / scores.length
    const maxScore = Math.max(...scores)
    const totalQuizzes = history.value.length

    const excellent = scores.filter(s => s >= 90).length
    const good = scores.filter(s => s >= 70 && s < 90).length
    const average = scores.filter(s => s >= 50 && s < 70).length
    const poor = scores.filter(s => s < 50).length

    return {
        avgScore: avgScore.toFixed(1),
        maxScore,
        totalQuizzes,
        excellent,
        good,
        average,
        poor
    }
})

function getScoreColor(score) {
    const numScore = parseFloat(score)
    if (numScore >= 90) return '#2ed573' // Excellent
    if (numScore >= 70) return '#1e90ff' // Good  
    if (numScore >= 50) return '#ffa502' // Average
    return '#ff4757' // Poor
}

function getScoreLabel(score) {
    const numScore = parseFloat(score)
    if (numScore >= 90) return 'Xuất sắc'
    if (numScore >= 70) return 'Tốt'
    if (numScore >= 50) return 'Trung bình'
    return 'Cần cải thiện'
}

function getScoreIcon(score) {
    const numScore = parseFloat(score)
    if (numScore >= 90) return 'bi-trophy-fill'
    if (numScore >= 70) return 'bi-star-fill'
    if (numScore >= 50) return 'bi-hand-thumbs-up-fill'
    return 'bi-arrow-up-circle-fill'
}

function formatDate(dateStr) {
    const date = new Date(dateStr)
    const now = new Date()
    const diffTime = Math.abs(now - date)
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

    if (diffDays === 1) return 'Hôm qua'
    if (diffDays <= 7) return `${diffDays} ngày trước`
    if (diffDays <= 30) return `${Math.ceil(diffDays / 7)} tuần trước`
    if (diffDays <= 365) return `${Math.ceil(diffDays / 30)} tháng trước`
    return `${Math.ceil(diffDays / 365)} năm trước`
}

function formatDateTime(dateStr) {
    return new Date(dateStr).toLocaleString('vi-VN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    })
}

onMounted(async () => {
    try {
        const res = await api.get(`/result/user/${userId}`)
        history.value = res.data
    } catch (err) {
        error.value = err.response?.data?.message || 'Lỗi khi tải lịch sử làm bài.'
    } finally {
        loading.value = false
    }
})
</script>

<template>
    <div class="quiz-history-container">
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
                    <i class="bi bi-clock-history"></i>
                </div>
                <h1 class="hero-title">Lịch sử làm bài</h1>
                <p class="hero-subtitle">Theo dõi tiến trình học tập và cải thiện kết quả của bạn</p>
            </div>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="loading-container">
            <div class="loading-spinner">
                <div class="spinner-ring"></div>
                <div class="spinner-ring"></div>
                <div class="spinner-ring"></div>
            </div>
            <h3 class="loading-text">Đang tải lịch sử...</h3>
        </div>

        <!-- Error State -->
        <div v-else-if="error" class="error-container">
            <div class="error-card">
                <i class="bi bi-exclamation-triangle"></i>
                <h3>Có lỗi xảy ra</h3>
                <p>{{ error }}</p>
                <button @click="$router.go(-1)" class="btn-back">
                    <i class="bi bi-arrow-left"></i>
                    Quay lại
                </button>
            </div>
        </div>

        <!-- Main Content -->
        <div v-else class="main-content">
            <!-- Stats Overview -->
            <div v-if="stats" class="stats-section">
                <div class="stats-grid">
                    <div class="stat-card primary">
                        <div class="stat-icon">
                            <i class="bi bi-graph-up"></i>
                        </div>
                        <div class="stat-content">
                            <div class="stat-value">{{ stats.avgScore }}</div>
                            <div class="stat-label">Điểm trung bình</div>
                        </div>
                    </div>

                    <div class="stat-card success">
                        <div class="stat-icon">
                            <i class="bi bi-trophy"></i>
                        </div>
                        <div class="stat-content">
                            <div class="stat-value">{{ stats.maxScore }}</div>
                            <div class="stat-label">Điểm cao nhất</div>
                        </div>
                    </div>

                    <div class="stat-card info">
                        <div class="stat-icon">
                            <i class="bi bi-list-check"></i>
                        </div>
                        <div class="stat-content">
                            <div class="stat-value">{{ stats.totalQuizzes }}</div>
                            <div class="stat-label">Tổng số bài</div>
                        </div>
                    </div>

                    <div class="stat-card warning">
                        <div class="stat-icon">
                            <i class="bi bi-star-fill"></i>
                        </div>
                        <div class="stat-content">
                            <div class="stat-value">{{ stats.excellent }}</div>
                            <div class="stat-label">Bài xuất sắc</div>
                        </div>
                    </div>
                </div>

                <!-- Performance Chart -->
                <div class="performance-chart">
                    <h3 class="chart-title">Phân bố kết quả</h3>
                    <div class="chart-bars">
                        <div class="chart-bar excellent">
                            <div class="bar-fill"
                                :style="{ height: `${(stats.excellent / stats.totalQuizzes) * 100}%` }"></div>
                            <div class="bar-label">
                                <span class="bar-count">{{ stats.excellent }}</span>
                                <span class="bar-text">Xuất sắc</span>
                            </div>
                        </div>
                        <div class="chart-bar good">
                            <div class="bar-fill" :style="{ height: `${(stats.good / stats.totalQuizzes) * 100}%` }">
                            </div>
                            <div class="bar-label">
                                <span class="bar-count">{{ stats.good }}</span>
                                <span class="bar-text">Tốt</span>
                            </div>
                        </div>
                        <div class="chart-bar average">
                            <div class="bar-fill" :style="{ height: `${(stats.average / stats.totalQuizzes) * 100}%` }">
                            </div>
                            <div class="bar-label">
                                <span class="bar-count">{{ stats.average }}</span>
                                <span class="bar-text">TB</span>
                            </div>
                        </div>
                        <div class="chart-bar poor">
                            <div class="bar-fill" :style="{ height: `${(stats.poor / stats.totalQuizzes) * 100}%` }">
                            </div>
                            <div class="bar-label">
                                <span class="bar-count">{{ stats.poor }}</span>
                                <span class="bar-text">Yếu</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Filters & Search -->
            <div class="filters-section">
                <div class="search-box">
                    <i class="bi bi-search"></i>
                    <input v-model="searchTerm" type="text" placeholder="Tìm kiếm quiz..." class="search-input" />
                </div>

                <div class="filter-controls">
                    <select v-model="sortBy" class="filter-select">
                        <option value="recent">Mới nhất</option>
                        <option value="score">Điểm cao nhất</option>
                        <option value="title">Tên A-Z</option>
                    </select>

                    <select v-model="filterScore" class="filter-select">
                        <option value="all">Tất cả điểm</option>
                        <option value="excellent">Xuất sắc (90+)</option>
                        <option value="good">Tốt (70-89)</option>
                        <option value="average">Trung bình (50-69)</option>
                        <option value="poor">Cần cải thiện (&lt;50)</option>
                    </select>
                </div>
            </div>

            <!-- History Timeline -->
            <div v-if="filteredHistory.length > 0" class="timeline-section">
                <div class="timeline">
                    <div v-for="(item, index) in filteredHistory" :key="item.id" class="timeline-item"
                        :class="{ 'animate-in': true }" :style="{ 'animation-delay': `${index * 0.1}s` }">
                        <div class="timeline-marker" :style="{ backgroundColor: getScoreColor(item.score) }">
                            <i :class="getScoreIcon(item.score)"></i>
                        </div>

                        <div class="timeline-content">
                            <div class="quiz-card">
                                <div class="card-header">
                                    <h3 class="quiz-title">{{ item.quizTitle }}</h3>
                                    <div class="quiz-meta">
                                        <span class="date-badge">
                                            <i class="bi bi-calendar3"></i>
                                            {{ formatDate(item.completedAt) }}
                                        </span>
                                    </div>
                                </div>

                                <div class="card-body">
                                    <div class="score-section">
                                        <div class="score-circle" :style="{ borderColor: getScoreColor(item.score) }">
                                            <div class="score-value">{{ item.score }}</div>
                                            <div class="score-max">/ 100</div>
                                        </div>

                                        <div class="score-details">
                                            <div class="score-label" :style="{ color: getScoreColor(item.score) }">
                                                {{ getScoreLabel(item.score) }}
                                            </div>
                                            <div class="score-progress">
                                                <div class="progress-bar">
                                                    <div class="progress-fill" :style="{
                                                        width: `${item.score}%`,
                                                        backgroundColor: getScoreColor(item.score)
                                                    }"></div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="quiz-info">
                                        <div class="info-item">
                                            <i class="bi bi-clock"></i>
                                            <span>{{ formatDateTime(item.completedAt) }}</span>
                                        </div>
                                        <div class="info-item">
                                            <i class="bi bi-hash"></i>
                                            <span>Quiz ID: {{ item.id }}</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Empty State -->
            <div v-else-if="!loading" class="empty-state">
                <div class="empty-icon">
                    <i class="bi bi-inbox"></i>
                </div>
                <h3 class="empty-title">
                    {{ searchTerm || filterScore !== 'all' ? 'Không tìm thấy kết quả' : 'Chưa có lịch sử làm bài' }}
                </h3>
                <p class="empty-subtitle">
                    {{ searchTerm || filterScore !== 'all'
                        ? 'Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm'
                        : 'Hãy bắt đầu làm quiz để xem lịch sử tại đây'
                    }}
                </p>
                <button v-if="searchTerm || filterScore !== 'all'" @click="searchTerm = ''; filterScore = 'all'"
                    class="btn-reset">
                    <i class="bi bi-arrow-clockwise"></i>
                    Xóa bộ lọc
                </button>
            </div>
        </div>
    </div>
</template>

<style scoped>
.quiz-history-container {
    min-height: 100vh;
    background: var(--app-background);
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

    0%,
    20%,
    50%,
    80%,
    100% {
        transform: translateY(0);
    }

    40% {
        transform: translateY(-10px);
    }

    60% {
        transform: translateY(-5px);
    }
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

/* Loading State */
.loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 400px;
    position: relative;
    z-index: 1;
}

.loading-spinner {
    display: flex;
    gap: 10px;
    margin-bottom: 2rem;
}

.spinner-ring {
    width: 20px;
    height: 20px;
    border: 3px solid rgba(255, 255, 255, 0.3);
    border-top: 3px solid white;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

.spinner-ring:nth-child(2) {
    animation-delay: 0.1s;
}

.spinner-ring:nth-child(3) {
    animation-delay: 0.2s;
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}

.loading-text {
    color: white;
    font-size: 1.5rem;
    font-weight: 600;
    margin-bottom: 0.5rem;
}

/* Error State */
.error-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 400px;
    position: relative;
    z-index: 1;
}

.error-card {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    border-radius: 20px;
    padding: 3rem;
    text-align: center;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    max-width: 400px;
}

.error-card i {
    font-size: 4rem;
    color: #ff4757;
    margin-bottom: 1.5rem;
}

.error-card h3 {
    color: #333;
    margin-bottom: 1rem;
    font-weight: 600;
}

.error-card p {
    color: #666;
    margin-bottom: 2rem;
}

.btn-back {
    background: linear-gradient(45deg, #667eea, #764ba2);
    color: white;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: 12px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
}

.btn-back:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
}

/* Main Content */
.main-content {
    max-width: 1200px;
    margin: 0 auto;
    position: relative;
    z-index: 1;
}

/* Stats Section */
.stats-section {
    margin-bottom: 3rem;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1.5rem;
    margin-bottom: 2rem;
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

.stat-card.primary .stat-icon {
    background: linear-gradient(45deg, #667eea, #764ba2);
}

.stat-card.success .stat-icon {
    background: linear-gradient(45deg, #2ed573, #1e90ff);
}

.stat-card.info .stat-icon {
    background: linear-gradient(45deg, #1e90ff, #667eea);
}

.stat-card.warning .stat-icon {
    background: linear-gradient(45deg, #ffa502, #ff6b9d);
}

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

/* Performance Chart */
.performance-chart {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    border-radius: 20px;
    padding: 2rem;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
}

.chart-title {
    color: #333;
    font-size: 1.5rem;
    font-weight: 700;
    margin-bottom: 2rem;
    text-align: center;
}

.chart-bars {
    display: flex;
    justify-content: center;
    align-items: end;
    gap: 2rem;
    height: 200px;
}

.chart-bar {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
}

.bar-fill {
    width: 40px;
    border-radius: 20px 20px 0 0;
    transition: height 1s ease;
    min-height: 20px;
}

.chart-bar.excellent .bar-fill {
    background: linear-gradient(to top, #2ed573, #a8e6cf);
}

.chart-bar.good .bar-fill {
    background: linear-gradient(to top, #1e90ff, #87ceeb);
}

.chart-bar.average .bar-fill {
    background: linear-gradient(to top, #ffa502, #ffd700);
}

.chart-bar.poor .bar-fill {
    background: linear-gradient(to top, #ff4757, #ff6b9d);
}

.bar-label {
    text-align: center;
}

.bar-count {
    display: block;
    font-size: 1.2rem;
    font-weight: 700;
    color: #333;
}

.bar-text {
    display: block;
    font-size: 0.8rem;
    color: #666;
    font-weight: 500;
}

/* Filters Section */
.filters-section {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    border-radius: 20px;
    padding: 1.5rem;
    margin-bottom: 2rem;
    display: flex;
    gap: 1.5rem;
    align-items: center;
    flex-wrap: wrap;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
}

.search-box {
    flex: 1;
    min-width: 250px;
    position: relative;
}

.search-box i {
    position: absolute;
    left: 1rem;
    top: 50%;
    transform: translateY(-50%);
    color: #666;
    font-size: 1.1rem;
}

.search-input {
    width: 100%;
    padding: 0.75rem 1rem 0.75rem 3rem;
    border: 2px solid #e0e0e0;
    border-radius: 12px;
    font-size: 1rem;
    transition: all 0.3s ease;
    background: white;
}

.search-input:focus {
    outline: none;
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.filter-controls {
    display: flex;
    gap: 1rem;
    flex-wrap: wrap;
}

.filter-select {
    padding: 0.75rem 1rem;
    border: 2px solid #e0e0e0;
    border-radius: 12px;
    font-size: 1rem;
    background: white;
    cursor: pointer;
    transition: all 0.3s ease;
    min-width: 150px;
}

.filter-select:focus {
    outline: none;
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* Timeline Section */
.timeline-section {
    position: relative;
}

.timeline {
    position: relative;
    padding-left: 2rem;
}

.timeline::before {
    content: '';
    position: absolute;
    left: 20px;
    top: 0;
    bottom: 0;
    width: 3px;
    background: linear-gradient(to bottom, #667eea, #764ba2);
    border-radius: 2px;
}

.timeline-item {
    position: relative;
    margin-bottom: 2rem;
    opacity: 0;
    transform: translateX(30px);
}

.timeline-item.animate-in {
    animation: slideIn 0.6s ease forwards;
}

@keyframes slideIn {
    to {
        opacity: 1;
        transform: translateX(0);
    }
}

.timeline-marker {
    position: absolute;
    left: -32px;
    top: 1.5rem;
    width: 40px;
    height: 40px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 1.2rem;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    z-index: 2;
}

.timeline-content {
    margin-left: 2rem;
}

.quiz-card {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    border-radius: 20px;
    overflow: hidden;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    transition: all 0.3s ease;
}

.quiz-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.card-header {
    padding: 1.5rem;
    border-bottom: 1px solid rgba(0, 0, 0, 0.1);
    display: flex;
    justify-content: between;
    align-items: flex-start;
    gap: 1rem;
}

.quiz-title {
    font-size: 1.3rem;
    font-weight: 700;
    color: #333;
    margin: 0;
    flex: 1;
}

.quiz-meta {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
}

.date-badge {
    background: rgba(102, 126, 234, 0.1);
    color: #667eea;
    padding: 0.25rem 0.75rem;
    border-radius: 20px;
    font-size: 0.8rem;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 0.25rem;
}

.card-body {
    padding: 1.5rem;
}

.score-section {
    display: flex;
    align-items: center;
    gap: 2rem;
    margin-bottom: 1.5rem;
}

.score-circle {
    width: 80px;
    height: 80px;
    border: 4px solid;
    border-radius: 50%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;
}

.score-value {
    font-size: 1.5rem;
    font-weight: 800;
    color: #333;
    line-height: 1;
}

.score-max {
    font-size: 0.7rem;
    color: #666;
    line-height: 1;
}

.score-details {
    flex: 1;
}

.score-label {
    font-size: 1.1rem;
    font-weight: 600;
    margin-bottom: 0.5rem;
}

.progress-bar {
    width: 100%;
    height: 8px;
    background: #e0e0e0;
    border-radius: 4px;
    overflow: hidden;
}

.progress-fill {
    height: 100%;
    border-radius: 4px;
    transition: width 1s ease;
}

.quiz-info {
    display: flex;
    gap: 2rem;
    flex-wrap: wrap;
}

.info-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: #666;
    font-size: 0.9rem;
}

.info-item i {
    color: #667eea;
}

/* Empty State */
.empty-state {
    text-align: center;
    padding: 4rem 2rem;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    border-radius: 20px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
}

.empty-icon {
    font-size: 4rem;
    color: #ccc;
    margin-bottom: 1.5rem;
}

.empty-title {
    font-size: 1.5rem;
    font-weight: 600;
    color: #333;
    margin-bottom: 1rem;
}

.empty-subtitle {
    color: #666;
    margin-bottom: 2rem;
    max-width: 400px;
    margin-left: auto;
    margin-right: auto;
}

.btn-reset {
    background: linear-gradient(45deg, #667eea, #764ba2);
    color: white;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: 12px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
}

.btn-reset:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
}

/* Responsive Design */
@media (max-width: 768px) {
    .quiz-history-container {
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

    .stat-card {
        padding: 1.5rem;
    }

    .filters-section {
        flex-direction: column;
        align-items: stretch;
    }

    .search-box {
        min-width: auto;
    }

    .filter-controls {
        justify-content: center;
    }

    .timeline {
        padding-left: 1rem;
    }

    .timeline::before {
        left: 10px;
    }

    .timeline-marker {
        left: -22px;
        width: 30px;
        height: 30px;
        font-size: 1rem;
    }

    .timeline-content {
        margin-left: 1rem;
    }

    .score-section {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
    }

    .quiz-info {
        flex-direction: column;
        gap: 1rem;
    }

    .card-header {
        flex-direction: column;
        align-items: flex-start;
    }
}

@media (max-width: 480px) {
    .hero-icon {
        font-size: 3rem;
    }

    .hero-title {
        font-size: 1.5rem;
    }

    .chart-bars {
        gap: 1rem;
    }

    .performance-chart {
        padding: 1rem;
    }

    .filters-section {
        padding: 1rem;
    }

    .filter-select {
        min-width: 120px;
    }
}
</style>