<template>
  <header class="navbar navbar-expand-lg navbar-light bg-white border-bottom shadow-sm px-4 py-2">
    <!-- Logo / Tên admin panel -->
    <a class="navbar-brand fw-bold text-dark" href="/admin/dashboard">Quiz Master</a>

    <!-- Spacer -->
    <div class="flex-grow-1"></div>

    <!-- Thanh chức năng bên phải -->
    <ul class="navbar-nav ms-auto d-flex align-items-center gap-3">
      <!-- Nút tìm kiếm -->
      <li class="nav-item d-none d-md-block">
        <form class="d-flex">
          <input class="form-control form-control-sm" type="search" placeholder="Search" aria-label="Search" />
        </form>
      </li>

      <!-- Icon thông báo -->
      <li class="nav-item position-relative">
        <i class="bi bi-bell fs-5 text-dark" style="cursor: pointer;"></i>
        <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
          3
        </span>
      </li>

      <!-- Avatar & Dropdown -->
      <li class="nav-item dropdown">

        <a class="nav-link dropdown-toggle d-flex align-items-center" href="#" id="navbarDropdown" role="button"
          data-bs-toggle="dropdown" aria-expanded="false">
          <img src="/img/anh-mo-ta.jpg" class="rounded-circle me-2" alt="avatar"
            style="width: 32px; height: 32px; object-fit: cover;" />

          <span class="fw-medium text-dark">Admin</span>
        </a>
        <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="navbarDropdown">
          <li><a class="dropdown-item" href="#">Thông tin tài khoản</a></li>
          <li>
            <hr class="dropdown-divider" />
          </li>
          <li>
            <a class="dropdown-item text-danger" href="#" @click.prevent="logout">Đăng xuất</a>
          </li>
        </ul>
      </li>
    </ul>
  </header>
</template>

<script setup>
const logout = () => {
  localStorage.removeItem('admin_user')
  window.location.href = '/admin/login'
}
</script>

<style scoped>
.navbar-brand {
  font-size: 1.25rem;
}

.navbar-nav .nav-item i:hover {
  color: #0d6efd;
}
</style>
