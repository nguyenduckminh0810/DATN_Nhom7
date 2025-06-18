
<script setup>
import router from '@/router'
import { RouterLink } from 'vue-router'

function login() {
  // Redirect to login page
  router.push('/login')
}


</script>
<template scoped>
  <nav class="navbar navbar-expand-lg navbar-light bg-light">
    <div class="container-fluid">
      <RouterLink to="/" class="navbar-brand">QuizApp</RouterLink>

      <button
        class="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#navbarSupportedContent"
        aria-controls="navbarSupportedContent"
        aria-expanded="false"
        aria-label="Toggle navigation"
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <div class="collapse navbar-collapse" id="navbarSupportedContent">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0">
          <li class="nav-item">
            <RouterLink to="/" class="nav-link">Trang chủ</RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink to="/quiz" class="nav-link">Làm Quiz</RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink to="/result" class="nav-link">Kết quả</RouterLink>
          </li>
        </ul>

        <div class="d-flex">
          <button class="btn btn-outline-primary me-2" @click="login">Đăng nhập</button>
          <button class="btn btn-primary" @click="logout">Đăng xuất</button>
        </div>
      </div>
    </div>
  </nav>
</template>
