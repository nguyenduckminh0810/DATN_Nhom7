<template>
  <div class="leaderboard-container">
    <div class="leaderboard-header">
      <h3 class="leaderboard-title">
        <i class="bi bi-trophy text-warning"></i>
        {{ isQuizSpecific ? 'Bảng xếp hạng Quiz' : 'Bảng xếp hạng Toàn cầu' }}
      </h3>

      <!-- Period selector for global leaderboard -->
      <div v-if="!isQuizSpecific" class="period-selector">
        <button
          v-for="period in periods"
          :key="period.value"
          @click="selectedPeriod = period.value"
          :class="['period-btn', { active: selectedPeriod === period.value }]"
        >
          {{ period.label }}
        </button>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="loading" class="loading-container">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <p class="mt-2">Đang tải bảng xếp hạng...</p>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="error-container">
      <div class="alert alert-danger" role="alert">
        <i class="bi bi-exclamation-triangle"></i>
        {{ error }}
      </div>
    </div>

    <!-- Leaderboard content -->
    <div v-else-if="leaderboardData.length > 0" class="leaderboard-content">
      <div class="leaderboard-list">
        <div
          v-for="(entry, index) in leaderboardData"
          :key="entry.userId"
          :class="['leaderboard-entry', getRankClass(index)]"
        >
          <!-- Rank -->
          <div class="rank-container">
            <span class="rank-number">{{ index + 1 }}</span>
            <div class="rank-icon">
              <i v-if="index === 0" class="bi bi-trophy-fill text-warning"></i>
              <i v-else-if="index === 1" class="bi bi-trophy-fill text-secondary"></i>
              <i v-else-if="index === 2" class="bi bi-trophy-fill text-bronze"></i>
            </div>
            <!-- Special crown for #1 -->
            <div v-if="index === 0" class="crown-effect">
              <i class="bi bi-star-fill"></i>
            </div>
          </div>

          <!-- User info -->
          <div class="user-info">
            <div class="user-avatar">
              <img
                :src="entry.avatarUrl || '/img/default-avatar.png'"
                :alt="entry.fullName || entry.username"
                class="avatar-img"
              />
            </div>
            <div class="user-details">
              <h4 class="user-name">{{ entry.fullName || entry.username }}</h4>
              <div class="user-badges">
                <BadgeDisplay :badges="entry.badges" />
              </div>
            </div>
          </div>

          <!-- Score and time -->
          <div class="score-info">
            <div class="score">
              <span class="score-value">{{ entry.score }}</span>
              <span class="score-label">điểm</span>
            </div>
            <div v-if="entry.timeTaken" class="time-taken">
              <i class="bi bi-clock"></i>
              {{ formatTime(entry.timeTaken) }}
            </div>
          </div>
        </div>
      </div>

      <!-- Show more button -->
      <div v-if="leaderboardData.length >= limit" class="show-more">
        <button class="btn btn-outline-primary btn-sm">Xem thêm</button>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else class="empty-state">
      <div class="text-center">
        <i class="bi bi-inbox text-muted" style="font-size: 3rem"></i>
        <p class="mt-3 text-muted">Chưa có dữ liệu xếp hạng</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import axios from '@/utils/axios'
import BadgeDisplay from './BadgeDisplay.vue'

// Component name for linter
defineOptions({
  name: 'QuizLeaderboard',
})

// Props
const props = defineProps({
  quizId: {
    type: Number,
    default: null,
  },
  limit: {
    type: Number,
    default: 10,
  },
})

// Reactive data
const loading = ref(false)
const error = ref(null)
const leaderboardData = ref([])
const selectedPeriod = ref('all')

// Period options for global leaderboard
const periods = [
  { value: 'weekly', label: 'Tuần này' },
  { value: 'monthly', label: 'Tháng này' },
  { value: 'all', label: 'Tất cả thời gian' },
]

// Computed
const isQuizSpecific = computed(() => props.quizId !== null)

// Methods
const fetchLeaderboard = async () => {
  loading.value = true
  error.value = null

  try {
    let url = ''
    let params = { limit: props.limit }

    if (isQuizSpecific.value) {
      url = `/leaderboard/quiz/${props.quizId}`
    } else {
      url = '/leaderboard/global'
      params.period = selectedPeriod.value
    }

    const response = await axios.get(url, { params })
    leaderboardData.value = response.data
  } catch (err) {
    console.error('Error fetching leaderboard:', err)
    error.value = 'Không thể tải bảng xếp hạng. Vui lòng thử lại sau.'
  } finally {
    loading.value = false
  }
}

const getRankClass = (index) => {
  if (index === 0) return 'rank-first'
  if (index === 1) return 'rank-second'
  if (index === 2) return 'rank-third'
  return ''
}

const formatTime = (seconds) => {
  if (!seconds) return ''

  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60

  if (minutes > 0) {
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
  }
  return `${remainingSeconds}s`
}

// Watchers
watch(selectedPeriod, () => {
  if (!isQuizSpecific.value) {
    fetchLeaderboard()
  }
})

// Lifecycle
onMounted(() => {
  fetchLeaderboard()
})
</script>

<style scoped>
.leaderboard-container {
  background: linear-gradient(145deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  box-shadow:
    0 10px 30px rgba(0, 0, 0, 0.1),
    0 1px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.leaderboard-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  color: white;
  padding: 25px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  position: relative;
  overflow: hidden;
}

.leaderboard-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="50" cy="50" r="1" fill="rgba(255,255,255,0.1)"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  opacity: 0.3;
}

.leaderboard-title {
  margin: 0;
  font-size: 1.75rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 12px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1;
}

.leaderboard-title i {
  font-size: 1.5rem;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.period-selector {
  display: flex;
  gap: 10px;
  position: relative;
  z-index: 1;
}

.period-btn {
  background: rgba(255, 255, 255, 0.15);
  border: 2px solid rgba(255, 255, 255, 0.2);
  color: white;
  padding: 8px 16px;
  border-radius: 25px;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
}

.period-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.period-btn.active {
  background: rgba(255, 255, 255, 0.95);
  color: #667eea;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.loading-container,
.error-container,
.empty-state {
  padding: 50px 30px;
  text-align: center;
}

.leaderboard-content {
  padding: 0;
}

.leaderboard-list {
  max-height: 600px;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #cbd5e0 #f7fafc;
}

.leaderboard-list::-webkit-scrollbar {
  width: 6px;
}

.leaderboard-list::-webkit-scrollbar-track {
  background: #f7fafc;
}

.leaderboard-list::-webkit-scrollbar-thumb {
  background: #cbd5e0;
  border-radius: 3px;
}

.leaderboard-entry {
  display: flex;
  align-items: center;
  padding: 20px 30px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.leaderboard-entry:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  transform: translateX(5px);
}

.leaderboard-entry:last-child {
  border-bottom: none;
}

.rank-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 80px;
  margin-right: 20px;
  position: relative;
}

.rank-number {
  font-size: 1.5rem;
  font-weight: 800;
  color: #4a5568;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.rank-icon {
  margin-top: 4px;
  position: relative;
}

.rank-icon i {
  font-size: 1.4rem;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.text-bronze {
  color: #cd7f32;
}

.user-info {
  display: flex;
  align-items: center;
  flex: 1;
  gap: 15px;
}

.user-avatar {
  width: 55px;
  height: 55px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #e2e8f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  flex: 1;
}

.user-name {
  margin: 0 0 6px 0;
  font-size: 1.1rem;
  font-weight: 700;
  color: #2d3748;
  line-height: 1.2;
}

.user-badges {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.score-info {
  text-align: right;
  min-width: 120px;
}

.score {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.score-value {
  font-size: 1.5rem;
  font-weight: 800;
  color: #38a169;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  line-height: 1;
}

.score-label {
  font-size: 0.8rem;
  color: #718096;
  text-transform: uppercase;
  font-weight: 600;
  letter-spacing: 0.5px;
  margin-top: 2px;
}

.time-taken {
  font-size: 0.9rem;
  color: #718096;
  margin-top: 6px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  font-weight: 500;
}

.show-more {
  padding: 20px 30px;
  text-align: center;
  border-top: 1px solid rgba(226, 232, 240, 0.8);
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
}

.show-more .btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  padding: 10px 24px;
  border-radius: 25px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.show-more .btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* Enhanced Rank styling */
.rank-first {
  background: linear-gradient(135deg, #fff9c4 0%, #fff59d 50%, #fff176 100%);
  box-shadow: 0 4px 20px rgba(255, 193, 7, 0.3);
  border-left: 4px solid #ffc107;
}

.rank-second {
  background: linear-gradient(135deg, #f5f5f5 0%, #e0e0e0 50%, #d5d5d5 100%);
  box-shadow: 0 4px 20px rgba(158, 158, 158, 0.3);
  border-left: 4px solid #9e9e9e;
}

.rank-third {
  background: linear-gradient(135deg, #ffecb3 0%, #ffcc80 50%, #ffb74d 100%);
  box-shadow: 0 4px 20px rgba(205, 127, 50, 0.3);
  border-left: 4px solid #cd7f32;
}

.rank-first .rank-number {
  color: #f57f17;
  font-size: 1.75rem;
}

.rank-second .rank-number {
  color: #757575;
  font-size: 1.6rem;
}

.rank-third .rank-number {
  color: #cd7f32;
  font-size: 1.5rem;
}

.rank-first .rank-icon i {
  font-size: 1.6rem;
  color: #ffd700;
}

.rank-second .rank-icon i {
  font-size: 1.5rem;
  color: #c0c0c0;
}

.rank-third .rank-icon i {
  font-size: 1.4rem;
  color: #cd7f32;
}

/* Special crown effect for #1 */
.crown-effect {
  position: absolute;
  top: -8px;
  right: -8px;
  animation: crownGlow 2s ease-in-out infinite alternate;
}

.crown-effect i {
  font-size: 1rem;
  color: #ffd700;
  filter: drop-shadow(0 0 8px rgba(255, 215, 0, 0.8));
}

@keyframes crownGlow {
  0% {
    transform: scale(1) rotate(0deg);
    filter: drop-shadow(0 0 8px rgba(255, 215, 0, 0.8));
  }
  100% {
    transform: scale(1.1) rotate(5deg);
    filter: drop-shadow(0 0 12px rgba(255, 215, 0, 1));
  }
}

/* Animation for new entries */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.leaderboard-entry {
  animation: slideIn 0.5s ease-out;
}

/* Pulse animation for top ranks */
@keyframes pulse {
  0% {
    box-shadow: 0 4px 20px rgba(255, 193, 7, 0.3);
  }
  50% {
    box-shadow: 0 4px 25px rgba(255, 193, 7, 0.5);
  }
  100% {
    box-shadow: 0 4px 20px rgba(255, 193, 7, 0.3);
  }
}

.rank-first {
  animation: pulse 2s ease-in-out infinite;
}

/* Responsive design */
@media (max-width: 768px) {
  .leaderboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
    padding: 20px 25px;
  }

  .leaderboard-title {
    font-size: 1.5rem;
  }

  .period-selector {
    width: 100%;
    justify-content: space-between;
  }

  .period-btn {
    padding: 6px 12px;
    font-size: 0.8rem;
  }

  .leaderboard-entry {
    padding: 15px 20px;
  }

  .user-info {
    gap: 12px;
  }

  .user-avatar {
    width: 45px;
    height: 45px;
  }

  .user-name {
    font-size: 1rem;
  }

  .score-value {
    font-size: 1.3rem;
  }

  .time-taken {
    font-size: 0.8rem;
  }

  .rank-container {
    min-width: 60px;
    margin-right: 15px;
  }

  .rank-number {
    font-size: 1.3rem;
  }
}

@media (max-width: 480px) {
  .leaderboard-container {
    border-radius: 15px;
  }

  .leaderboard-header {
    padding: 15px 20px;
  }

  .leaderboard-title {
    font-size: 1.3rem;
  }

  .leaderboard-entry {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 15px 20px;
  }

  .rank-container {
    flex-direction: row;
    gap: 10px;
    margin-right: 0;
    min-width: auto;
  }

  .score-info {
    text-align: left;
    width: 100%;
  }

  .score {
    align-items: flex-start;
  }

  .time-taken {
    justify-content: flex-start;
  }

  .user-info {
    width: 100%;
  }
}
</style>
