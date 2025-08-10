<template>
  <div class="global-leaderboard-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header">
        <h1 class="page-title">
          <i class="bi bi-trophy-fill text-warning me-3"></i>
          Bảng xếp hạng Toàn cầu
        </h1>
        <p class="page-description">
          Xem top những người chơi cao điểm nhất của tất cả quiz công khai
        </p>
      </div>

      <!-- Global Leaderboard Component -->
      <GlobalLeaderboard :limit="20" />
    </div>
  </div>
</template>

<script setup>
import GlobalLeaderboard from './GlobalLeaderboard.vue'

defineOptions({ name: 'GlobalLeaderboardPage' })
</script>

<style scoped>
.global-leaderboard-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.page-header {
  text-align: center;
  margin-bottom: 2rem;
  color: white;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 800;
  margin-bottom: 1rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.page-description {
  font-size: 1.1rem;
  opacity: 0.9;
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .page-title {
    font-size: 2rem;
  }
  
  .page-description {
    font-size: 1rem;
  }
  
  .global-leaderboard-page {
    padding: 1rem 0;
  }
}
</style>
