<template>
    <div class="modal fade" id="reportModal" tabindex="-1" aria-labelledby="reportModalLabel" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header bg-warning text-dark">
            <h5 class="modal-title" id="reportModalLabel">
              <i class="bi bi-exclamation-triangle me-2"></i>Báo cáo Quiz
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          
          <div class="modal-body">
            <div v-if="quizState.currentQuizData" class="mb-3">
              <h6 class="text-muted">Quiz được báo cáo:</h6>
              <div class="card bg-light">
                <div class="card-body py-2">
                    <strong>{{ quizState.currentQuizData.title }}</strong>
                  <br>
                  <small class="text-muted">Tác giả: {{ quizState.currentQuizData.author }}</small>
                </div>
              </div>
            </div>
  
            <form @submit.prevent="submitReport">
              <div class="mb-3">
                <label for="reportReason" class="form-label">
                  <strong>Lý do báo cáo <span class="text-danger">*</span></strong>
                </label>
                <textarea 
                  id="reportReason"
                  v-model="reportReason" 
                  class="form-control" 
                  rows="4" 
                  placeholder="Vui lòng mô tả lý do báo cáo quiz này (ví dụ: nội dung không phù hợp, câu hỏi sai, vi phạm bản quyền...)"
                  required
                  maxlength="500"
                ></textarea>
                <div class="form-text">
                  {{ reportReason.length }}/500 ký tự
                </div>
              </div>
  
              <div class="mb-3">
                <div class="form-check">
                  <input 
                    class="form-check-input" 
                    type="checkbox" 
                    id="confirmReport" 
                    v-model="confirmReport"
                    required
                  >
                  <label class="form-check-label" for="confirmReport">
                    Tôi xác nhận thông tin báo cáo là chính xác và không có ý định spam
                  </label>
                </div>
              </div>
  
              <div v-if="error" class="alert alert-danger">
                <i class="bi bi-exclamation-circle me-2"></i>{{ error }}
              </div>
  
              <div v-if="success" class="alert alert-success">
                <i class="bi bi-check-circle me-2"></i>{{ success }}
              </div>
            </form>
          </div>
          
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
              <i class="bi bi-x-circle me-1"></i>Hủy
            </button>
            <button 
              type="button" 
              class="btn btn-warning" 
              @click="submitReport"
              :disabled="loading || !reportReason.trim() || !confirmReport"
            >
              <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status"></span>
              <i v-else class="bi bi-flag me-1"></i>
              {{ loading ? 'Đang gửi...' : 'Gửi báo cáo' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <<script setup>
  import { ref, reactive } from 'vue'  // ✅ THÊM reactive
  import axios from 'axios'
  import { useLogin } from './useLogin'
  
  const { userId, getUserId } = useLogin()
  const emit = defineEmits(['reported'])
  
  // ✅ SỬA: Dùng reactive object thay vì biến thường
  const quizState = reactive({
    currentQuizData: null
  })
  
  const reportReason = ref('')
  const confirmReport = ref(false)
  const loading = ref(false)
  const error = ref('')
  const success = ref('')
  
  async function openModal(quiz) {
  // Lưu vào cả 2 nơi: reactive và localStorage
  quizState.currentQuizData = quiz
  localStorage.setItem('reportQuizData', JSON.stringify(quiz))
  
  reportReason.value = ''
  confirmReport.value = false
  error.value = ''
  success.value = ''
  
  if (!userId.value) {
    try {
      await getUserId()
    } catch (error) {
      // Tiếp tục cho phép báo cáo dù không lấy được userId
    }
  }
}
  
async function submitReport() {
  // Thử lấy từ quizState trước
  let quizData = quizState.currentQuizData
  
  // Nếu null, lấy từ localStorage
  if (!quizData) {
    const stored = localStorage.getItem('reportQuizData')
    quizData = stored ? JSON.parse(stored) : null
  }
  
  // Validation
  if (!quizData) {
    error.value = 'Không có thông tin quiz'
    return
  }
  
  if (!reportReason.value.trim()) {
    error.value = 'Vui lòng nhập lý do báo cáo'
    return
  }
  
  if (!confirmReport.value) {
    error.value = 'Vui lòng xác nhận thông tin báo cáo'
    return
  }

  if (!userId.value) {
    error.value = 'Bạn cần đăng nhập để báo cáo quiz'
    return
  }
  
  loading.value = true
  error.value = ''
  success.value = ''

  try {
    const reportData = {
      userId: userId.value,
      quizId: quizData.quiz_id || quizData.id,
      reason: reportReason.value.trim()
    }

    const response = await axios.post('http://localhost:8080/api/reports', reportData)
    
    success.value = 'Báo cáo đã được gửi thành công! Cảm ơn bạn đã góp ý.'
    
    emit('reported', quizData)
    
    setTimeout(() => {
      const modal = document.getElementById('reportModal')
      
      // Kiểm tra bootstrap có tồn tại không
      if (typeof bootstrap !== 'undefined') {
        const modalInstance = bootstrap.Modal.getInstance(modal)
        if (modalInstance) {
          modalInstance.hide()
        }
      } else {
        // Fallback: ẩn modal bằng cách khác
        if (modal) {
          modal.style.display = 'none'
          modal.classList.remove('show')
          document.body.classList.remove('modal-open')
          
          // Xóa backdrop
          const backdrop = document.querySelector('.modal-backdrop')
          if (backdrop) {
            backdrop.remove()
          }
        }
      }
      
      // Xóa dữ liệu
      quizState.currentQuizData = null
      localStorage.removeItem('reportQuizData')
      reportReason.value = ''
      confirmReport.value = false
      success.value = ''
    }, 2000)

  } catch (err) {
    
    if (err.response?.status === 409) {
      error.value = 'Bạn đã báo cáo quiz này rồi!'
    } else if (err.response?.status === 404) {
      error.value = 'Quiz không tồn tại!'
    } else {
      error.value = err.response?.data?.message || 'Có lỗi xảy ra khi gửi báo cáo. Vui lòng thử lại!'
    }
  } finally {
    loading.value = false
  }
}
  
  defineExpose({
    openModal
  })
  </script>
  
  <style scoped>
  .modal-content {
    border-radius: 12px;
    border: none;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  }
  
  .modal-header {
    border-top-left-radius: 12px;
    border-top-right-radius: 12px;
    border-bottom: 1px solid #dee2e6;
  }
  
  .card {
    border: 1px solid #e9ecef;
    border-radius: 8px;
  }
  
  .form-control:focus {
    border-color: #ffc107;
    box-shadow: 0 0 0 0.2rem rgba(255, 193, 7, 0.25);
  }
  
  .btn-warning {
    background-color: #ffc107;
    border-color: #ffc107;
    color: #000;
    font-weight: 500;
  }
  
  .btn-warning:hover {
    background-color: #e0a800;
    border-color: #d39e00;
    color: #000;
  }
  
  .btn-warning:disabled {
    background-color: #ffc107;
    border-color: #ffc107;
    opacity: 0.65;
  }
  
  .spinner-border-sm {
    width: 1rem;
    height: 1rem;
  }
  
  .alert {
    border-radius: 8px;
    border: none;
  }
  
  .alert-danger {
    background-color: #f8d7da;
    color: #721c24;
  }
  
  .alert-success {
    background-color: #d1edff;
    color: #0c5460;
  }
  
  .form-check-input:checked {
    background-color: #ffc107;
    border-color: #ffc107;
  }
  
  .form-text {
    font-size: 0.875rem;
    color: #6c757d;
  }
  </style>