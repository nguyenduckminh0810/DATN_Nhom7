<template>
  <div class="leaderboard-container">
    <div class="leaderboard-header">
      <h2 class="leaderboard-title">
        <span class="trophy-icon">🏆</span>
        Bảng xếp hạng
      </h2>

      <div class="period-selector">
        <button
          v-for="period in periods"
          :key="period.value"
          @click="changePeriod(period.value)"
          :class="['period-btn', { active: currentPeriod === period.value }]"
        >
          {{ period.label }}
        </button>
      </div>
    </div>

    <div class="leaderboard-content">
      <!-- Loading state -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>Đang tải bảng xếp hạng...</p>
      </div>

      <!-- Error state -->
      <div v-else-if="error" class="error-container">
        <p class="error-message">{{ error }}</p>
        <button @click="loadLeaderboard" class="retry-btn">Thử lại</button>
      </div>

      <!-- Leaderboard list -->
      <div v-else class="leaderboard-list">
        <div
          v-for="(entry, index) in leaderboard"
          :key="entry.userId"
          class="leaderboard-entry"
          :class="getRankClass(index)"
        >
          <!-- Rank -->
          <div class="rank-section">
            <div class="rank-number">{{ index + 1 }}</div>
            <div class="rank-medal" v-if="index < 3">
              {{ getMedalIcon(index) }}
            </div>
          </div>

          <!-- User info -->
          <div class="user-section">
            <div class="user-avatar">
              <img
                :src="entry.avatarUrl || '/img/default-avatar.png'"
                :alt="entry.fullName"
                @error="handleImageError"
              />
            </div>
            <div class="user-details">
              <h4 class="user-name">{{ entry.fullName || entry.username }}</h4>
              <div class="user-badges">
                <BadgeDisplay :badges="entry.badges" />
              </div>
            </div>
          </div>

          <!-- Score -->
          <div class="score-section">
            <div class="score-value">{{ entry.score }}</div>
            <div class="score-label">điểm</div>
          </div>

          <!-- Time (if available) -->
          <div class="time-section" v-if="entry.timeTaken > 0">
            <div class="time-value">{{ formatTime(entry.timeTaken) }}</div>
            <div class="time-label">thời gian</div>
          </div>
        </div>

        <!-- Empty state -->
        <div v-if="leaderboard.length === 0" class="empty-state">
          <div class="empty-icon">📊</div>
          <p>Chưa có dữ liệu xếp hạng</p>
        </div>
      </div>
    </div>

    <!-- User rank info -->
    <div v-if="userRank > 0" class="user-rank-info">
      <div class="user-rank-card">
        <div class="user-rank-title">Vị trí của bạn</div>
        <div class="user-rank-number">#{{ userRank }}</div>
        <div class="user-rank-period">{{ getPeriodLabel(currentPeriod) }}</div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from "vue";
import axios from "@/utils/axios";
import BadgeDisplay from "./BadgeDisplay.vue";

export default {
  name: "Leaderboard",
  props: {
    quizId: {
      type: Number,
      default: null,
    },
    limit: {
      type: Number,
      default: 10,
    },
  },
  setup(props) {
    const leaderboard = ref([]);
    const loading = ref(false);
    const error = ref(null);
    const currentPeriod = ref("weekly");
    const userRank = ref(0);

    const periods = [
      { value: "weekly", label: "Tuần này" },
      { value: "monthly", label: "Tháng này" },
      { value: "all-time", label: "Tất cả thời gian" },
    ];

    const loadLeaderboard = async () => {
      loading.value = true;
      error.value = null;

      try {
        let url = props.quizId
          ? `/api/leaderboard/quiz/${props.quizId}?limit=${props.limit}`
          : `/api/leaderboard/global?period=${currentPeriod.value}&limit=${props.limit}`;

        console.log("🔍 Calling leaderboard API:", url);
        const response = await axios.get(url);
        console.log("✅ Leaderboard response:", response.data);

        // Backend trả về trực tiếp List<LeaderboardEntry>, không có wrapper
        leaderboard.value = response.data || [];

        // Load user rank if not quiz-specific
        if (!props.quizId) {
          await loadUserRank();
        }
      } catch (err) {
        console.error("❌ Leaderboard error:", err);
        if (err.response?.status === 500) {
          error.value = "Lỗi server. Vui lòng thử lại sau.";
        } else if (err.response?.status === 404) {
          error.value = "Không tìm thấy dữ liệu xếp hạng.";
        } else {
          error.value = "Không thể tải bảng xếp hạng. Vui lòng thử lại.";
        }
      } finally {
        loading.value = false;
      }
    };

    const loadUserRank = async () => {
      try {
        // TODO: Get current user ID from store/auth
        const userId = 1; // Placeholder
        const response = await axios.get(
          `/api/leaderboard/user/${userId}/rank?period=${currentPeriod.value}`
        );
        userRank.value = response.data;
      } catch (err) {
        console.error("User rank error:", err);
      }
    };

    const changePeriod = (period) => {
      currentPeriod.value = period;
      loadLeaderboard();
    };

    const getRankClass = (index) => {
      if (index === 0) return "rank-first";
      if (index === 1) return "rank-second";
      if (index === 2) return "rank-third";
      return "rank-normal";
    };

    const getMedalIcon = (index) => {
      const medals = ["🥇", "🥈", "🥉"];
      return medals[index] || "";
    };

    const formatTime = (seconds) => {
      const minutes = Math.floor(seconds / 60);
      const remainingSeconds = seconds % 60;
      return `${minutes}:${remainingSeconds.toString().padStart(2, "0")}`;
    };

    const getPeriodLabel = (period) => {
      const periodMap = {
        weekly: "Tuần này",
        monthly: "Tháng này",
        "all-time": "Tất cả thời gian",
      };
      return periodMap[period] || period;
    };

    const handleImageError = (event) => {
      event.target.src = "/img/default-avatar.png";
    };

    onMounted(() => {
      loadLeaderboard();
    });

    return {
      leaderboard,
      loading,
      error,
      currentPeriod,
      userRank,
      periods,
      loadLeaderboard,
      changePeriod,
      getRankClass,
      getMedalIcon,
      formatTime,
      getPeriodLabel,
      handleImageError,
    };
  },
};
</script>

<style scoped>
.leaderboard-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.leaderboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f0f0;
}

.leaderboard-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.trophy-icon {
  font-size: 28px;
}

.period-selector {
  display: flex;
  gap: 8px;
}

.period-btn {
  padding: 8px 16px;
  border: 2px solid #e1e8ed;
  background: #fff;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
}

.period-btn:hover {
  border-color: #3498db;
  color: #3498db;
}

.period-btn.active {
  background: #3498db;
  border-color: #3498db;
  color: #fff;
}

.loading-container {
  text-align: center;
  padding: 40px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.error-container {
  text-align: center;
  padding: 40px;
}

.error-message {
  color: #e74c3c;
  margin-bottom: 16px;
}

.retry-btn {
  padding: 8px 16px;
  background: #3498db;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.leaderboard-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.leaderboard-entry {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.leaderboard-entry:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.rank-first {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  border: 2px solid #ffd700;
}

.rank-second {
  background: linear-gradient(135deg, #c0c0c0, #e5e5e5);
  border: 2px solid #c0c0c0;
}

.rank-third {
  background: linear-gradient(135deg, #cd7f32, #daa520);
  border: 2px solid #cd7f32;
}

.rank-section {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 60px;
}

.rank-number {
  font-size: 20px;
  font-weight: 700;
  color: #2c3e50;
}

.rank-medal {
  font-size: 24px;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  flex: 1;
}

.user-name {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.user-badges {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.badge {
  padding: 2px 6px;
  background: #3498db;
  color: #fff;
  border-radius: 12px;
  font-size: 10px;
  font-weight: 500;
}

.score-section {
  text-align: center;
  min-width: 80px;
}

.score-value {
  font-size: 20px;
  font-weight: 700;
  color: #27ae60;
}

.score-label {
  font-size: 12px;
  color: #7f8c8d;
  text-transform: uppercase;
}

.time-section {
  text-align: center;
  min-width: 80px;
}

.time-value {
  font-size: 16px;
  font-weight: 600;
  color: #34495e;
}

.time-label {
  font-size: 12px;
  color: #7f8c8d;
  text-transform: uppercase;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #7f8c8d;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.user-rank-info {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 2px solid #f0f0f0;
}

.user-rank-card {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: #fff;
  padding: 16px;
  border-radius: 8px;
  text-align: center;
}

.user-rank-title {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 4px;
}

.user-rank-number {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 4px;
}

.user-rank-period {
  font-size: 12px;
  opacity: 0.8;
}

@media (max-width: 768px) {
  .leaderboard-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .period-selector {
    justify-content: center;
  }

  .leaderboard-entry {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }

  .user-section {
    flex-direction: column;
  }

  .score-section,
  .time-section {
    min-width: auto;
  }
}
</style>
