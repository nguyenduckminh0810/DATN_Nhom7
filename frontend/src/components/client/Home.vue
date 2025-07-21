<template>
  <section
    class="hero-section text-center text-light d-flex flex-column justify-content-center align-items-center vh-100"
  >
    <!-- SVG nền -->
    <svg
      class="hero-background-svg"
      xmlns="http://www.w3.org/2000/svg"
      width="100%"
      height="100%"
      viewBox="0 0 500 410"
      fill="none"
    >
      <path
        d="M344.95 213.185L199.168 213.185C162.854 213.185 133.914 207.985 112.962 193.967C92.3928 180.205 79.364 157.834 74.7451 123.056L74.5313 121.391L74.5303 121.385C73.9129 116.638 73.6023 111.728 73.5088 106.748L73.5098 106.748L73.5098 103.787C73.4166 57.1363 88.6625 28.6635 110.006 15.3935L110.019 15.3847L110.031 15.3769C110.694 14.9358 111.008 14.0133 110.637 13.2275L110.638 13.2265L36.1094 -150.775L36.1094 -150.776C35.8088 -151.441 35.8099 -152.52 36.1651 -153.461C36.5316 -154.433 37.0779 -154.815 37.5303 -154.815L67.1055 -154.815L249.356 -153.507L320.367 -153.507C321.331 -153.507 322.143 -152.8 322.31 -151.873L322.334 -151.685L346.885 211.105L346.885 211.111L346.89 211.32C346.857 212.355 346.018 213.185 344.95 213.185Z"
        stroke="currentColor"
        stroke-width="2"
      />
      <path
        d="M-601.845 -36.8321L-641.279 333.337C-641.365 334.139 -640.927 335.048 -639.896 335.812C-638.941 336.52 -637.576 337.012 -636.056 337.053L-635.749 337.054L-635.738 337.054L-128.973 335.308L-128.972 335.308C-45.3758 335.063 21.9625 318.297 69.9976 286.426C117.997 254.579 146.795 207.604 153.28 146.732C159.764 85.8627 140.98 38.932 99.8548 7.23318C59.3088 -24.0194 -3.08653 -40.5704 -84.7775 -40.8464L-88.6812 -40.8468L-88.6822 -40.8469L-597.348 -39.48L-597.351 -39.4803C-598.641 -39.4801 -599.765 -39.141 -600.564 -38.6254C-601.366 -38.1083 -601.778 -37.4627 -601.845 -36.8321Z"
        stroke="currentColor"
        stroke-width="2"
      />
      <path
        d="M246 98.8437L246 218.987C246 273.626 262.327 317.099 292.472 346.912C322.618 376.726 366.703 393 422.455 393C478.237 393 522.345 376.726 552.506 346.911C582.665 317.098 599 273.626 599 218.987L599 98.8437C599 97.8173 598.179 97 597.174 97L247.826 97C246.821 97 246 97.8173 246 98.8437Z"
        stroke="currentColor"
        stroke-width="2"
      />
    </svg>

    <!-- Nội dung chính -->
    <p class="hero-subtitle">
      Chúng tôi là sinh viên – và chúng tôi xây dựng công cụ học tập dành cho chính bạn.
    </p>

    <h1 class="hero-title">Quiz Master</h1>
    <p class="hero-description">
      QuizMaster giúp bạn thực hành và cải thiện kiến thức một cách hiệu quả.<br />
      Nền tảng học tập thông minh dành cho học sinh & giáo viên.
    </p>
    <ul class="hero-benefits list-unstyled mt-3">
      <li>✅ Ôn luyện mọi lúc, mọi nơi</li>
      <li>✅ Câu hỏi phong phú</li>
      <li>✅ Dễ dùng</li>
    </ul>

    <button class="btn btn-primary mt-4 px-4 py-2">Bắt đầu ngay</button>
  </section>
</template>
<FooterComponent />

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
.hero-background-svg {
  position: absolute;
  top: 0;
  left: 0;
  width: 120%;
  height: 120%;
  z-index: 0;
  opacity: 0.06;
  color: #f6e7dc; /* Màu nét của SVG, đổi tùy theme */
  pointer-events: none; /* Để không ảnh hưởng click */
  animation: float-bg 15s infinite linear;
}
@keyframes float-bg {
  0% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(-20px, -10px) scale(1.05);
  }
  100% {
    transform: translate(0, 0) scale(1);
  }
}
@media (max-width: 576px) {
  .hero-title {
    font-size: 2.5rem;
  }

  .hero-description {
    font-size: 1rem;
  }

  button.btn {
    font-size: 1.2rem;
  }
}
.hero-benefits {
  font-size: 1rem;
  color: #ddd;
  z-index: 1;
}
.hero-benefits li {
  margin: 5px 0;
}
</style>
<script></script>
