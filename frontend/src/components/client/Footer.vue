<template>
 <footer class="footer bg-dark text-light pt-3 pb-2">
    <div class="container">
      <div class="row gy-3">
        <!-- Logo & Contact -->
        <div class="col-lg-4 col-md-6 mb-3">
          <div class="d-flex align-items-center mb-2">
            <img src="/img/logo.png" alt="QuizMaster Logo" style="height: 32px; margin-right: 8px;" />
            <span class="h5 mb-0 fw-bold">QuizMaster</span>
          </div>
          <p class="mb-1 small"><i class="bi bi-geo-alt-fill me-2"></i>271 Le Thanh Tong, HaiPhong, Vietnam</p>
          <p class="mb-1 small"><i class="bi bi-envelope-fill me-2"></i>quizmaster@example.com</p>
          <p class="mb-2 small"><i class="bi bi-telephone-fill me-2"></i>+84 123 456 789</p>
          <div class="d-flex gap-2">
  <a href="#" class="btn btn-outline-light btn-sm rounded-circle"><i class="bi bi-facebook"></i></a> <!-- Chuyển về btn-outline-light -->
  <a href="#" class="btn btn-outline-light btn-sm rounded-circle"><i class="bi bi-instagram"></i></a>
  <a href="#" class="btn btn-outline-light btn-sm rounded-circle"><i class="bi bi-linkedin"></i></a>
</div>
        </div>
       <!-- Quick Links -->
<div class="col-lg-2 col-md-3 mb-3">
  <h6 class="fw-bold mb-2">Liên kết nhanh</h6>
  <ul class="list-unstyled">
    <li><a href="/" class="text-light text-decoration-none small">Trang chủ</a></li> <!-- Chuyển về text-light -->
    <li><a href="/about" class="text-light text-decoration-none small">Về chúng tôi</a></li>
    <li><a href="/quizzes" class="text-light text-decoration-none small">Quiz công khai</a></li>
    <li><a href="/contact" class="text-light text-decoration-none small">Liên hệ</a></li>
  </ul>
</div>
        <!-- About Project -->
        <div class="col-lg-3 col-md-3 mb-3">
          <h6 class="fw-bold mb-2">Về QuizMaster</h6>
          <p class="small">
            QuizMaster là nền tảng thi trắc nghiệm trực tuyến, giúp bạn tạo, chia sẻ và kiểm tra kiến thức dễ dàng, nhanh chóng và hiệu quả.
          </p>
        </div>
        <!-- Newsletter -->
        <div class="col-lg-3 col-md-12">
          <h6 class="fw-bold mb-2">Nhận tin mới</h6>
          <form @submit.prevent="subscribe">
            <div class="input-group mb-2">
              <input v-model="email" type="email" class="form-control form-control-sm" placeholder="Nhập email của bạn" required>
              <button class="btn btn-primary btn-sm" type="submit">Đăng ký</button>
            </div>
            <div v-if="message" :class="{'text-success': success, 'text-danger': !success}" class="small">{{ message }}</div>
          </form>
        </div>
      </div>
      <hr class="bg-secondary my-3" />
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
  font-size: 0.9rem;
  background: linear-gradient(90deg, #0072ff 0%, #00c6ff 100%);
  color: #fff;
}
.footer a:hover {
  text-decoration: underline;
  color: #ffc107 !important;
}
</style>