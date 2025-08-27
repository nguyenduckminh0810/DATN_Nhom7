<script setup>
import api from '@/utils/axios'
import { ref } from 'vue'

const emailOrUsername = ref('')
const code = ref('')
const newPassword = ref('')
const confirm = ref('')
const error = ref('')
const ok = ref(false)
const loading = ref(false)

async function reset() {
  error.value = ''
  if (!emailOrUsername.value || !code.value || !newPassword.value) {
    error.value = 'Vui lòng nhập đầy đủ thông tin.'
    return
  }
  if (newPassword.value.length < 6 || newPassword.value !== confirm.value) {
    error.value = 'Mật khẩu tối thiểu 6 ký tự và cần trùng khớp.'
    return
  }
  loading.value = true
  try {
    await api.post('/auth/verify-reset-code', {
      emailOrUsername: emailOrUsername.value.trim(),
      code: code.value.trim(),
      newPassword: newPassword.value
    })
    ok.value = true
  } catch {
    error.value = 'Mã OTP không hợp lệ hoặc đã hết hạn.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="card">
    <h2>Đặt lại mật khẩu</h2>
    <div v-if="ok">Đổi mật khẩu thành công! Hãy đăng nhập với mật khẩu mới.</div>
    <template v-else>
      <input v-model="emailOrUsername" placeholder="Email hoặc Tên đăng nhập" />
      <input v-model="code" placeholder="Mã OTP 6 số" />
      <input v-model="newPassword" placeholder="Mật khẩu mới" type="password" />
      <input v-model="confirm" placeholder="Xác nhận mật khẩu mới" type="password" />
      <button :disabled="loading" @click="reset">Cập nhật</button>
      <p v-if="error" style="color:#e53e3e">{{ error }}</p>
    </template>
  </div>
</template>

<style scoped>
.card {
  max-width: 420px;
  margin: 2rem auto;
  display: grid;
  gap: .75rem;
}

input {
  padding: .75rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

button {
  padding: .75rem 1rem;
  border-radius: 8px;
  border: none;
  background: #667eea;
  color: #fff;
  cursor: pointer;
}

button:disabled {
  opacity: .6;
  cursor: not-allowed;
}
</style>
