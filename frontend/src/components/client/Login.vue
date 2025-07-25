<script setup>
import { RouterLink, useRouter } from 'vue-router'
import { useLogin } from './useLogin'
import { useQuizCRUD } from './useQuizCRUD'
import { watch, ref, computed } from 'vue'

const router = useRouter()
const {
    status,
    username,
    message,
    password,
    userId,
    getUserId,
    login,
    logout
} = useLogin()

const { toQuizCRUD } = useQuizCRUD()
const showPassword = ref(false)
const isLoading = computed(() => status.value === 'loggingIn')
const hasError = computed(() => message.value && status.value === 'loggedOut')
const isSuccess = computed(() => status.value === 'loggedIn')

function toQuizHistory() {
    if (!userId.value) {
        console.warn("Chưa có userId. Đang thử gọi getUserId() lại...")
        getUserId().then(id => {
            if (id) router.push({ name: 'quizHistory', params: { userId: id } })
            else alert("Không thể lấy thông tin người dùng.")
        })
    } else {
        router.push({ name: 'quizHistory', params: { userId: userId.value } })
    }
}

watch(status, (newStatus) => {
    if (newStatus === 'loggedIn') {
        getUserId().then(id => {
            if (id) router.push({ name: 'ClientDashboard', params: { userId: id } })
            else alert("Không thể lấy thông tin người dùng.")
        })
    }
})

function togglePassword() {
    showPassword.value = !showPassword.value
}

function handleSubmit() {
    if (!username.value.trim() || !password.value.trim()) {
        return
    }
    login()
}
</script>

<template>
    <div class="login-container">
        <!-- Background Elements -->
        <div class="background-elements">
            <div class="floating-element element-1"></div>
            <div class="floating-element element-2"></div>
            <div class="floating-element element-3"></div>
            <div class="floating-element element-4"></div>
        </div>

        <!-- Login Card -->
        <div class="login-card">
            <!-- Header Section -->
            <div class="login-header">
                <div class="logo-section">
                    <div class="logo-icon">
                        <i class="bi bi-shield-lock"></i>
                    </div>
                    <h1 class="app-title">QuizMaster</h1>
                </div>
                <div class="welcome-text">
                    <h2 class="login-title">Chào mừng trở lại!</h2>
                    <p class="login-subtitle">Đăng nhập để tiếp tục hành trình học tập của bạn</p>
                </div>
            </div>

            <!-- Login Form -->
            <form @submit.prevent="handleSubmit" class="login-form">
                <!-- Username Field -->
                <div class="form-group">
                    <label for="username" class="form-label">
                        <i class="bi bi-person"></i>
                        <span>Tên đăng nhập</span>
                    </label>
                    <div class="input-wrapper">
                        <input 
                            type="text" 
                            id="username" 
                            v-model="username" 
                            class="form-input"
                            :class="{ 'error': hasError, 'success': isSuccess }"
                            placeholder="Nhập tên đăng nhập của bạn"
                            required 
                            :disabled="isLoading"
                        />
                        <div class="input-icon">
                            <i class="bi bi-person-circle"></i>
                        </div>
                    </div>
                </div>

                <!-- Password Field -->
                <div class="form-group">
                    <label for="password" class="form-label">
                        <i class="bi bi-lock"></i>
                        <span>Mật khẩu</span>
                    </label>
                    <div class="input-wrapper">
                        <input 
                            :type="showPassword ? 'text' : 'password'" 
                            id="password" 
                            v-model="password" 
                            class="form-input"
                            :class="{ 'error': hasError, 'success': isSuccess }"
                            placeholder="Nhập mật khẩu của bạn"
                            required 
                            :disabled="isLoading"
                        />
                        <button 
                            type="button" 
                            class="password-toggle"
                            @click="togglePassword"
                            :disabled="isLoading"
                        >
                            <i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
                        </button>
                    </div>
                </div>

                <!-- Remember & Forgot -->
                <div class="form-options">
                    <label class="remember-checkbox">
                        <input type="checkbox" class="checkbox-input">
                        <span class="checkbox-custom"></span>
                        <span class="checkbox-label">Ghi nhớ đăng nhập</span>
                    </label>
                    <RouterLink to="/forgot-password" class="forgot-link">
                        Quên mật khẩu?
                    </RouterLink>
                </div>

                <!-- Error/Success Message -->
                <div class="message-container" v-if="message">
                    <div class="message" :class="{ 'error': hasError, 'success': isSuccess }">
                        <i :class="hasError ? 'bi bi-exclamation-triangle' : 'bi bi-check-circle'"></i>
                        <span>{{ message }}</span>
                    </div>
                </div>

                <!-- Submit Button -->
                <button 
                    type="submit" 
                    class="login-btn"
                    :disabled="isLoading || !username.trim() || !password.trim()"
                    :class="{ 'loading': isLoading }"
                >
                    <span v-if="!isLoading" class="btn-content">
                        <i class="bi bi-box-arrow-in-right"></i>
                        <span>Đăng nhập</span>
                    </span>
                    <span v-else class="btn-loading">
                        <div class="loading-spinner"></div>
                        <span>Đang xử lý...</span>
                    </span>
                </button>
            </form>

            <!-- Divider -->
            <div class="divider">
                <span class="divider-text">Hoặc đăng nhập với</span>
            </div>

            <!-- Social Login (Placeholder) -->
            <div class="social-login">
                <button class="social-btn google" disabled>
                    <i class="bi bi-google"></i>
                    <span>Google</span>
                </button>
                <button class="social-btn facebook" disabled>
                    <i class="bi bi-facebook"></i>
                    <span>Facebook</span>
                </button>
            </div>

            <!-- Register Link -->
            <div class="register-section">
                <p class="register-text">
                    Chưa có tài khoản? 
                    <RouterLink to="/register" class="register-link">
                        Đăng ký ngay
                    </RouterLink>
                </p>
            </div>
        </div>

        <!-- Bottom Links -->
        <div class="bottom-links">
            <RouterLink to="/" class="back-home">
                <i class="bi bi-arrow-left"></i>
                <span>Về trang chủ</span>
            </RouterLink>
        </div>
    </div>
</template>

<style scoped>
/* === LOGIN CONTAINER === */
.login-container {
    min-height: 100vh;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    position: relative;
    overflow: hidden;
}

/* === BACKGROUND ELEMENTS === */
.background-elements {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;
}

.floating-element {
    position: absolute;
    border-radius: 50%;
    background: linear-gradient(45deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
    backdrop-filter: blur(10px);
    border: 2px solid rgba(255, 255, 255, 0.1);
    animation-duration: 8s;
    animation-iteration-count: infinite;
    animation-timing-function: ease-in-out;
}

.element-1 {
    width: 120px;
    height: 120px;
    top: 10%;
    left: 10%;
    animation-name: float1;
}

.element-2 {
    width: 80px;
    height: 80px;
    top: 20%;
    right: 15%;
    animation-name: float2;
    animation-delay: -2s;
}

.element-3 {
    width: 100px;
    height: 100px;
    bottom: 20%;
    left: 15%;
    animation-name: float3;
    animation-delay: -4s;
}

.element-4 {
    width: 60px;
    height: 60px;
    bottom: 30%;
    right: 20%;
    animation-name: float4;
    animation-delay: -6s;
}

@keyframes float1 {
    0%, 100% { transform: translate(0, 0) rotate(0deg); }
    25% { transform: translate(30px, -30px) rotate(90deg); }
    50% { transform: translate(-20px, -40px) rotate(180deg); }
    75% { transform: translate(-40px, 20px) rotate(270deg); }
}

@keyframes float2 {
    0%, 100% { transform: translate(0, 0) rotate(0deg); }
    33% { transform: translate(-25px, 30px) rotate(120deg); }
    66% { transform: translate(40px, -10px) rotate(240deg); }
}

@keyframes float3 {
    0%, 100% { transform: translate(0, 0) rotate(0deg); }
    50% { transform: translate(20px, -50px) rotate(180deg); }
}

@keyframes float4 {
    0%, 100% { transform: translate(0, 0) rotate(0deg); }
    25% { transform: translate(-30px, -20px) rotate(90deg); }
    75% { transform: translate(25px, 35px) rotate(270deg); }
}

/* === LOGIN CARD === */
.login-card {
    background: rgba(0, 0, 0, 0.4);
    backdrop-filter: blur(25px);
    border: 3px solid rgba(255, 255, 255, 0.9);
    border-radius: 30px;
    padding: 40px;
    width: 100%;
    max-width: 480px;
    box-shadow: 0 25px 60px rgba(0, 0, 0, 0.3);
    position: relative;
    animation: slideInUp 0.8s ease-out;
}

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

/* === HEADER SECTION === */
.login-header {
    text-align: center;
    margin-bottom: 40px;
}

.logo-section {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 15px;
    margin-bottom: 25px;
}

.logo-icon {
    width: 60px;
    height: 60px;
    background: linear-gradient(45deg, #00d4ff, #00b8d4);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 1.8rem;
    box-shadow: 0 8px 25px rgba(0, 212, 255, 0.4);
    border: 3px solid rgba(255, 255, 255, 0.9);
}

.app-title {
    font-size: 2.2rem;
    font-weight: 800;
    color: white;
    text-shadow: 3px 3px 6px rgba(0, 0, 0, 0.8);
    margin: 0;
    background: linear-gradient(45deg, #ffd700, #ffed4e);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.welcome-text {
    margin-bottom: 10px;
}

.login-title {
    font-size: 1.8rem;
    font-weight: 700;
    color: white;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.7);
    margin-bottom: 8px;
}

.login-subtitle {
    color: rgba(255, 255, 255, 0.9);
    font-size: 1rem;
    text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
    margin: 0;
}

/* === FORM STYLES === */
.login-form {
    margin-bottom: 30px;
}

.form-group {
    margin-bottom: 25px;
}

.form-label {
    display: flex;
    align-items: center;
    gap: 8px;
    color: white;
    font-weight: 600;
    font-size: 1rem;
    margin-bottom: 10px;
    text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

.form-label i {
    color: #00d4ff;
    font-size: 1.1rem;
}

.input-wrapper {
    position: relative;
}

.form-input {
    width: 100%;
    padding: 16px 20px;
    padding-right: 50px;
    background: rgba(0, 0, 0, 0.6);
    backdrop-filter: blur(10px);
    border: 3px solid rgba(255, 255, 255, 0.3);
    border-radius: 15px;
    color: white;
    font-size: 1rem;
    font-weight: 500;
    transition: all 0.3s ease;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.form-input::placeholder {
    color: rgba(255, 255, 255, 0.6);
}

.form-input:focus {
    outline: none;
    border-color: #00d4ff;
    background: rgba(0, 0, 0, 0.7);
    box-shadow: 0 0 0 4px rgba(0, 212, 255, 0.2);
}

.form-input.error {
    border-color: #ff4757;
    box-shadow: 0 0 0 4px rgba(255, 71, 87, 0.2);
}

.form-input.success {
    border-color: #4ecdc4;
    box-shadow: 0 0 0 4px rgba(78, 205, 196, 0.2);
}

.form-input:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.input-icon {
    position: absolute;
    right: 15px;
    top: 50%;
    transform: translateY(-50%);
    color: rgba(255, 255, 255, 0.5);
    font-size: 1.2rem;
    pointer-events: none;
}

.password-toggle {
    position: absolute;
    right: 15px;
    top: 50%;
    transform: translateY(-50%);
    background: none;
    border: none;
    color: rgba(255, 255, 255, 0.7);
    font-size: 1.2rem;
    cursor: pointer;
    padding: 5px;
    border-radius: 5px;
    transition: all 0.3s ease;
}

.password-toggle:hover:not(:disabled) {
    color: #00d4ff;
    background: rgba(0, 212, 255, 0.1);
}

.password-toggle:disabled {
    opacity: 0.4;
    cursor: not-allowed;
}

/* === FORM OPTIONS === */
.form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 25px;
}

.remember-checkbox {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    color: rgba(255, 255, 255, 0.9);
    font-size: 0.9rem;
    font-weight: 500;
}

.checkbox-input {
    display: none;
}

.checkbox-custom {
    width: 18px;
    height: 18px;
    border: 2px solid rgba(255, 255, 255, 0.5);
    border-radius: 4px;
    background: rgba(0, 0, 0, 0.3);
    position: relative;
    transition: all 0.3s ease;
}

.checkbox-input:checked + .checkbox-custom {
    background: linear-gradient(45deg, #00d4ff, #00b8d4);
    border-color: #00d4ff;
}

.checkbox-input:checked + .checkbox-custom::after {
    content: '✓';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: white;
    font-size: 12px;
    font-weight: bold;
}

.forgot-link {
    color: #00d4ff;
    text-decoration: none;
    font-size: 0.9rem;
    font-weight: 600;
    transition: all 0.3s ease;
}

.forgot-link:hover {
    color: #ffd700;
    text-shadow: 0 0 8px rgba(255, 215, 0, 0.5);
}

/* === MESSAGE === */
.message-container {
    margin-bottom: 20px;
}

.message {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 16px;
    border-radius: 12px;
    font-weight: 600;
    font-size: 0.9rem;
    backdrop-filter: blur(10px);
    border: 2px solid;
    animation: messageSlide 0.5s ease-out;
}

@keyframes messageSlide {
    from {
        opacity: 0;
        transform: translateY(-10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.message.error {
    background: rgba(255, 71, 87, 0.2);
    border-color: #ff4757;
    color: #ff4757;
}

.message.success {
    background: rgba(78, 205, 196, 0.2);
    border-color: #4ecdc4;
    color: #4ecdc4;
}

/* === LOGIN BUTTON === */
.login-btn {
    width: 100%;
    padding: 18px;
    background: linear-gradient(45deg, #00d4ff, #00b8d4);
    color: white;
    border: none;
    border-radius: 15px;
    font-size: 1.1rem;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 8px 25px rgba(0, 212, 255, 0.4);
    border: 3px solid rgba(255, 255, 255, 0.3);
    position: relative;
    overflow: hidden;
}

.login-btn:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 12px 35px rgba(0, 212, 255, 0.5);
    background: linear-gradient(45deg, #00b8d4, #0288d1);
}

.login-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.btn-content, .btn-loading {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
}

.loading-spinner {
    width: 20px;
    height: 20px;
    border: 2px solid transparent;
    border-top: 2px solid white;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

/* === DIVIDER === */
.divider {
    position: relative;
    text-align: center;
    margin: 30px 0;
}

.divider::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
}

.divider-text {
    background: rgba(0, 0, 0, 0.4);
    color: rgba(255, 255, 255, 0.8);
    padding: 0 20px;
    font-size: 0.9rem;
    font-weight: 500;
    position: relative;
    z-index: 1;
}

/* === SOCIAL LOGIN === */
.social-login {
    display: flex;
    gap: 15px;
    margin-bottom: 25px;
}

.social-btn {
    flex: 1;
    padding: 12px;
    border: none;
    border-radius: 12px;
    font-weight: 600;
    font-size: 0.9rem;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    opacity: 0.5;
    pointer-events: none;
}

.social-btn.google {
    background: rgba(255, 255, 255, 0.1);
    color: white;
    border: 2px solid rgba(255, 255, 255, 0.3);
}

.social-btn.facebook {
    background: rgba(66, 103, 178, 0.2);
    color: #4267B2;
    border: 2px solid rgba(66, 103, 178, 0.4);
}

/* === REGISTER SECTION === */
.register-section {
    text-align: center;
}

.register-text {
    color: rgba(255, 255, 255, 0.8);
    font-size: 0.95rem;
    margin: 0;
}

.register-link {
    color: #00d4ff;
    text-decoration: none;
    font-weight: 600;
    transition: all 0.3s ease;
}

.register-link:hover {
    color: #ffd700;
    text-shadow: 0 0 8px rgba(255, 215, 0, 0.5);
}

/* === BOTTOM LINKS === */
.bottom-links {
    position: absolute;
    bottom: 20px;
    left: 50%;
    transform: translateX(-50%);
}

.back-home {
    display: flex;
    align-items: center;
    gap: 8px;
    color: rgba(255, 255, 255, 0.7);
    text-decoration: none;
    font-size: 0.9rem;
    font-weight: 500;
    transition: all 0.3s ease;
    padding: 10px 20px;
    border-radius: 20px;
    background: rgba(0, 0, 0, 0.2);
    backdrop-filter: blur(10px);
    border: 2px solid rgba(255, 255, 255, 0.2);
}

.back-home:hover {
    color: white;
    background: rgba(0, 0, 0, 0.4);
    border-color: rgba(255, 255, 255, 0.4);
    transform: translateX(-50%) translateY(-2px);
}

/* === RESPONSIVE DESIGN === */
@media (max-width: 768px) {
    .login-container {
        padding: 15px;
    }
    
    .login-card {
        padding: 30px 25px;
        max-width: 100%;
    }
    
    .app-title {
        font-size: 1.8rem;
    }
    
    .login-title {
        font-size: 1.5rem;
    }
    
    .form-options {
        flex-direction: column;
        gap: 15px;
        align-items: stretch;
    }
    
    .social-login {
        flex-direction: column;
    }
    
    .bottom-links {
        position: static;
        margin-top: 30px;
        transform: none;
        text-align: center;
    }
    
    .back-home {
        display: inline-flex;
    }
}

@media (max-width: 480px) {
    .login-card {
        padding: 25px 20px;
    }
    
    .logo-section {
        flex-direction: column;
        gap: 10px;
    }
    
    .app-title {
        font-size: 1.6rem;
    }
    
    .form-input {
        padding: 14px 18px;
        padding-right: 45px;
    }
    
    .login-btn {
        padding: 16px;
    }
}
</style>