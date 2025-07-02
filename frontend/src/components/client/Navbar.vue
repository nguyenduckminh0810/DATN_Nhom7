<script setup>
import { useRouter, RouterLink } from 'vue-router'

const router = useRouter()

function login() {
  router.push('/login')
}

function logout() {
  console.log('Đăng xuất')
  // TODO: thêm xóa token nếu cần
}
</script>

<template>
  <header id="header" class="header d-flex align-items-center fixed-top">
    <div class="container-fluid container-xl position-relative d-flex align-items-center justify-content-between">

      <!-- Logo -->
      <RouterLink to="/" class="logo d-flex align-items-center">
        <h1 class="sitename">Bootslander</h1>
      </RouterLink>

      <nav id="navmenu" class="navmenu">
        <ul>
          <li><a href="#hero" class="active">Home</a></li>
          <li><a href="#about">About</a></li>
          <li><a href="#features">Features</a></li>
          <li><a href="#gallery">Gallery</a></li>
          <li><a href="#team">Team</a></li>
          <li><a href="#pricing">Pricing</a></li>

          <li class="dropdown">
            <a href="#"><span>Dropdown</span> <i class="bi bi-chevron-down toggle-dropdown"></i></a>
            <ul>
              <li><a href="#">Dropdown 1</a></li>
              <li class="dropdown"><a href="#"><span>Deep Dropdown</span> <i
                    class="bi bi-chevron-down toggle-dropdown"></i></a>
                <ul>
                  <li><a href="#">Deep Dropdown 1</a></li>
                  <li><a href="#">Deep Dropdown 2</a></li>
                  <li><a href="#">Deep Dropdown 3</a></li>
                  <li><a href="#">Deep Dropdown 4</a></li>
                  <li><a href="#">Deep Dropdown 5</a></li>
                </ul>
              </li>
              <li><a href="#">Dropdown 2</a></li>
              <li><a href="#">Dropdown 3</a></li>
              <li><a href="#">Dropdown 4</a></li>
            </ul>
          </li>

          <li><a href="#contact">Contact</a></li>

          <!-- Login/Logout -->
          <li>
            <a href="#" @click.prevent="login" class="btn btn-outline-primary px-4 py-2">Đăng nhập</a>
          </li>
          <li class="ms-2">
            <a href="#" @click.prevent="logout" class="btn btn-primary px-4 py-2">Đăng xuất</a>
          </li>

        </ul>
        <i class="mobile-nav-toggle d-xl-none bi bi-list"></i>
      </nav>

    </div>
  </header>
</template>

<style scoped>
/* Header: tăng padding để header cao hơn */
.header {
  background: linear-gradient(90deg, #007cf0, #00dfd8);
  color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  padding: 20px 0;
  /* Tăng độ cao */
}

/* Logo màu trắng */
.logo h1 {
  color: white;
  margin: 0;
}

/* Menu links mặc định trắng */
.navmenu ul li>a {
  color: white;
  transition: color 0.3s ease;
}

/* Hover màu vàng */
.navmenu ul li>a:hover {
  color: #ffd700;
}

/* Dropdown menu - gradient background */
.navmenu ul li ul {
  background: linear-gradient(135deg, #667eea, #764ba2);
  /* tím xanh */
  border-radius: 8px;
  padding: 10px 0;
  transition: background 0.3s ease;
}

/* Dropdown menu items */
.navmenu ul li ul li a {
  color: white;
  padding: 10px 20px;
  display: block;
  white-space: nowrap;
}

/* Hover submenu items */
.navmenu ul li ul li a:hover {
  background-color: rgba(255, 255, 255, 0.15);
  /* semi-transparent white */
  color: #ffd700;
  border-radius: 5px;
}

/* Optional: dropdown shadow */
.navmenu ul li ul {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

/* Buttons: login/logout */
.btn {
  font-weight: 500;
  border-radius: 30px;
}

/* Responsive spacing fix */
@media (max-width: 991px) {
  .header {
    padding: 15px;
  }
}
</style>
