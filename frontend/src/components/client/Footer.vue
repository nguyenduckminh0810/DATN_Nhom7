<template>
  <footer class="footer bg-dark text-light pt-5 pb-3">
    <div class="container">
      <div class="row gy-4">
        <!-- Logo & Contact -->
        <div class="col-lg-4 col-md-6 mb-4">
          <div class="d-flex align-items-center mb-2">
            <img src="/img/logo.png" alt="QuizMaster Logo" style="height: 40px; margin-right: 10px;" />
            <span class="h4 mb-0 fw-bold">QuizMaster</span>
          </div>
          <p class="mb-1"><i class="bi bi-geo-alt-fill me-2"></i>271 Le Thanh Tong, HaiPhong, Vietnam</p>
          <p class="mb-1"><i class="bi bi-envelope-fill me-2"></i>quizmaster@example.com</p>
          <p class="mb-3"><i class="bi bi-telephone-fill me-2"></i>+84 123 456 789</p>
          <div class="d-flex gap-2">
            <a href="#" class="btn btn-outline-light btn-sm rounded-circle"><i class="bi bi-facebook"></i></a>
            <a href="#" class="btn btn-outline-light btn-sm rounded-circle"><i class="bi bi-instagram"></i></a>
            <a href="#" class="btn btn-outline-light btn-sm rounded-circle"><i class="bi bi-linkedin"></i></a>
          </div>
        </div>
        <!-- Quick Links -->
        <div class="col-lg-2 col-md-3 mb-4">
          <h5 class="fw-bold mb-3">Liên kết nhanh</h5>
          <ul class="list-unstyled">
            <li><a href="/" class="text-light text-decoration-none">Trang chủ</a></li>
            <li><a href="/about" class="text-light text-decoration-none">Về chúng tôi</a></li>
            <li><a href="/quizzes" class="text-light text-decoration-none">Quiz công khai</a></li>
            <li><a href="/contact" class="text-light text-decoration-none">Liên hệ</a></li>
          </ul>
        </div>
        <!-- About Project -->
        <div class="col-lg-3 col-md-3 mb-4">
          <h5 class="fw-bold mb-3">Về QuizMaster</h5>
          <p>
            QuizMaster là nền tảng thi trắc nghiệm trực tuyến, giúp bạn tạo, chia sẻ và kiểm tra kiến thức dễ dàng, nhanh chóng và hiệu quả.
          </p>
        </div>
        <!-- Newsletter -->
        <div class="col-lg-3 col-md-12">
          <h5 class="fw-bold mb-3">Nhận tin mới</h5>
          <form @submit.prevent="subscribe">
            <div class="input-group mb-2">
              <input v-model="email" type="email" class="form-control" placeholder="Nhập email của bạn" required>
              <button class="btn btn-primary" type="submit">Đăng ký</button>
            </div>
            <div v-if="message" :class="{'text-success': success, 'text-danger': !success}">{{ message }}</div>
          </form>
        </div>
      </div>
      <hr class="bg-secondary my-4" />
      <div class="text-center small">
        © 2024 <strong>QuizMaster</strong>. All Rights Reserved.
      </div>
    </div>
  </footer>
</template>

<script setup>
import { ref } from 'vue'
const email = ref('')
const message = ref('')
const success = ref(false)

function subscribe() {
  // Giả lập gửi email
  if (email.value) {
    message.value = 'Đăng ký nhận tin thành công!'
    success.value = true
    email.value = ''
  } else {
    message.value = 'Vui lòng nhập email hợp lệ.'
    success.value = false
  }
}
</script>

<style scoped>
.footer {
  font-size: 1rem;
  background: linear-gradient(90deg, #0072ff 0%, #00c6ff 100%);
  color: #fff;
}
.footer a:hover {
  text-decoration: underline;
  color: #ffc107 !important;
}
</style>