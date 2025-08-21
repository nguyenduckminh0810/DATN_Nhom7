<script setup>
import { useRegin } from './useRegin'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

defineOptions({ name: 'UserRegister' })

const {
  status,
  username,
  email,
  password,
  confirmPassword,
  fullName,
  // avatarUrl,  // ⟵ removed
  bio,
  message,
  isLoading,
  register,
} = useRegin()

const router = useRouter()

// Method để chuyển hướng đến dashboard
const goToDashboard = () => {
  if (localStorage.getItem('token')) {
    router.push('/dashboard')
  } else {
    router.push('/login')
  }
}

const showPassword = ref(false)
const showConfirmPassword = ref(false)
const avatarPreview = ref('')
const avatarFile = ref(null)

/* Password strength */
const passwordStrength = computed(() => {
  if (!password.value) return 0
  let strength = 0
  const pass = password.value
  if (pass.length >= 8) strength++
  if (pass.length >= 12) strength++
  if (/[a-z]/.test(pass)) strength++
  if (/[A-Z]/.test(pass)) strength++
  if (/[0-9]/.test(pass)) strength++
  if (/[^A-Za-z0-9]/.test(pass)) strength++
  if (pass.length >= 16) strength++
  return Math.min(strength, 5)
})

const passwordMatch = computed(() => {
  if (!confirmPassword.value) return null
  return password.value === confirmPassword.value
})

/* File-only avatar upload */
const handleAvatarFile = (event) => {
  const file = event.target.files[0]
  if (file) {
    avatarFile.value = file
    const reader = new FileReader()
    reader.onload = (e) => {
      avatarPreview.value = e.target.result
    }
    reader.readAsDataURL(file)
    // no URL mode anymore
  }
}

const getStrengthColor = (strength) => {
  switch (strength) {
    case 1:
      return '#ff4757'
    case 2:
      return '#ffa502'
    case 3:
      return '#2ed573'
    case 4:
      return '#1e90ff'
    case 5:
      return '#8e44ad'
    default:
      return '#ddd'
  }
}

const getStrengthText = (strength) => {
  switch (strength) {
    case 1:
      return 'Yếu'
    case 2:
      return 'Trung bình'
    case 3:
      return 'Mạnh'
    case 4:
      return 'Rất mạnh'
    case 5:
      return 'Xuất sắc'
    default:
      return ''
  }
}

const clearAvatar = () => {
  avatarPreview.value = ''
  avatarFile.value = null
  const fileInput = document.getElementById('avatarFile')
  if (fileInput) fileInput.value = ''
}
</script>

<template>
  <div class="register-page">
    <!-- Registration Form -->
    <div v-if="status === 'loggedOut'" class="register-container">
      <div class="register-card">
        <!-- Header -->
        <div class="register-header">
          <div class="header-icon">
            <i class="fas fa-user-plus"></i>
          </div>
          <h1 class="register-title">Tạo tài khoản</h1>
          <p class="register-subtitle">Gia nhập cộng đồng QuizMaster ngay hôm nay</p>
        </div>

        <!-- Form -->
        <form @submit.prevent="register" class="register-form">
          <!-- Username -->
          <div class="form-group">
            <label class="form-label">
              <i class="fas fa-user"></i>
              Tên đăng nhập
            </label>
            <input type="text" v-model="username" required maxlength="50" class="form-input"
              placeholder="Nhập tên đăng nhập" />
          </div>

          <!-- Email -->
          <div class="form-group">
            <label class="form-label">
              <i class="fas fa-envelope"></i>
              Email
            </label>
            <input type="email" v-model="email" required maxlength="100" class="form-input"
              placeholder="your@email.com" />
          </div>

          <!-- Password -->
          <div class="form-group">
            <label class="form-label">
              <i class="fas fa-lock"></i>
              Mật khẩu
              <span class="password-hint">(8-50 ký tự)</span>
            </label>
            <div class="password-wrapper">
              <input :type="showPassword ? 'text' : 'password'" v-model="password" required minlength="8" maxlength="50"
                class="form-input password-input" placeholder="Tối thiểu 8 ký tự" />
              <button type="button" @click="showPassword = !showPassword" class="password-toggle">
                <div class="eye-icon" :class="{ 'eye-hidden': showPassword }"></div>
              </button>
            </div>

            <div v-if="password" class="password-strength">
              <div class="strength-bar">
                <div class="strength-fill" :style="{
                  width: passwordStrength * 20 + '%',
                  backgroundColor: getStrengthColor(passwordStrength),
                }"></div>
              </div>
              <span class="strength-text" :style="{ color: getStrengthColor(passwordStrength) }">
                {{ getStrengthText(passwordStrength) }}
              </span>
            </div>

            <div v-if="password" class="password-requirements">
              <div class="requirement" :class="{ met: password.length >= 8 }">
                <i :class="password.length >= 8 ? 'fas fa-check' : 'fas fa-times'"></i>
                Tối thiểu 8 ký tự
              </div>
              <div class="requirement" :class="{ met: /[a-z]/.test(password) }">
                <i :class="/[a-z]/.test(password) ? 'fas fa-check' : 'fas fa-times'"></i>
                Chữ thường (a-z)
              </div>
              <div class="requirement" :class="{ met: /[A-Z]/.test(password) }">
                <i :class="/[A-Z]/.test(password) ? 'fas fa-check' : 'fas fa-times'"></i>
                Chữ hoa (A-Z)
              </div>
              <div class="requirement" :class="{ met: /[0-9]/.test(password) }">
                <i :class="/[0-9]/.test(password) ? 'fas fa-check' : 'fas fa-times'"></i>
                Số (0-9)
              </div>
              <div class="requirement" :class="{ met: /[^A-Za-z0-9]/.test(password) }">
                <i :class="/[^A-Za-z0-9]/.test(password) ? 'fas fa-check' : 'fas fa-times'"></i>
                Ký tự đặc biệt (!@#$...)
              </div>
            </div>
          </div>

          <!-- Confirm Password -->
          <div class="form-group">
            <label class="form-label">
              <i class="fas fa-lock"></i>
              Nhập lại mật khẩu
            </label>
            <div class="password-wrapper">
              <input :type="showConfirmPassword ? 'text' : 'password'" v-model="confirmPassword" required maxlength="50"
                class="form-input password-input" :class="{
                  'match-success': passwordMatch === true,
                  'match-error': passwordMatch === false,
                }" placeholder="Nhập lại mật khẩu" />
              <button type="button" @click="showConfirmPassword = !showConfirmPassword" class="password-toggle">
                <div class="eye-icon" :class="{ 'eye-hidden': showConfirmPassword }"></div>
              </button>
            </div>
            <div v-if="confirmPassword" class="password-match">
              <i :class="passwordMatch ? 'fas fa-check text-success' : 'fas fa-times text-danger'"></i>
              <span :class="passwordMatch ? 'text-success' : 'text-danger'">
                {{ passwordMatch ? 'Mật khẩu khớp' : 'Mật khẩu không khớp' }}
              </span>
            </div>
          </div>

          <!-- Full Name -->
          <div class="form-group">
            <label class="form-label">
              <i class="fas fa-id-card"></i>
              Họ và tên
            </label>
            <input type="text" v-model="fullName" maxlength="100" class="form-input" placeholder="Nhập họ và tên" />
          </div>


          <!-- Bio -->
          <div class="form-group">
            <label class="form-label">
              <i class="fas fa-pen"></i>
              Giới thiệu bản thân
            </label>
            <textarea v-model="bio" rows="3" maxlength="500" class="form-textarea"
              placeholder="Viết vài dòng về bản thân... (tối đa 500 ký tự)"></textarea>
            <div class="char-count">{{ bio?.length || 0 }}/500 ký tự</div>
          </div>

          <!-- Submit -->
          <button type="submit" class="register-btn" :disabled="isLoading || !passwordMatch || passwordStrength < 2">
            <div v-if="isLoading" class="btn-loading">
              <div class="spinner"></div>
              <span>Đang tạo tài khoản...</span>
            </div>
            <div v-else class="btn-content">
              <i class="fas fa-user-plus"></i>
              <span>Tạo tài khoản</span>
            </div>
          </button>

          <div v-if="passwordStrength < 2" class="validation-warning">
            <i class="fas fa-exclamation-triangle"></i>
            Mật khẩu cần đạt mức "Trung bình" trở lên để tạo tài khoản
          </div>

          <div class="form-footer">
            <p>
              Đã có tài khoản?
              <router-link to="/login" class="login-link"> Đăng nhập ngay </router-link>
            </p>
          </div>
        </form>
      </div>
    </div>

    <!-- Success -->
    <div v-else-if="status === 'SUCCESS'" class="success-container">
      <div class="success-card">
        <div class="success-icon">
          <i class="fas fa-check-circle"></i>
        </div>
        <h2 class="success-title">Đăng ký thành công!</h2>
        <p class="success-message">{{ message }}</p>
        <div class="success-details">
          <div class="detail-item">
            <i class="fas fa-user"></i>
            <span>Tài khoản: {{ username }}</span>
          </div>
          <div class="detail-item">
            <i class="fas fa-envelope"></i>
            <span>Email: {{ email }}</span>
          </div>
        </div>
        <div class="redirect-info">
          <i class="fas fa-clock"></i>
          <span>Tự động chuyển hướng đến Dashboard sau 2 giây...</span>
        </div>
        <button @click="goToDashboard" class="home-btn">
          <i class="fas fa-tachometer-alt"></i>
          Vào Dashboard ngay
        </button>
      </div>
    </div>

    <!-- Errors -->
    <div v-else-if="status === 'EMAIL_EXISTS'" class="error-container">
      <div class="error-card">
        <div class="error-icon">
          <i class="fas fa-envelope-open-text"></i>
        </div>
        <h2 class="error-title">Email đã tồn tại</h2>
        <p class="error-message">{{ message }}</p>
        <button @click="goToDashboard" class="retry-btn">
          <i class="fas fa-redo"></i>
          Thử lại
        </button>
      </div>
    </div>

    <div v-else-if="status === 'USERNAME_EXISTS'" class="error-container">
      <div class="error-card">
        <div class="error-icon">
          <i class="fas fa-user-times"></i>
        </div>
        <h2 class="error-title">Tên đăng nhập đã tồn tại</h2>
        <p class="error-message">{{ message }}</p>
        <button @click="goToDashboard" class="retry-btn">
          <i class="fas fa-redo"></i>
          Thử lại
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* (giữ nguyên toàn bộ CSS hiện có ngoại trừ phần dành cho URL đã xoá) */

/* === FIXED: PASSWORD FEATURES === */
.password-wrapper {
  position: relative;
  width: 100%;
}

.password-input {
  width: 100%;
  padding: 16px 60px 16px 20px !important;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  color: white;
  font-size: 1.1rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.password-input:focus {
  outline: none;
  border-color: #00d4ff;
  background: rgba(255, 255, 255, 0.2);
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.3);
  transform: translateY(-2px);
}

.password-toggle {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 100;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.password-toggle:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
  color: white;
  transform: translateY(-50%) scale(1.05);
}

.password-toggle:active {
  transform: translateY(-50%) scale(0.95);
}

.password-toggle i {
  pointer-events: none;
}

.register-page {
  min-height: 100vh;
  background: var(--app-background);
  padding: 40px 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.register-container {
  width: 100%;
  max-width: 600px;
  animation: slideInUp 0.8s ease-out;
}

.register-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  border-radius: 30px;
  padding: 50px 40px;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.3);
  border: 2px solid rgba(255, 255, 255, 0.2);
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
}

.header-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(45deg, #00d4ff, #5f27cd);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  box-shadow: 0 15px 35px rgba(95, 39, 205, 0.4);
  animation: pulse 2s infinite;
}

.header-icon i {
  font-size: 2.5rem;
  color: white;
}

.register-title {
  font-size: 2.8rem;
  font-weight: 800;
  color: white;
  margin-bottom: 10px;
  text-shadow: 2px 2px 15px rgba(0, 0, 0, 0.3);
}

.register-subtitle {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 0;
  font-weight: 300;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 1.1rem;
  font-weight: 600;
  color: white;
  display: flex;
  align-items: center;
  gap: 10px;
  text-shadow: 1px 1px 5px rgba(0, 0, 0, 0.3);
}

.form-label i {
  width: 20px;
  color: #00d4ff;
}

.password-hint {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 400;
  margin-left: auto;
}

.form-input {
  padding: 16px 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  color: white;
  font-size: 1.1rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: #00d4ff;
  background: rgba(255, 255, 255, 0.2);
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.3);
  transform: translateY(-2px);
}

.form-input::placeholder {
  color: rgba(255, 255, 255, 0.6);
}

.form-textarea {
  padding: 16px 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  color: white;
  font-size: 1.1rem;
  font-weight: 500;
  transition: all 0.3s ease;
  resize: vertical;
  min-height: 100px;
}

.form-textarea:focus {
  outline: none;
  border-color: #00d4ff;
  background: rgba(255, 255, 255, 0.2);
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.3);
  transform: translateY(-2px);
}

.form-textarea::placeholder {
  color: rgba(255, 255, 255, 0.6);
}

.char-count {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.6);
  text-align: right;
  margin-top: 5px;
}

.password-strength {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
}

.strength-bar {
  flex: 1;
  height: 8px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s ease;
  border-radius: 4px;
}

.strength-text {
  font-size: 0.9rem;
  font-weight: 600;
  min-width: 80px;
}

.password-requirements {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-top: 10px;
  padding: 15px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 10px;
}

.requirement {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.6);
  transition: color 0.3s ease;
}

.requirement.met {
  color: #2ed573;
}

.requirement i {
  width: 12px;
  font-size: 0.8rem;
}

.password-match {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  font-size: 0.95rem;
  font-weight: 500;
}

.match-success {
  border-color: #2ed573 !important;
}

.match-error {
  border-color: #ff4757 !important;
}

.text-success {
  color: #2ed573;
}

.text-danger {
  color: #ff4757;
}

.avatar-upload-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.upload-buttons {
  display: flex;
  align-items: center;
  gap: 15px;
  justify-content: center;
}

.upload-btn {
  background: linear-gradient(45deg, #00d4ff, #5f27cd);
  color: white;
  padding: 12px 20px;
  border-radius: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 8px 20px rgba(95, 39, 205, 0.3);
}

.upload-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 25px rgba(95, 39, 205, 0.4);
}

.avatar-preview-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
}

.avatar-preview-container {
  position: relative;
  display: inline-block;
}

.avatar-preview-img {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.3);
}

.avatar-remove-btn {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 30px;
  height: 30px;
  background: #ff4757;
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 5px 15px rgba(255, 71, 87, 0.4);
}

.avatar-remove-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 8px 20px rgba(255, 71, 87, 0.6);
}

.avatar-preview-text {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
  margin: 0;
}

.register-btn {
  background: linear-gradient(45deg, #00d4ff, #5f27cd);
  border: none;
  border-radius: 20px;
  padding: 18px;
  color: white;
  font-size: 1.2rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 10px 30px rgba(95, 39, 205, 0.4);
  margin-top: 10px;
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 15px 40px rgba(95, 39, 205, 0.6);
}

.register-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-loading,
.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
}

.validation-warning {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 15px;
  background: rgba(255, 193, 7, 0.2);
  border: 1px solid rgba(255, 193, 7, 0.4);
  border-radius: 10px;
  color: #ffc107;
  font-size: 0.95rem;
  font-weight: 500;
}

.form-footer {
  text-align: center;
  margin-top: 20px;
}

.form-footer p {
  color: rgba(255, 255, 255, 0.8);
  font-size: 1rem;
  margin: 0;
}

.login-link {
  color: #00d4ff;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.login-link:hover {
  color: #5f27cd;
  text-decoration: underline;
}

/* Success / Error / Animations / Responsive — giữ nguyên như bản cũ */
@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(50px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {

  0%,
  100% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.05);
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

@keyframes successPulse {

  0%,
  100% {
    transform: scale(1);
    box-shadow: 0 15px 35px rgba(46, 213, 115, 0.4);
  }

  50% {
    transform: scale(1.1);
    box-shadow: 0 20px 45px rgba(46, 213, 115, 0.6);
  }
}

@keyframes blink {

  0%,
  50% {
    opacity: 1;
  }

  51%,
  100% {
    opacity: 0.3;
  }
}

@media (max-width: 768px) {
  .register-page {
    padding: 20px 15px;
  }

  .register-card {
    padding: 30px 25px;
    border-radius: 20px;
  }

  .register-title {
    font-size: 2.2rem;
  }

  .register-subtitle {
    font-size: 1rem;
  }

  .header-icon {
    width: 60px;
    height: 60px;
  }

  .header-icon i {
    font-size: 2rem;
  }

  .password-requirements {
    grid-template-columns: 1fr;
  }

  .upload-buttons {
    flex-direction: column;
    gap: 10px;
  }

  .password-input {
    padding: 16px 55px 16px 16px !important;
  }

  .password-toggle {
    right: 15px;
    width: 32px;
    height: 32px;
  }
}

@media (max-width: 480px) {
  .register-card {
    padding: 25px 20px;
  }

  .register-title {
    font-size: 1.8rem;
  }

  .form-input,
  .form-textarea {
    padding: 14px 16px;
    font-size: 1rem;
  }

  .password-input {
    padding: 14px 50px 14px 16px !important;
  }

  .password-toggle {
    right: 12px;
    width: 30px;
    height: 30px;
    font-size: 0.9rem;
  }

  .eye-icon {
    width: 18px;
    height: 18px;
  }

  .eye-icon:not(.eye-hidden)::before {
    width: 16px;
    height: 8px;
  }

  .eye-icon:not(.eye-hidden)::after {
    width: 5px;
    height: 5px;
  }

  .eye-icon.eye-hidden::before {
    width: 16px;
    height: 2px;
  }

  .eye-icon.eye-hidden::after {
    height: 20px;
  }
}

/* Eye icon */
.eye-icon {
  position: relative;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.eye-icon:not(.eye-hidden)::before {
  content: '';
  position: absolute;
  width: 18px;
  height: 10px;
  border: 2px solid currentColor;
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
  background: transparent;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.eye-icon:not(.eye-hidden)::after {
  content: '';
  position: absolute;
  width: 6px;
  height: 6px;
  background: currentColor;
  border-radius: 50%;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  transition: all 0.2s ease;
}

.eye-icon.eye-hidden::before {
  content: '';
  position: absolute;
  width: 18px;
  height: 3px;
  background: currentColor;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border-radius: 2px;
  opacity: 0.7;
}

.eye-icon.eye-hidden::after {
  content: '';
  position: absolute;
  width: 2px;
  height: 22px;
  background: currentColor;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) rotate(45deg);
  border-radius: 1px;
  opacity: 0.8;
}

/* === SUCCESS CONTAINER === */
.success-container {
  min-height: 100vh;
  background: var(--app-background);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  animation: fadeIn 0.6s ease-out;
}

.success-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 30px;
  padding: 50px 40px;
  text-align: center;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.3);
  max-width: 500px;
  width: 100%;
  animation: slideInUp 0.8s ease-out;
}

.success-icon {
  width: 100px;
  height: 100px;
  background: linear-gradient(45deg, #2ed573, #1e90ff);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 30px;
  box-shadow: 0 15px 35px rgba(46, 213, 115, 0.4);
  animation: successPulse 2s infinite;
}

.success-icon i {
  font-size: 3rem;
  color: white;
}

.success-title {
  font-size: 2.5rem;
  font-weight: 800;
  color: #2ed573;
  margin-bottom: 20px;
  text-shadow: 0 2px 10px rgba(46, 213, 115, 0.3);
}

.success-message {
  font-size: 1.2rem;
  color: #666;
  margin-bottom: 30px;
  line-height: 1.6;
}

.success-details {
  background: rgba(46, 213, 115, 0.1);
  border-radius: 20px;
  padding: 25px;
  margin-bottom: 30px;
  border: 1px solid rgba(46, 213, 115, 0.2);
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  font-size: 1.1rem;
  color: #333;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-item i {
  width: 20px;
  color: #2ed573;
}

.redirect-info {
  background: rgba(0, 212, 255, 0.1);
  border-radius: 15px;
  padding: 20px;
  margin-bottom: 30px;
  border: 1px solid rgba(0, 212, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  color: #00d4ff;
  font-weight: 600;
  font-size: 1.1rem;
  animation: blink 2s infinite;
}

.home-btn {
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 25px;
  padding: 18px 40px;
  font-size: 1.2rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 15px;
  margin: 0 auto;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.home-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 15px 40px rgba(102, 126, 234, 0.6);
}

.home-btn:active {
  transform: translateY(-1px);
}

/* === ERROR CONTAINERS === */
.error-container {
  min-height: 100vh;
  background: var(--app-background);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  animation: fadeIn 0.6s ease-out;
}

.error-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 30px;
  padding: 50px 40px;
  text-align: center;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.3);
  max-width: 500px;
  width: 100%;
  animation: slideInUp 0.8s ease-out;
}

.error-icon {
  width: 100px;
  height: 100px;
  background: linear-gradient(45deg, #ff4757, #ff6b9d);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 30px;
  box-shadow: 0 15px 35px rgba(255, 71, 87, 0.4);
}

.error-icon i {
  font-size: 3rem;
  color: white;
}

.error-title {
  font-size: 2.5rem;
  font-weight: 800;
  color: #ff4757;
  margin-bottom: 20px;
  text-shadow: 0 2px 10px rgba(255, 71, 87, 0.3);
}

.error-message {
  font-size: 1.2rem;
  color: #666;
  margin-bottom: 30px;
  line-height: 1.6;
}

.retry-btn {
  background: linear-gradient(45deg, #ff4757, #ff6b9d);
  color: white;
  border: none;
  border-radius: 25px;
  padding: 18px 40px;
  font-size: 1.2rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 15px;
  margin: 0 auto;
  box-shadow: 0 10px 30px rgba(255, 71, 87, 0.4);
}

.retry-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 15px 40px rgba(255, 71, 87, 0.6);
}

.retry-btn:active {
  transform: translateY(-1px);
}

/* === ANIMATIONS === */
@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes successPulse {

  0%,
  100% {
    transform: scale(1);
    box-shadow: 0 15px 35px rgba(46, 213, 115, 0.4);
  }

  50% {
    transform: scale(1.1);
    box-shadow: 0 20px 45px rgba(46, 213, 115, 0.6);
  }
}

@keyframes blink {

  0%,
  50% {
    opacity: 1;
  }

  51%,
  100% {
    opacity: 0.3;
  }
}
</style>
