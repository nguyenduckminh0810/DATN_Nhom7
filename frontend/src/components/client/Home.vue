<template>
  <section
    class="hero-section text-center text-light d-flex flex-column justify-content-center align-items-center vh-100"
  >
    <!-- <p class="hero-subtitle">Quizizz is now</p> -->
    <h1 class="hero-title">Quiz Master</h1>
    <p class="hero-description">
      QuizMaster giúp bạn thực hành và cải thiện kiến thức một cách hiệu quả.<br />
      Nền tảng học tập thông minh dành cho học sinh & giáo viên.
    </p>
    <button class="btn btn-primary mt-4 px-4 py-2">Bắt đầu ngay</button>
  </section>
  <!-- <FooterComponent /> -->
</template>

<script setup>
// Không cần script thêm ở đây nếu chỉ là giao diện
</script>

<style scoped>
.hero-section {
  background: radial-gradient(circle at center, #d3d3d3 0%, #000000 100%);
  padding: 60px 20px;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

.hero-subtitle {
  font-size: 1.5rem;
  font-weight: 300;
  margin-bottom: 0.5rem;
  color: #f6e7dc;
  opacity: 0.8;
}

.hero-title {
  font-size: 4rem;
  font-weight: 900;
  letter-spacing: 2px;
  color: #625020;
  text-transform: uppercase;
  margin-bottom: 1rem;
}

.hero-description {
  font-size: 1.2rem;
  font-weight: 400;
  color: #f6e7dc;
  max-width: 700px;
  line-height: 1.6;
}

button.btn {
  font-size: 2rem;
  border-radius: 10px;
  transition: all 0.3s ease;
}

button.btn:hover {
  transform: scale(1.05);
  background-color: #4b3d1f;
  border-color: #4b3d1f;
}
</style>
<script>
import FooterComponent from '@/components/client/Footer.vue' // hoặc đường dẫn đúng của bạn
</script>
