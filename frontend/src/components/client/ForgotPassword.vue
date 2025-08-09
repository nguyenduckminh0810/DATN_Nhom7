<script setup>
import api from '@/utils/axios'
import { ref } from 'vue'

// Step 1
const emailOrUsername = ref('')
const message = ref('')
const loading = ref(false)
const error = ref('')
const step = ref(1)
const resendCountdown = ref(0)
let timerId

// Step 2 (OTP + đổi mật khẩu)
const code = ref('')
const newPassword = ref('')
const confirm = ref('')
const success = ref(false)

function startCountdown(seconds = 60) {
  resendCountdown.value = seconds
  clearInterval(timerId)
  timerId = setInterval(() => {
    if (resendCountdown.value <= 0) return clearInterval(timerId)
    resendCountdown.value -= 1
  }, 1000)
}

async function sendOtp() {
  error.value = ''
  message.value = ''
  if (!emailOrUsername.value.trim()) {
    error.value = 'Vui lòng nhập Email hoặc Tên đăng nhập'
    return
  }
  loading.value = true
  try {
    await api.post('/auth/forgot-password-code', { emailOrUsername: emailOrUsername.value.trim() })
    message.value = 'Nếu tài khoản hợp lệ, mã OTP đã được gửi tới email của bạn.'
    step.value = 2
    startCountdown(60)
  } catch (e) {
    // vẫn chuyển step để người dùng nhập mã (tránh lộ thông tin)
    message.value = 'Nếu tài khoản hợp lệ, mã OTP đã được gửi tới email của bạn.'
    step.value = 2
    startCountdown(60)
  } finally {
    loading.value = false
  }
}

async function verifyAndReset() {
  error.value = ''
  message.value = ''
  if (!code.value.trim()) {
    error.value = 'Vui lòng nhập mã OTP 6 số'
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
    success.value = true
    message.value = 'Đổi mật khẩu thành công! Hãy đăng nhập với mật khẩu mới.'
  } catch (e) {
    error.value = 'Mã OTP không hợp lệ hoặc đã hết hạn.'
  } finally {
    loading.value = false
  }
}

async function resendOtp() {
  if (resendCountdown.value > 0) return
  await sendOtp()
}
</script>

<template>
  <div class="fp-container">
    <div class="fp-card">
      <div class="fp-header">
        <div class="fp-icon"><i class="bi bi-shield-lock"></i></div>
        <h2>Quên mật khẩu</h2>
        <p v-if="step===1">Nhập email hoặc tên đăng nhập để nhận mã OTP.</p>
        <p v-else>Nhập mã OTP và mật khẩu mới để hoàn tất.</p>
      </div>

      <!-- Step 1: yêu cầu mã -->
      <div v-if="step===1" class="fp-body">
        <label class="fp-label">Email hoặc Tên đăng nhập</label>
        <input class="fp-input" v-model="emailOrUsername" placeholder="Email hoặc Tên đăng nhập" />
        <button class="fp-btn primary" :disabled="loading || !emailOrUsername.trim()" @click="sendOtp">
          <span v-if="!loading">Gửi mã OTP</span>
          <span v-else>Đang gửi...</span>
        </button>
      </div>

      <!-- Step 2: nhập OTP + đổi mật khẩu -->
      <div v-else class="fp-body">
        <div class="fp-note">Mã OTP đã được gửi tới: <strong>{{ emailOrUsername }}</strong></div>
        <label class="fp-label">Mã OTP</label>
        <input class="fp-input" maxlength="6" v-model="code" placeholder="Nhập mã 6 số" />

        <label class="fp-label">Mật khẩu mới</label>
        <input class="fp-input" type="password" v-model="newPassword" placeholder="Mật khẩu mới" />

        <label class="fp-label">Xác nhận mật khẩu</label>
        <input class="fp-input" type="password" v-model="confirm" placeholder="Nhập lại mật khẩu" />

        <button class="fp-btn primary" :disabled="loading" @click="verifyAndReset">
          <span v-if="!loading">Cập nhật mật khẩu</span>
          <span v-else>Đang xử lý...</span>
        </button>

        <button class="fp-btn ghost" :disabled="resendCountdown>0 || loading" @click="resendOtp">
          <span v-if="resendCountdown===0">Gửi lại mã</span>
          <span v-else>Gửi lại sau {{ resendCountdown }}s</span>
        </button>
      </div>

      <div v-if="error" class="fp-alert error"><i class="bi bi-exclamation-triangle"></i> {{ error }}</div>
      <div v-if="message" class="fp-alert" :class="{ success: success }"><i class="bi bi-info-circle"></i> {{ message }}</div>
    </div>
  </div>
</template>

<style scoped>
.fp-container{
  min-height: 100vh;
  display:flex;align-items:center;justify-content:center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px;
}
.fp-card{
  width:100%;max-width:520px;
  background: rgba(255,255,255,0.08);
  backdrop-filter: blur(16px);
  border:1px solid rgba(255,255,255,0.25);
  border-radius: 20px;
  padding: 24px 24px 20px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.25);
}
.fp-header{ text-align:center; margin-bottom: 16px; color:#fff; }
.fp-icon{ width:60px;height:60px;border-radius:50%;margin:0 auto 8px;display:flex;align-items:center;justify-content:center;background:linear-gradient(45deg,#667eea,#764ba2);border:2px solid rgba(255,255,255,.8); box-shadow:0 10px 30px rgba(102,126,234,.35); color:#fff; font-size:1.6rem; }
.fp-body{ display:grid; gap:10px; }
.fp-label{ color:#fff; font-weight:600; font-size:.95rem; }
.fp-input{ padding:12px 14px; border-radius:12px; border:2px solid rgba(255,255,255,.6); background: rgba(0,0,0,.35); color:#fff; }
.fp-input::placeholder{ color: rgba(255,255,255,.7); }
.fp-btn{ padding:12px 16px; border-radius:12px; border:none; cursor:pointer; font-weight:700; }
.fp-btn.primary{ background: linear-gradient(135deg,#667eea,#764ba2); color:#fff; box-shadow:0 12px 30px rgba(102,126,234,.35); }
.fp-btn.primary:disabled{ opacity:.65; cursor:not-allowed; }
.fp-btn.ghost{ background: transparent; color:#fff; border:2px solid rgba(255,255,255,.6); }
.fp-note{ color:#fff; opacity:.9; font-size:.9rem; margin-bottom:4px; }
.fp-alert{ margin-top:12px; padding:10px 12px; border-radius:10px; background: rgba(255,255,255,.15); color:#fff; border:1px solid rgba(255,255,255,.35); display:flex; gap:8px; align-items:center; }
.fp-alert.error{ background: rgba(239,83,80,.2); border-color: rgba(239,83,80,.6); }
.fp-alert.success{ background: rgba(76,175,80,.2); border-color: rgba(76,175,80,.6); }
@media (max-width: 480px){ .fp-card{ padding:20px 16px; } }
</style>


