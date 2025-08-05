<template>
  <div class="unauthorized-container">
    <div class="unauthorized-content">
      <div class="error-icon">
        <i class="bi bi-shield-exclamation"></i>
      </div>
      
      <h1 class="error-title">Truy cập bị từ chối</h1>
      
      <p class="error-message">
        Bạn không có quyền truy cập vào trang này. 
        <span v-if="requiredRole === 'ADMIN'">
          Chỉ Admin mới có thể truy cập tính năng này.
        </span>
        <span v-else>
          Vui lòng đăng nhập để tiếp tục.
        </span>
      </p>
      
      <div class="action-buttons">
        <button @click="goBack" class="btn btn-outline-secondary me-3">
          <i class="bi bi-arrow-left me-2"></i>
          Quay lại
        </button>
        
        <button @click="goHome" class="btn btn-primary me-3">
          <i class="bi bi-house me-2"></i>
          Về trang chủ
        </button>
        
        <button @click="goLogin" class="btn btn-success">
          <i class="bi bi-box-arrow-in-right me-2"></i>
          Đăng nhập
        </button>
      </div>
      
      <div class="help-section">
        <h5>Bạn cần hỗ trợ?</h5>
        <p>
          Nếu bạn nghĩ đây là lỗi, vui lòng liên hệ với chúng tôi:
        </p>
        <div class="contact-info">
          <div class="contact-item">
            <i class="bi bi-envelope"></i>
            <span>support@quizmaster.com</span>
          </div>
          <div class="contact-item">
            <i class="bi bi-telephone"></i>
            <span>+84 123 456 789</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// Props
const props = defineProps({
  requiredRole: {
    type: String,
    default: 'USER'
  },
  message: {
    type: String,
    default: ''
  }
})

// Methods
const goBack = () => {
  router.go(-1)
}

const goHome = () => {
  router.push('/')
}

const goLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.unauthorized-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem;
}

.unauthorized-content {
  background: white;
  border-radius: 1rem;
  padding: 3rem;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  width: 100%;
}

.error-icon {
  font-size: 4rem;
  color: #dc3545;
  margin-bottom: 1.5rem;
}

.error-title {
  color: #343a40;
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.error-message {
  color: #6c757d;
  font-size: 1.1rem;
  line-height: 1.6;
  margin-bottom: 2rem;
}

.action-buttons {
  margin-bottom: 2rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  font-weight: 600;
  border-radius: 0.5rem;
  transition: all 0.3s ease;
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.help-section {
  border-top: 1px solid #e9ecef;
  padding-top: 2rem;
  margin-top: 2rem;
}

.help-section h5 {
  color: #343a40;
  margin-bottom: 1rem;
}

.help-section p {
  color: #6c757d;
  margin-bottom: 1.5rem;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: center;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #6c757d;
}

.contact-item i {
  color: #007bff;
}

/* Responsive */
@media (max-width: 768px) {
  .unauthorized-content {
    padding: 2rem;
  }
  
  .error-title {
    font-size: 1.5rem;
  }
  
  .action-buttons {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  .btn {
    width: 100%;
  }
}
</style> 