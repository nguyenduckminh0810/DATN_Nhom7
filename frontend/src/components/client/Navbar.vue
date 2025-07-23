<script setup>
import { useRouter, RouterLink } from 'vue-router'
import { useLogin } from './useLogin'
import { computed, watch, onMounted, onUnmounted } from 'vue'

const { logout, username, message } = useLogin()
const router = useRouter()

const isLoggedIn = computed(() => !!message.value)

function login() {
  router.push('/login')
}

watch(message, (newVal) => {
  if (newVal === 'SUCCESS' && !username.value) {
    username.value = localStorage.getItem('username')
  }
})

function logoutForNavbar() {
  logout()
  router.push('/login')
}

let handleScroll

onMounted(() => {
  const header = document.getElementById('header')
  handleScroll = () => {
    if (window.scrollY > 10) {
      header?.classList.add('scrolled')
    } else {
      header?.classList.remove('scrolled')
    }
  }

  window.addEventListener('scroll', handleScroll)
  handleScroll()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>


<template>
  <header id="header" class="header d-flex align-items-center fixed-top">
    <div class="container-fluid container-xl position-relative d-flex align-items-center justify-content-between">

      <!-- Logo -->
      <RouterLink to="/" class="logo d-flex align-items-center">
        <h1 class="nabla">QuizMaster</h1>
      </RouterLink>


      <!-- Menu -->
      <nav class="navmenu d-flex align-items-center">
        <ul>
          <li><a href="#hero">Home</a></li>

          <li class="dropdown">
            <a href="#"><span>Dropdown</span> <i class="bi bi-chevron-down toggle-dropdown"></i></a>
            <ul>
              <li><a href="#">Dropdown 1</a></li>
              <li class="dropdown">
                <a href="#"><span>Deep Dropdown</span> <i class="bi bi-chevron-down toggle-dropdown"></i></a>
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
          <!-- Login/Logout -->
          <li v-if="!isLoggedIn">
            <a @click.prevent="login" class="btn btn-outline-primary px-4 py-2">Đăng nhập</a>
          </li>
          <li v-else>
            <a @click.prevent="logoutForNavbar" class="btn btn-primary px-4 py-2">Đăng xuất</a>
          </li>

        </ul>
      </nav>

    </div>
  </header>
</template>


<style scope>
.navmenu a,
.navmenu a:hover,
.navmenu a:focus,
.navmenu a:active,
.logo a,
.logo a:hover,
.logo a:focus,
.logo a:active {
  text-decoration: none !important;
}


.logo,
.logo a {
  text-decoration: none;
}

.nabla {
  font-family: "Nabla", system-ui;
  font-optical-sizing: auto;
  font-weight: 400;
  font-style: normal;
  font-variation-settings:
    "EDPT" 100,
    "EHLT" 12;
}


.nabla:hover {
  animation: glitch 0.5s linear;
  text-shadow:
    2px 0 red,
    -2px 0 rgb(1, 225, 255),
    0 0 12px rgba(255, 110, 108, 0.6);
}

@keyframes glitch {
  0% {
    transform: translate(0);
  }

  15% {
    transform: translate(-2px, 1px);
  }

  30% {
    transform: translate(2px, -1px);
  }

  45% {
    transform: translate(-1px, 2px);
  }

  60% {
    transform: translate(1px, -2px);
  }

  75% {
    transform: translate(0.5px, 1px);
  }

  100% {
    transform: translate(0);
  }
}

.header {
  background-color: rgba(255, 255, 255, 0.1);
  /* hoặc rgba(0, 0, 0, 0.3) nếu muốn tối */
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  /* hỗ trợ Safari */
  color: rgb(0, 0, 0);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  padding: 10px 0;
}

.header.scrolled {
  background-color: rgba(0, 124, 240, 0.3);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  transition: background-color 0.3s ease, backdrop-filter 0.3s ease;
}


.navmenu ul li>a {
  color: rgb(0, 0, 0);
  transition: color 0.3s ease;
}

.navmenu ul li>a:hover {
  color: #000000;
}

.navmenu ul li ul {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 8px;
  padding: 10px 0;
  transition: background 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.navmenu ul li ul li a {
  color: rgb(2, 2, 2);
  padding: 10px 20px;
  display: block;
  white-space: nowrap;
}

.navmenu ul li ul li a:hover {
  background-color: rgba(255, 255, 255, 0.15);
  color: #000000;
  border-radius: 5px;
}

.btn {
  font-weight: 500;
  border-radius: 30px;
}

.navmenu a {
  text-decoration: none;
}

@media (max-width: 991px) {
  .header {
    padding: 15px;
  }
}

/* Loại bỏ gạch chân khi hover ở menu */
.navmenu ul li:hover {
  text-decoration: none;
}
</style>
