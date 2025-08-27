<template>
  <div class="modal fade show" id="resumeQuizModal" tabindex="-1" aria-labelledby="resumeQuizModalLabel"
    style="display: block; background-color: rgba(0,0,0,0.5);" @click.self="closeModal">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="resumeQuizModalLabel">
            <i class="bi bi-arrow-clockwise text-primary me-2"></i>
            Tiếp tục Quiz
          </h5>
          <button type="button" class="btn-close" @click="closeModal" aria-label="Close"></button>
        </div>

        <div class="modal-body">
          <div class="text-center mb-4">
            <div class="alert alert-info">
              <i class="bi bi-info-circle me-2"></i>
              <strong>Bạn chưa hoàn thành quiz này!</strong>
            </div>

            <div class="quiz-info p-3 bg-light rounded">
              <h6 class="mb-2">{{ quizTitle }}</h6>
              <div class="row text-muted small">
                <div class="col-6">
                  <i class="bi bi-question-circle me-1"></i>
                  Câu {{ currentQuestionIndex + 1 }}/{{ totalQuestions }}
                </div>
                <div class="col-6" v-if="hasTimeLimit">
                  <i class="bi bi-clock me-1"></i>
                  {{ formatTime(timeRemaining) }}
                </div>
              </div>
              <div class="mt-2 text-muted">
                <small>
                  <i class="bi bi-calendar me-1"></i>
                  Bắt đầu: {{ formatDateTime(startedAt) }}
                </small>
              </div>
            </div>
          </div>

          <div class="text-center">
            <p class="mb-4">Bạn muốn tiếp tục hay làm lại từ đầu?</p>

            <div class="d-grid gap-2">
              <button @click="resumeQuiz" class="btn btn-primary btn-lg" :disabled="loading">
                <i class="bi bi-play-circle me-2"></i>
                <span v-if="loading">Đang xử lý...</span>
                <span v-else>Tiếp tục</span>
              </button>

              <button @click="startNewAttempt" class="btn btn-outline-secondary" :disabled="loading">
                <i class="bi bi-arrow-repeat me-2"></i>
                <span v-if="loading">Đang xử lý...</span>
                <span v-else>Làm lại từ đầu</span>
              </button>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeModal">
            <i class="bi bi-x-circle me-2"></i>
            Hủy
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1050;
}

.modal.show {
  display: block !important;
}

.modal-backdrop {
  background-color: rgba(0, 0, 0, 0.5);
}

.modal-dialog {
  margin: 1.75rem auto;
  max-width: 500px;
}

.modal-content {
  border: none;
  border-radius: 0.5rem;
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
}

.modal-header {
  border-bottom: 1px solid #dee2e6;
  padding: 1rem 1.5rem;
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  border-top: 1px solid #dee2e6;
  padding: 1rem 1.5rem;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0.25rem;
}

.btn-close:hover {
  opacity: 0.75;
}
</style>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { quizResumeService } from '@/services/quizResumeService'
import { useToast } from '@/composables/useToast'

export default {
  name: 'ResumeQuizModal',
  props: {
    quizId: {
      type: Number,
      required: true
    },
    attemptData: {
      type: Object,
      required: true
    }
  },

  emits: ['resume', 'new-attempt', 'close'],

  setup(props, { emit }) {
    const router = useRouter()
    const { showToast } = useToast()

    const loading = ref(false)

    /**
     *  Đóng modal
     */
    const closeModal = () => {
      emit('close')
    }

    // Lấy dữ liệu từ props
    const {
      attemptId,
      quizTitle,
      currentQuestionIndex,
      timeRemaining,
      startedAt,
      hasTimeLimit,
      totalQuestions
    } = props.attemptData

    /**
     *  Tiếp tục quiz hiện tại
     */
    const resumeQuiz = async () => {
      try {
        loading.value = true

        // Gọi API để lấy thông tin chi tiết
        const response = await quizResumeService.resumeAttempt(attemptId)

        if (response.success) {
          // Emit event để parent component xử lý
          emit('resume', response.data)

          // Đóng modal
          closeModal()

          showToast('Đã khôi phục quiz thành công!', 'success')
        }

      } catch (error) {
        console.error('Lỗi khi resume quiz:', error)
        showToast('Có lỗi xảy ra khi khôi phục quiz', 'error')
      } finally {
        loading.value = false
      }
    }

    /**
     *  Tạo attempt mới
     */
    const startNewAttempt = async () => {
      try {
        loading.value = true

        // Gọi API để tạo attempt mới
        const response = await quizResumeService.createNewAttempt(props.quizId)

        if (response.success) {
          // Emit event để parent component xử lý
          emit('new-attempt', response.attemptId)

          // Đóng modal
          closeModal()

          showToast('Đã tạo attempt mới thành công!', 'success')
        }

      } catch (error) {
        console.error('Lỗi khi tạo attempt mới:', error)
        showToast('Có lỗi xảy ra khi tạo attempt mới', 'error')
      } finally {
        loading.value = false
      }
    }

    /**
     *  Format thời gian
     */
    const formatTime = (seconds) => {
      if (!seconds || seconds <= 0) return '00:00'

      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = seconds % 60
      return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
    }

    /**
     *  Format ngày giờ
     */
    const formatDateTime = (dateTime) => {
      if (!dateTime) return ''

      const date = new Date(dateTime)
      return date.toLocaleString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    return {
      loading,
      quizTitle,
      currentQuestionIndex,
      timeRemaining,
      startedAt,
      hasTimeLimit,
      totalQuestions,
      resumeQuiz,
      startNewAttempt,
      formatTime,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.quiz-info {
  border-left: 4px solid #0d6efd;
}

.modal-dialog {
  max-width: 500px;
}

.btn-lg {
  padding: 12px 24px;
  font-size: 1.1rem;
}

.alert {
  border: none;
  background-color: #e7f3ff;
  color: #0c63e4;
}

.bg-light {
  background-color: #f8f9fa !important;
}
</style>
