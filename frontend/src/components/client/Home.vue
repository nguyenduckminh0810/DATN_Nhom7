<template>
  <div class="homepage">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-container">
        <div class="hero-content">
          <div class="hero-badge">
            <i class="bi bi-star-fill"></i>
            <span>Platform Quiz #1 Việt Nam</span>
          </div>

          <h1 class="hero-title">
            Khám phá thế giới
            <span class="highlight">Quiz</span>
            cùng QuizMaster
          </h1>

          <p class="hero-description">
            Tham gia hàng ngàn quiz thú vị, thử thách kiến thức và cải thiện kỹ năng của bạn. Từ
            khoa học đến nghệ thuật, chúng tôi có tất cả!
          </p>

          <div class="hero-actions">
            <RouterLink to="/register" class="btn-primary">
              <i class="bi bi-rocket-takeoff"></i>
              Bắt đầu ngay
            </RouterLink>
            <RouterLink to="/login" class="btn-secondary">
              <i class="bi bi-box-arrow-in-right"></i>
              Đăng nhập
            </RouterLink>
          </div>

          <div class="hero-stats">
            <div class="stat-item">
              <div class="stat-number">10K+</div>
              <div class="stat-label">Quiz</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">50K+</div>
              <div class="stat-label">Người dùng</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">1M+</div>
              <div class="stat-label">Lượt chơi</div>
            </div>
          </div>
        </div>

        <div class="hero-visual">
          <div class="floating-cards">
            <div class="quiz-card card-1">
              <i class="bi bi-lightbulb"></i>
              <span>Khoa học</span>
            </div>
            <div class="quiz-card card-2">
              <i class="bi bi-palette"></i>
              <span>Nghệ thuật</span>
            </div>
            <div class="quiz-card card-3">
              <i class="bi bi-globe"></i>
              <span>Địa lý</span>
            </div>
            <div class="quiz-card card-4">
              <i class="bi bi-calculator"></i>
              <span>Toán học</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="features-section">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">Tại sao chọn QuizMaster?</h2>
          <p class="section-subtitle">
            Trải nghiệm học tập thú vị và hiệu quả với những tính năng tuyệt vời
          </p>
        </div>

        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <i class="bi bi-lightning-charge"></i>
            </div>
            <h3 class="feature-title">Tốc độ nhanh</h3>
            <p class="feature-description">
              Giao diện mượt mà, tải nhanh. Trải nghiệm không gián đoạn.
            </p>
          </div>

          <div class="feature-card">
            <div class="feature-icon">
              <i class="bi bi-shield-check"></i>
            </div>
            <h3 class="feature-title">An toàn bảo mật</h3>
            <p class="feature-description">
              Dữ liệu được bảo vệ tối đa. Quyền riêng tư được đảm bảo.
            </p>
          </div>

          <div class="feature-card">
            <div class="feature-icon">
              <i class="bi bi-people"></i>
            </div>
            <h3 class="feature-title">Cộng đồng lớn</h3>
            <p class="feature-description">Tham gia cộng đồng hơn 50,000 người dùng active.</p>
          </div>

          <RouterLink to="/global-leaderboard" class="feature-card">
            <div class="feature-icon">
              <i class="bi bi-trophy"></i>
            </div>
            <h3 class="feature-title">Bảng xếp hạng</h3>
            <p class="feature-description">Cạnh tranh lành mạnh với bảng xếp hạng realtime.</p>
          </RouterLink>

          <div class="feature-card">
            <div class="feature-icon">
              <i class="bi bi-graph-up"></i>
            </div>
            <h3 class="feature-title">Theo dõi tiến độ</h3>
            <p class="feature-description">Thống kê chi tiết để theo dõi quá trình học tập.</p>
          </div>

          <div class="feature-card">
            <div class="feature-icon">
              <i class="bi bi-device-tablet"></i>
            </div>
            <h3 class="feature-title">Đa nền tảng</h3>
            <p class="feature-description">Sử dụng mọi lúc mọi nơi trên mọi thiết bị.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section">
      <div class="cta-container">
        <div class="cta-content">
          <h2 class="cta-title">Sẵn sàng thử thách bản thân?</h2>
          <p class="cta-description">Tham gia ngay hôm nay và khám phá tiềm năng vô hạn của bạn!</p>
          <div class="cta-actions">
            <RouterLink to="/register" class="cta-btn primary">
              <i class="bi bi-person-plus"></i>
              Đăng ký miễn phí
            </RouterLink>
            <RouterLink to="/quiz-crud" class="cta-btn secondary">
              <i class="bi bi-collection"></i>
              Khám phá Quiz
            </RouterLink>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'

onMounted(() => {
  console.log('✅ Home component mounted successfully!')
})
</script>

<style scoped>
/* === IMPROVED COLOR PALETTE === */

/* Global Homepage Styles */
.homepage {
  min-height: 100vh;
  background: var(--app-background);
  overflow-x: hidden;
}

/* Hero Section */
.hero-section {
  min-height: 100vh;
  display: flex;
  align-items: center;
  padding: 120px 20px 60px;
  position: relative;
  /* Add subtle overlay for better text contrast */
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.95) 0%, rgba(118, 75, 162, 0.95) 100%);
}

.hero-container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.hero-content {
  z-index: 2;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(255, 215, 0, 0.3);
  border-radius: 50px;
  padding: 8px 20px;
  color: #2d3748; /* Dark text instead of white */
  font-size: 0.9rem;
  font-weight: 600;
  margin-bottom: 30px;
  animation: fadeInUp 0.6s ease-out;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 800;
  color: #1a202c; /* Dark color for better contrast */
  line-height: 1.2;
  margin-bottom: 24px;
  animation: fadeInUp 0.6s ease-out 0.2s both;
  text-shadow: 2px 2px 4px rgba(255, 255, 255, 0.3);
}

.highlight {
  background: linear-gradient(45deg, #ffd700, #ffed4e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.hero-description {
  font-size: 1.2rem;
  color: #2d3748; /* Dark text */
  line-height: 1.6;
  margin-bottom: 40px;
  animation: fadeInUp 0.6s ease-out 0.4s both;
  background: rgba(255, 255, 255, 0.9);
  padding: 20px;
  border-radius: 15px;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 50px;
  animation: fadeInUp 0.6s ease-out 0.6s both;
}

.btn-primary,
.btn-secondary {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 16px 32px;
  border-radius: 50px;
  text-decoration: none;
  font-weight: 600;
  font-size: 1.1rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.btn-primary {
  background: linear-gradient(45deg, #ffd700, #ffed4e);
  color: #1a202c; /* Dark text */
  box-shadow: 0 8px 25px rgba(255, 215, 0, 0.4);
  border: 2px solid #ffd700;
}

.btn-primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 15px 40px rgba(255, 215, 0, 0.5);
  background: linear-gradient(45deg, #ffed4e, #ffd700);
  color: #1a202c;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.95);
  color: #2d3748; /* Dark text */
  border: 2px solid rgba(255, 215, 0, 0.5);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 1);
  border-color: #ffd700;
  transform: translateY(-3px);
  color: #1a202c;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.hero-stats {
  display: flex;
  gap: 40px;
  animation: fadeInUp 0.6s ease-out 0.8s both;
}

.stat-item {
  text-align: center;
  background: rgba(255, 255, 255, 0.9);
  padding: 20px;
  border-radius: 15px;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  border: 2px solid rgba(255, 215, 0, 0.3);
}

.stat-number {
  font-size: 2.5rem;
  font-weight: 800;
  color: #e53e3e; /* Red accent for numbers */
  line-height: 1;
}

.stat-label {
  font-size: 0.9rem;
  color: #4a5568; /* Dark gray */
  margin-top: 4px;
  font-weight: 600;
}

/* Hero Visual */
.hero-visual {
  position: relative;
  height: 500px;
}

.floating-cards {
  position: relative;
  width: 100%;
  height: 100%;
}

.quiz-card {
  position: absolute;
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid rgba(255, 215, 0, 0.4);
  border-radius: 20px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #2d3748; /* Dark text */
  font-weight: 600;
  animation-duration: 3s;
  animation-iteration-count: infinite;
  animation-timing-function: ease-in-out;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.quiz-card i {
  font-size: 2rem;
  color: #e53e3e; /* Red accent for icons */
}

.card-1 {
  top: 20%;
  left: 10%;
  animation-name: float1;
}

.card-2 {
  top: 10%;
  right: 20%;
  animation-name: float2;
  animation-delay: -0.5s;
}

.card-3 {
  bottom: 30%;
  left: 20%;
  animation-name: float3;
  animation-delay: -1s;
}

.card-4 {
  bottom: 10%;
  right: 10%;
  animation-name: float4;
  animation-delay: -1.5s;
}

/* Features Section */
.features-section {
  padding: 100px 20px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 80px;
  background: rgba(255, 255, 255, 0.9);
  padding: 40px;
  border-radius: 25px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 3rem;
  font-weight: 700;
  color: #1a202c; /* Dark text */
  margin-bottom: 20px;
}

.section-subtitle {
  font-size: 1.2rem;
  color: #4a5568; /* Dark gray */
  max-width: 600px;
  margin: 0 auto;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 30px;
}

.feature-card {
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid rgba(255, 215, 0, 0.3);
  border-radius: 24px;
  padding: 40px 30px;
  text-align: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  text-decoration: none;
  display: block;
  color: inherit;
}

.feature-card:hover {
  transform: translateY(-10px);
  background: rgba(255, 255, 255, 1);
  border-color: #ffd700;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(45deg, #e53e3e, #fc8181);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
}

.feature-icon i {
  font-size: 2rem;
  color: white;
}

.feature-title {
  font-size: 1.4rem;
  font-weight: 600;
  color: #1a202c; /* Dark text */
  margin-bottom: 16px;
}

.feature-description {
  color: #4a5568; /* Dark gray */
  line-height: 1.6;
}

/* CTA Section */
.cta-section {
  padding: 100px 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.cta-container {
  max-width: 800px;
  margin: 0 auto;
  text-align: center;
  background: rgba(255, 255, 255, 0.9);
  padding: 60px 40px;
  border-radius: 30px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
}

.cta-title {
  font-size: 3rem;
  font-weight: 700;
  color: #1a202c; /* Dark text */
  margin-bottom: 20px;
}

.cta-description {
  font-size: 1.2rem;
  color: #4a5568; /* Dark gray */
  margin-bottom: 40px;
}

.cta-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.cta-btn {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 18px 36px;
  border-radius: 50px;
  text-decoration: none;
  font-weight: 600;
  font-size: 1.1rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.cta-btn.primary {
  background: linear-gradient(45deg, #ffd700, #ffed4e);
  color: #1a202c; /* Dark text */
  box-shadow: 0 8px 25px rgba(255, 215, 0, 0.4);
  border: 2px solid #ffd700;
}

.cta-btn.primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 15px 40px rgba(255, 215, 0, 0.5);
  background: linear-gradient(45deg, #ffed4e, #ffd700);
  color: #1a202c;
}

.cta-btn.secondary {
  background: linear-gradient(45deg, #e53e3e, #fc8181);
  color: white;
  border: 2px solid #e53e3e;
  box-shadow: 0 8px 25px rgba(229, 62, 62, 0.3);
}

.cta-btn.secondary:hover {
  background: linear-gradient(45deg, #fc8181, #e53e3e);
  transform: translateY(-3px);
  color: white;
  box-shadow: 0 15px 40px rgba(229, 62, 62, 0.4);
}

/* Animations */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes float1 {
  0%,
  100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(2deg);
  }
}

@keyframes float2 {
  0%,
  100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-15px) rotate(-2deg);
  }
}

@keyframes float3 {
  0%,
  100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-25px) rotate(1deg);
  }
}

@keyframes float4 {
  0%,
  100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-18px) rotate(-1deg);
  }
}

/* Responsive Design */
@media (max-width: 768px) {
  .hero-container {
    grid-template-columns: 1fr;
    gap: 40px;
    text-align: center;
  }

  .hero-title {
    font-size: 2.5rem;
  }

  .hero-actions {
    flex-direction: column;
    align-items: center;
  }

  .hero-stats {
    flex-direction: column;
    gap: 20px;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .cta-actions {
    flex-direction: column;
    align-items: center;
  }

  .floating-cards {
    display: none;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 2rem;
  }

  .section-title {
    font-size: 2rem;
  }

  .cta-title {
    font-size: 2rem;
  }
}
</style>
