<template>
  <div class="badge-container">
    <div
      v-for="badge in badges"
      :key="badge"
      class="badge-item"
      :class="{ 'badge-earned': isEarned }"
      @click="showBadgeDetail(badge)"
    >
      <div class="badge-icon">{{ getBadgeIcon(badge) }}</div>
      <div class="badge-tooltip" v-if="showTooltip">
        {{ getBadgeDescription(badge) }}
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'

export default {
  name: 'BadgeDisplay',
  props: {
    badges: {
      type: Array,
      default: () => [],
    },
    isEarned: {
      type: Boolean,
      default: false,
    },
  },
  setup() {
    const showTooltip = ref(false)

    const badgeMap = {
      '💯 Perfect Score': {
        icon: '💯',
        description: 'Đạt 100% chính xác trong bài quiz',
      },
      '⚡ Speed Demon': {
        icon: '⚡',
        description: 'Hoàn thành bài quiz nhanh nhất',
      },
      '🥉 Top 3': {
        icon: '🥉',
        description: 'Nằm trong top 3 người chơi',
      },
      '🔥 Daily Streak': {
        icon: '🔥',
        description: 'Làm quiz liên tiếp 3 ngày',
      },
      '🏆 Master': {
        icon: '🏆',
        description: 'Tổng điểm đạt 1000+',
      },
      '🥇 Expert': {
        icon: '🥇',
        description: 'Tổng điểm đạt 500+',
      },
      '📚 Dedicated': {
        icon: '📚',
        description: 'Đã làm 50+ bài quiz',
      },
      '📖 Active': {
        icon: '📖',
        description: 'Đã làm 20+ bài quiz',
      },
    }

    const getBadgeIcon = (badge) => {
      return badgeMap[badge]?.icon || badge
    }

    const getBadgeDescription = (badge) => {
      return badgeMap[badge]?.description || badge
    }

    const showBadgeDetail = (badge) => {
      // TODO: Implement badge detail modal
      console.log('Badge clicked:', badge)
    }

    return {
      showTooltip,
      getBadgeIcon,
      getBadgeDescription,
      showBadgeDetail,
    }
  },
}
</script>

<style scoped>
.badge-container {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.badge-item {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.badge-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    45deg,
    transparent 30%,
    rgba(255, 255, 255, 0.2) 50%,
    transparent 70%
  );
  transform: translateX(-100%);
  transition: transform 0.6s ease;
}

.badge-item:hover {
  transform: scale(1.15) rotate(5deg);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
  border-color: rgba(255, 255, 255, 0.6);
}

.badge-item:hover::before {
  transform: translateX(100%);
}

.badge-item.badge-earned {
  animation: badgeEarned 0.8s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.badge-icon {
  font-size: 14px;
  color: white;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.4);
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.2));
  z-index: 1;
  position: relative;
}

.badge-tooltip {
  position: absolute;
  bottom: 120%;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #2c3e50, #34495e);
  color: white;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 11px;
  white-space: nowrap;
  z-index: 1000;
  margin-bottom: 8px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
}

.badge-item:hover .badge-tooltip {
  opacity: 1;
  visibility: visible;
  transform: translateX(-50%) translateY(-5px);
}

.badge-tooltip::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 6px solid transparent;
  border-top-color: #2c3e50;
}

@keyframes badgeEarned {
  0% {
    transform: scale(0) rotate(-180deg);
    opacity: 0;
  }
  50% {
    transform: scale(1.3) rotate(10deg);
    opacity: 1;
  }
  75% {
    transform: scale(0.9) rotate(-5deg);
  }
  100% {
    transform: scale(1) rotate(0deg);
    opacity: 1;
  }
}

/* Enhanced badge styles with better gradients */
.badge-item:nth-child(1) {
  background: linear-gradient(135deg, #ffd700, #ffed4e, #ffc107);
  box-shadow: 0 2px 8px rgba(255, 193, 7, 0.4);
}

.badge-item:nth-child(2) {
  background: linear-gradient(135deg, #c0c0c0, #e5e5e5, #b0b0b0);
  box-shadow: 0 2px 8px rgba(192, 192, 192, 0.4);
}

.badge-item:nth-child(3) {
  background: linear-gradient(135deg, #cd7f32, #daa520, #b8860b);
  box-shadow: 0 2px 8px rgba(205, 127, 50, 0.4);
}

.badge-item:nth-child(4) {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24, #ff4757);
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.4);
}

.badge-item:nth-child(5) {
  background: linear-gradient(135deg, #667eea, #764ba2, #5a67d8);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}

.badge-item:nth-child(6) {
  background: linear-gradient(135deg, #f093fb, #f5576c, #e91e63);
  box-shadow: 0 2px 8px rgba(240, 147, 251, 0.4);
}

.badge-item:nth-child(7) {
  background: linear-gradient(135deg, #4facfe, #00f2fe, #2196f3);
  box-shadow: 0 2px 8px rgba(79, 172, 254, 0.4);
}

.badge-item:nth-child(8) {
  background: linear-gradient(135deg, #43e97b, #38f9d7, #4caf50);
  box-shadow: 0 2px 8px rgba(67, 233, 123, 0.4);
}

/* Special effects for top badges */
.badge-item:nth-child(1):hover {
  box-shadow: 0 6px 20px rgba(255, 193, 7, 0.6);
}

.badge-item:nth-child(2):hover {
  box-shadow: 0 6px 20px rgba(192, 192, 192, 0.6);
}

.badge-item:nth-child(3):hover {
  box-shadow: 0 6px 20px rgba(205, 127, 50, 0.6);
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .badge-item {
    width: 24px;
    height: 24px;
  }

  .badge-icon {
    font-size: 12px;
  }

  .badge-container {
    gap: 4px;
  }
}

@media (max-width: 480px) {
  .badge-item {
    width: 22px;
    height: 22px;
  }

  .badge-icon {
    font-size: 11px;
  }
}
</style>
