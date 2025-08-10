<template>
  <div class="global-leaderboard-page">
    <div class="container">
      <!-- Spacer dưới navbar -->
      <div class="nav-spacer"></div>

      <!-- Page Header -->
      <header class="page-header" aria-label="Tiêu đề trang Bảng xếp hạng">
        <div class="page-title-wrap">
          <i class="bi bi-trophy-fill"></i>
          <h1 class="page-title">Bảng xếp hạng Toàn cầu</h1>
        </div>
        <p class="page-description">
          Xem top những người chơi cao điểm nhất của tất cả quiz công khai
        </p>
      </header>

      <GlobalLeaderboard :limit="20" />
    </div>
  </div>
</template>

<script setup>
import GlobalLeaderboard from './GlobalLeaderboard.vue'
defineOptions({ name: 'GlobalLeaderboardPage' })
</script>

<style scoped>
:root {
  --nav-h: 72px;
}

/* Nền trang */
.global-leaderboard-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* khung giữa */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

/* đẩy nội dung xuống dưới navbar */
.nav-spacer {
  height: calc(var(--nav-h) + 8px);
}

/* ===== Header glass, KHÔNG nền trắng ===== */
.page-header {
  text-align: center;
  color: #fff;
  margin: 16px 0 20px;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 16px;
  backdrop-filter: blur(12px);
  box-shadow: 0 6px 22px rgba(0, 0, 0, 0.15);
}

/* cụm tiêu đề */
.page-title-wrap {
  display: inline-flex;
  align-items: center;
  gap: 0.6rem;
}

/* DIỆT TRẮNG triệt để cho H1 */
h1.page-title,
h1.page-title * {
  background: transparent !important;
  box-shadow: none !important;
  padding: 0 !important;
}
.page-title {
  margin: 0;
  font-size: 2.2rem;
  font-weight: 900;
  letter-spacing: 0.2px;
  color: #fff !important;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.28);
}

/* icon cúp */
.page-title-wrap > i {
  font-size: 1.6rem;
  color: #ffd54f;
  filter: drop-shadow(0 2px 3px rgba(0, 0, 0, 0.25));
}

/* mô tả */
.page-description {
  margin: 0.6rem auto 0;
  max-width: 720px;
  font-size: 1.06rem;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.92);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.18);
}

/* Nếu có class bg-* nào bị gán nhầm trong header -> ép clear luôn */
.page-header [class*='bg-'] {
  background: transparent !important;
}

/* mobile */
@media (max-width: 768px) {
  .nav-spacer {
    height: calc(var(--nav-h) + 4px);
  }
  .page-header {
    padding: 12px;
  }
  .page-title {
    font-size: 1.8rem;
  }
  .page-title-wrap > i {
    font-size: 1.4rem;
  }
  .page-description {
    font-size: 0.98rem;
  }
}
</style>
