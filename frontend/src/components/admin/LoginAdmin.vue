<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()

const username = ref('')
const password = ref('')
const message = ref('')
const isLoading = ref(false)
const showPassword = ref(false)

const loginAdmin = async () => {
  message.value = ''
  isLoading.value = true
  try {
    const response = await axios.post('http://localhost:8080/api/admin/login', {
      username: username.value,
      password: password.value
    })

    if (response.data.status === 'SUCCESS') {
      localStorage.setItem('admin_user', JSON.stringify(response.data.user))
      router.push('/admin/dashboard')
    }
  } catch (err) {
    message.value = err.response?.data?.message || 'Đăng nhập thất bại!'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="login-wrapper d-flex justify-content-center align-items-center">
    <div class="login-box shadow-lg">
      <h3 class="text-center text-white mb-4">Sign In</h3>
      <form @submit.prevent="loginAdmin">
        <div class="form-group mb-3 position-relative">
          <input
            type="text"
            class="form-control ps-5"
            placeholder="Username"
            v-model="username"
            required
          />
          <i class="bi bi-person-fill input-icon"></i>
        </div>

        <div class="form-group mb-3 position-relative">
          <input
            :type="showPassword ? 'text' : 'password'"
            class="form-control ps-5 pe-5"
            placeholder="Password"
            v-model="password"
            required
          />
          <i class="bi bi-lock-fill input-icon"></i>
          <i
            class="bi"
            :class="showPassword ? 'bi-eye-slash-fill' : 'bi-eye-fill'"
            @click="showPassword = !showPassword"
            style="position: absolute; right: 15px; top: 50%; transform: translateY(-50%); cursor: pointer; color: #ccc;"
          ></i>
        </div>

        <div v-if="message" class="alert alert-danger py-2">
          {{ message }}
        </div>

        <button type="submit" class="btn btn-primary w-100 mb-3" :disabled="isLoading">
          {{ isLoading ? 'Đang đăng nhập...' : 'Login' }}
        </button>

        <p class="text-center text-white small">
          Don’t have an account? <a href="/register" class="text-decoration-none text-info">Sign Up</a>
        </p>
      </form>
    </div>
  </div>
</template>

<style scoped>
@import 'bootstrap-icons/font/bootstrap-icons.css';

.login-wrapper {
  height: 100vh;
  background: linear-gradient(to right, #4e54c8, #8f94fb);
}

.login-box {
  background-color: #1f1f2e;
  padding: 2rem;
  border-radius: 15px;
  width: 100%;
  max-width: 400px;
}

.input-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #aaa;
  font-size: 1.2rem;
}
</style>
