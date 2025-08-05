<template>
  <div v-if="isAuthorized" class="admin-layout">
    <!-- Header -->
    <HeaderAdmin />
    
    <!-- Main Content -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- Footer -->
    <FooterAdmin />
  </div>
  
  <!-- Unauthorized Access -->
  <div v-else class="unauthorized-container">
    <div class="unauthorized-content">
      <div class="unauthorized-icon">
        <i class="bi bi-shield-exclamation"></i>
      </div>
      
      <h1 class="unauthorized-title">Truy cập bị từ chối</h1>
      
      <p class="unauthorized-message">
        Bạn không có quyền truy cập vào Admin Panel. 
        Chỉ Admin mới có thể truy cập tính năng này.
      </p>
      
      <div class="unauthorized-actions">
        <button @click="goBack" class="btn btn-outline-secondary">
          <i class="bi bi-arrow-left"></i>
          Quay lại
        </button>
        
        <button @click="goHome" class="btn btn-primary">
          <i class="bi bi-house"></i>
          Về trang chủ
        </button>
        
        <button @click="goLogin" class="btn btn-success">
          <i class="bi bi-box-arrow-in-right"></i>
          Đăng nhập Admin
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import FooterAdmin from '@/components/admin/FooterAdmin.vue'
import HeaderAdmin from '@/components/admin/HeaderAdmin.vue'

const router = useRouter()
const adminUser = ref(null)

// ✅ CHECK AUTHORIZATION
const isAuthorized = computed(() => {
  return adminUser.value !== null
})

// ✅ LOAD ADMIN USER
const loadAdminUser = () => {
  try {
    // ✅ Kiểm tra token trước
    const token = localStorage.getItem('token')
    if (!token) {
      console.log('❌ No token found, clearing admin user')
      adminUser.value = null
      return
    }
    
    // Kiểm tra admin_user trước (cho admin login)
    const adminUserStr = localStorage.getItem('admin_user')
    if (adminUserStr) {
      adminUser.value = JSON.parse(adminUserStr)
      console.log('✅ Admin user loaded from admin_user:', adminUser.value)
      return
    }
    
    // Kiểm tra user role từ user data (cho user login với role admin)
    const userInfo = localStorage.getItem('user')
    if (userInfo) {
      const userData = JSON.parse(userInfo)
      if (userData.role === 'admin' || userData.role === 'ADMIN') {
        adminUser.value = userData
        console.log('✅ Admin user loaded from user data:', adminUser.value)
        return
      }
    }
    
    console.log('❌ No admin user found')
    adminUser.value = null
  } catch (error) {
    console.error('Error loading admin user:', error)
    adminUser.value = null
  }
}

// ✅ NAVIGATION FUNCTIONS
const goBack = () => {
  router.go(-1)
}

const goHome = () => {
  router.push('/')
}

const goLogin = () => {
  router.push('/login')
}

// ✅ MOUNTED
onMounted(() => {
  loadAdminUser()
  
  // Watch for changes in localStorage
  window.addEventListener('storage', (e) => {
    if (e.key === 'admin_user' || e.key === 'user' || e.key === 'token') {
      // ✅ Chỉ reload nếu có token
      const token = localStorage.getItem('token')
      if (token) {
        loadAdminUser()
      } else {
        // ✅ Nếu không có token, clear admin user
        adminUser.value = null
      }
    }
  })
})
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.main-content {
  flex: 1;
  padding: 0;
  margin-top: 0;
}

/* Unauthorized Access Styles */
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
  border-radius: 20px;
  padding: 3rem;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
}

.unauthorized-icon {
  font-size: 4rem;
  color: #dc3545;
  margin-bottom: 1.5rem;
}

.unauthorized-title {
  font-size: 2rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 1rem;
}

.unauthorized-message {
  font-size: 1.1rem;
  color: #666;
  margin-bottom: 2rem;
  line-height: 1.6;
}

.unauthorized-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none;
}

.btn-outline-secondary {
  background: transparent;
  color: #6c757d;
  border: 2px solid #6c757d;
}

.btn-outline-secondary:hover {
  background: #6c757d;
  color: white;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover {
  background: #0056b3;
}

.btn-success {
  background: #28a745;
  color: white;
}

.btn-success:hover {
  background: #1e7e34;
}

/* Responsive Design */
@media (max-width: 768px) {
  .unauthorized-content {
    padding: 2rem;
  }
  
  .unauthorized-title {
    font-size: 1.5rem;
  }
  
  .unauthorized-message {
    font-size: 1rem;
  }
  
  .unauthorized-actions {
    flex-direction: column;
  }
  
  .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
