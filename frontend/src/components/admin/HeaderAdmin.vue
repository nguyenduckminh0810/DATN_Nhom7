<template>
  <header class="admin-header d-flex align-items-center justify-content-between px-4 py-3">
    <div class="d-flex align-items-center">
      <img src="/img/logo.png" alt="QuizMaster Logo" class="me-2" style="height: 36px;" />
      <span class="h4 mb-0 fw-bold text-white">QuizMaster</span>
    </div>
    <nav>
      <ul class="nav">
        <li class="nav-item" v-for="item in menu" :key="item.label">
          <a :href="item.link" class="nav-link text-white fw-semibold">{{ item.label }}</a>
        </li>
      </ul>
    </nav>
    <button class="btn btn-outline-light btn-sm" @click="logout">Đăng xuất</button>
  </header>
</template>

<script setup>
const menu = [
  { label: 'Dashboard', link: '/admin/dashboard' },
  { label: 'Quản lý Quiz', link: '/admin/all-quizzes' },
  { label: 'Người dùng', link: '/admin/all-users' },
  { label: 'Thể loại', link: '/admin/categories' },
]

function logout() {
  // Xử lý logout
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  window.location.href = '/admin/login'
}
</script>

<style scoped>
.admin-header {
  background: linear-gradient(90deg, #0072ff 0%, #00c6ff 100%);
  color: #fff;
  min-height: 60px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
}
.admin-header .nav-link {
  color: #fff !important;
  margin-right: 1rem;
  transition: color 0.2s;
}
.admin-header .nav-link:hover {
  color: #ffc107 !important;
  text-decoration: underline;
}
</style>