<script setup>
import { ref, onMounted, computed } from 'vue'
import { useQuizCRUD } from './useQuizCRUD'
import { useLogin } from './useLogin'
import { useRouter } from 'vue-router'
import api from '@/utils/axios'
import * as XLSX from 'xlsx'
// Hooks
const { toQuizCRUD, categories } = useQuizCRUD()
const { getUserId, username } = useLogin()
const router = useRouter()

// State - Existing
const title = ref('')
const selectedCategoryId = ref('')
const userId = getUserId()
const isPublic = ref(true)
const userName = ref('')
const quizzes = ref([])
const message = ref('')
const selectedImage = ref(null)
const previewUrl = ref(null)
const isLoading = ref(false)
const isCreating = ref(false)
const loadingQuizzes = ref(true)
const description = ref('')
const messageType = ref('success')

// State - Import Excel
const activeTab = ref('create')
const importQuizTitle = ref('')
const importQuizDescription = ref('')
const importCategoryId = ref('')
const selectedExcelFile = ref(null)
const isDragOver = ref(false)
const isImporting = ref(false)
const importResult = ref(null)
const importSelectedImage = ref(null)
const importPreviewUrl = ref(null)
const importIsPublic = ref(true)

//  THÊM STATE CHO QUIZ CODE
const showCodeModal = ref(false)
const quizCode = ref('')
const quizInfo = ref(null)

//  HELPER: LẤY SỐ CÂU HỎI CỦA QUIZ (support nhiều field khác nhau)
const getQuestionCount = (q) => {
  return (
    q?.questionCount ??
    q?.totalQuestions ??
    q?.numQuestions ??
    (Array.isArray(q?.questions) ? q.questions.length : 0) ??
    0
  )
}

//điều hướng về edit quiz
const goToEditAfterCreate = () => {
  const qid = quizInfo.value?.quizId
  const uid = userId.value
  if (!qid || !uid) {
    // fallback: chỉ đóng modal nếu không có đủ dữ liệu
    showCodeModal.value = false
    return
  }
  router.push({
    name: 'EditQuiz',
    params: {
      userId: String(uid),
      quizId: String(qid),
    },
  })
}

function handleImageUpload(event) {
  const file = event.target.files[0]
  if (file) {
    selectedImage.value = file
    previewUrl.value = URL.createObjectURL(file)
  }
}

const getQuizImageUrl = (quizId) => {
  return `/api/image/quiz/${quizId}`
}

// Lifecycle
onMounted(async () => {
  isLoading.value = true
  try {
    userId.value = await getUserId()

    //  ĐẢM BẢO USERNAME ĐƯỢC KHỞI TẠO ĐÚNG CÁCH
    if (!username.value) {
      const savedUsername = localStorage.getItem('username')
      if (savedUsername) {
        username.value = savedUsername
      } else {
        // Thử lấy từ user object
        try {
          const userStr = localStorage.getItem('user')
          if (userStr) {
            const user = JSON.parse(userStr)
            if (user.username) {
              username.value = user.username
            }
          }
        } catch (e) {
          console.error('Error parsing user object:', e)
        }
      }
    }

    await Promise.all([fetchCategories(), fetchQuizzes()])
  } finally {
    isLoading.value = false
    loadingQuizzes.value = false
  }
})

function clearImage() {
  selectedImage.value = null
  previewUrl.value = null
}

// Fetch categories
async function fetchCategories() {
  try {
    const res = await api.get('/categories')
    categories.value = res.data
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

// Fetch quizzes
async function fetchQuizzes() {
  try {
    //  SỬA: CHỈ LẤY QUIZ CỦA USER HIỆN TẠI
    const response = await api.get(`/quiz/user/${userId.value}/paginated`, {
      params: { page: 0, size: 50 }, // Lấy nhiều quiz hơn
    })
    console.log(' Fetch quizzes response:', response.data)
    quizzes.value = response.data.quizzes || response.data
    console.log(' Quizzes loaded:', quizzes.value.length)

    //  DEBUG: Kiểm tra từng quiz
    quizzes.value.forEach((quiz, index) => {
      console.log(` Quiz ${index + 1}:`, {
        id: quiz.id,
        title: quiz.title,
        isPublic: quiz.isPublic,
        deleted: quiz.deleted,
        deletedAt: quiz.deletedAt,
        questionCount: getQuestionCount(quiz),
      })
    })
  } catch (error) {
    console.error('Error fetching quizzes:', error)
  }
}

// Tạo quiz
async function createQuiz() {
  if (isCreating.value) return

  isCreating.value = true
  try {
    const formData = new FormData()

    formData.append('title', title.value.trim())
    formData.append('description', description.value.trim())
    formData.append('isPublic', isPublic.value)
    formData.append('categoryId', selectedCategoryId.value)
    formData.append('userId', userId.value)

    if (selectedImage.value) {
      formData.append('image', selectedImage.value)
    }

    //  THỰC HIỆN POST VÀ LẤY RESPONSE
    const response = await api.post('/quiz/create-quiz-with-image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    //  LẤY quizId từ response
    const quizId = response.data.quiz?.id || response.data.id
    const quizCode = response.data.quiz?.quizCode || response.data.quizCode

    message.value = 'Tạo quiz thành công!'
    messageType.value = 'success'

    //  HIỂN THỊ QUIZ CODE VÀ LƯU QUIZ INFO
    if (quizCode) {
      showQuizCode(quizCode, quizId)
    }

    resetForm()
    await fetchQuizzes()
  } catch (error) {
    console.error('Lỗi khi tạo quiz:', error)
    message.value = 'Tạo quiz thất bại!'
    messageType.value = 'error'
    setTimeout(() => {
      message.value = ''
    }, 3000)
  } finally {
    isCreating.value = false
  }
}

function editQuiz(quizId) {
  const quiz = quizzes.value.find((q) => q.id === quizId)
  if (!quiz) return

  //  Nhắc nếu chưa có question (nhưng vẫn cho vào trang Sửa)
  if (getQuestionCount(quiz) === 0) {
    message.value = 'Quiz này chưa có question — hãy thêm question sau khi vào trang Sửa.'
    messageType.value = 'error'
    setTimeout(() => (message.value = ''), 2500)
  }

  const uid = quiz.user?.id ?? userId.value
  router.push({
    name: 'EditQuiz',
    params: {
      userId: uid,
      quizId,
    },
  })
}

async function deleteQuiz(quizId) {
  if (confirm('Bạn có chắc chắn muốn xóa quiz này?')) {
    try {
      const response = await api.delete(`/quiz/${quizId}`)

      if (response.status === 200 && response.data && response.data.success) {
        message.value = response.data.message || 'Xóa quiz thành công!'
        messageType.value = 'success'
      } else {
        message.value = 'Xóa quiz thất bại: Quiz không tồn tại!'
        messageType.value = 'error'
      }

      await fetchQuizzes()

      setTimeout(() => {
        message.value = ''
      }, 3000)
    } catch (error) {
      if (error.response && error.response.status === 403) {
        message.value = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!'
      } else if (error.response && error.response.status === 404) {
        message.value = 'Quiz không tồn tại!'
      } else {
        console.error('Lỗi khi xóa quiz:', error)
        message.value = 'Xóa quiz thất bại!'
      }
      messageType.value = 'error'
      setTimeout(() => {
        message.value = ''
      }, 3000)
    }
  }
}

async function playQuiz(quizId) {
  try {
    const { quizAttemptService } = await import('@/services/quizAttemptService')
    const resp = await quizAttemptService.startAttempt(quizId)
    router.push({ name: 'PlayAttempt', params: { attemptId: resp.attemptId } })
  } catch (e) {
    console.error('Không thể bắt đầu attempt:', e)
  }
}

//  COMPUTED CHO IMPORT EXCEL
const canImport = computed(() => {
  return importQuizTitle.value.trim() && importCategoryId.value && selectedExcelFile.value
})

//  METHODS CHO IMPORT EXCEL
const downloadTemplate = () => {
  // Tạo file Excel template thực sự với thư viện xlsx
  const sampleData = [
    {
      'Câu hỏi': 'Thủ đô của Việt Nam là gì?',
      'Đáp án A': 'Hà Nội',
      'Đáp án B': 'TP.HCM',
      'Đáp án C': 'Đà Nẵng',
      'Đáp án D': 'Huế',
      'Đáp án đúng': 'A',
      'Thời gian (giây)': 30,
    },
    {
      'Câu hỏi': '1 + 1 = ?',
      'Đáp án A': '1',
      'Đáp án B': '2',
      'Đáp án C': '3',
      'Đáp án D': '4',
      'Đáp án đúng': 'B',
      'Thời gian (giây)': 20,
    },
    {
      'Câu hỏi': 'Màu của lá cây thường là gì?',
      'Đáp án A': 'Đỏ',
      'Đáp án B': 'Vàng',
      'Đáp án C': 'Xanh',
      'Đáp án D': 'Trắng',
      'Đáp án đúng': 'C',
      'Thời gian (giây)': 25,
    },
    {
      'Câu hỏi': 'Con vật nào có 4 chân?',
      'Đáp án A': 'Cá',
      'Đáp án B': 'Chim',
      'Đáp án C': 'Chó',
      'Đáp án D': 'Rắn',
      'Đáp án đúng': 'C',
      'Thời gian (giây)': 15,
    },
    {
      'Câu hỏi': 'Nước nào lớn nhất thế giới?',
      'Đáp án A': 'Trung Quốc',
      'Đáp án B': 'Mỹ',
      'Đáp án C': 'Nga',
      'Đáp án D': 'Canada',
      'Đáp án đúng': 'C',
      'Thời gian (giây)': 60,
    },
  ]

  try {
    const worksheet = XLSX.utils.json_to_sheet(sampleData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Câu hỏi')
    XLSX.writeFile(workbook, 'quiz-template.xlsx')
    console.log(' Excel template downloaded successfully')
  } catch (error) {
    console.error(' Error creating Excel template:', error)
  }
}

const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    selectedExcelFile.value = file
    importResult.value = null
  }
}

const handleDrop = (event) => {
  event.preventDefault()
  isDragOver.value = false

  const files = event.dataTransfer.files
  if (files.length > 0) {
    selectedExcelFile.value = files[0]
    importResult.value = null
  }
}

const removeExcelFile = () => {
  selectedExcelFile.value = null
  importResult.value = null
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

async function importQuiz() {
  if (!selectedExcelFile.value || !importQuizTitle.value.trim() || !importCategoryId.value) {
    alert('Vui lòng điền đầy đủ thông tin và chọn file Excel!')
    return
  }

  if (!username.value) {
    alert('Không thể xác định người dùng. Vui lòng đăng nhập lại!')
    return
  }

  isImporting.value = true
  importResult.value = null

  try {
    const formData = new FormData()
    formData.append('file', selectedExcelFile.value)
    formData.append('title', importQuizTitle.value.trim())
    formData.append('description', importQuizDescription.value.trim())
    formData.append('categoryId', importCategoryId.value)
    formData.append('username', username.value)
    formData.append('isPublic', importIsPublic.value)

    //  THÊM IMAGE VÀO FORMDATA
    if (importSelectedImage.value) {
      formData.append('image', importSelectedImage.value)
    }

    const response = await api.post('/quiz/import-excel-with-image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        Authorization: `Bearer ${localStorage.getItem('token')}`,
      },
    })

    importResult.value = response.data

    if (response.data.success) {
      message.value = 'Import quiz thành công!'
      messageType.value = 'success'

      //  HIỂN THỊ QUIZ CODE CHO IMPORT
      if (response.data.quiz?.quizCode) {
        showQuizCode(response.data.quiz.quizCode, response.data.quiz.id)
      } else if (response.data.quizCode && response.data.id) {
        showQuizCode(response.data.quizCode, response.data.id)
      }

      // Reset form và refresh quiz list
      setTimeout(() => {
        resetImportForm()
        fetchQuizzes()
        activeTab.value = 'create' // Chuyển về tab tạo mới
      }, 3000)
    }
  } catch (error) {
    importResult.value = {
      success: false,
      message: error.response?.data?.message || 'Có lỗi xảy ra khi import',
    }
  } finally {
    isImporting.value = false
  }
}

const resetImportForm = () => {
  importQuizTitle.value = ''
  importQuizDescription.value = ''
  importCategoryId.value = ''
  selectedExcelFile.value = null
  importSelectedImage.value = null
  importPreviewUrl.value = null
  importIsPublic.value = true
  importResult.value = null
}

function handleImportImageUpload(event) {
  const file = event.target.files[0]
  if (file) {
    // Validate file type
    if (!file.type.startsWith('image/')) {
      alert('Vui lòng chọn file ảnh!')
      return
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      alert('File ảnh quá lớn! Tối đa 5MB.')
      return
    }

    importSelectedImage.value = file
    importPreviewUrl.value = URL.createObjectURL(file)
  }
}

function removeImportImage() {
  importSelectedImage.value = null
  importPreviewUrl.value = null
}

//  HIỂN THỊ QUIZ CODE
const showQuizCode = (code, quizId = null) => {
  quizCode.value = code
  if (quizId) {
    //  LƯU QUIZ INFO ĐỂ SHARE
    quizInfo.value = {
      quizId: quizId,
      quizCode: code,
    }
  }
  showCodeModal.value = true
}

//  COPY CODE
const copyQuizCode = async () => {
  try {
    await navigator.clipboard.writeText(quizCode.value)
    message.value = 'Đã copy mã code!'
    messageType.value = 'success'
  } catch (error) {
    console.error('Lỗi khi copy:', error)
    message.value = 'Lỗi khi copy mã code'
    messageType.value = 'error'
  }
}

//  SHARE CODE
const shareCode = async () => {
  try {
    //  TẠO LINK TRỰC TIẾP ĐẾN QUIZ PLAY PAGE
    const userId = localStorage.getItem('userId') || '1'
    const quizId = quizInfo.value?.quizId
    const shareUrl = `${window.location.origin}/quiz/${quizId}/${userId}/play`
    const shareText = `Tham gia quiz với mã code: ${quizCode.value}\nLink trực tiếp: ${shareUrl}`

    if (navigator.share) {
      await navigator.share({
        title: 'Tham gia Quiz',
        text: shareText,
        url: shareUrl,
      })
    } else {
      // Fallback: copy to clipboard
      await navigator.clipboard.writeText(shareText)
      message.value = 'Đã copy thông tin chia sẻ!'
      messageType.value = 'success'
    }
  } catch (error) {
    console.error('Error sharing:', error)
    message.value = 'Lỗi khi chia sẻ!'
    messageType.value = 'error'
  }
}

//  RESET FORM
const resetForm = () => {
  title.value = ''
  description.value = ''
  selectedCategoryId.value = ''
  isPublic.value = true
  selectedImage.value = null
  previewUrl.value = null
}
</script>

<template>
  <div class="quiz-crud-container">
    <!-- Animated Background Elements -->

    <div class="container py-5">
      <!-- Loading Spinner -->
      <div v-if="isLoading" class="loading-section">
        <div class="loading-spinner-container">
          <div class="modern-spinner">
            <div class="spinner-ring"></div>
            <div class="spinner-ring"></div>
            <div class="spinner-ring"></div>
          </div>
          <h3 class="loading-text">Đang tải dữ liệu...</h3>
          <p class="loading-subtitle">Vui lòng chờ trong giây lát</p>
        </div>
      </div>

      <div v-else>
        <!-- Enhanced Create Quiz Section -->
        <div class="row justify-content-center mb-5">
          <div class="col-xl-8 col-lg-10">
            <div class="create-quiz-card">
              <div class="card-glow"></div>
              <div class="card-header-custom">
                <!--  TAB NAVIGATION -->
                <div class="tab-navigation">
                  <button
                    @click="activeTab = 'create'"
                    :class="['tab-btn', { active: activeTab === 'create' }]"
                  >
                    <i class="bi bi-plus-circle-fill"></i>
                    <span>Tạo mới</span>
                  </button>
                  <button
                    @click="activeTab = 'import'"
                    :class="['tab-btn', { active: activeTab === 'import' }]"
                  >
                    <i class="bi bi-file-earmark-excel"></i>
                    <span>Import Excel</span>
                  </button>
                </div>

                <!--  TAB CONTENT HEADER -->
                <div class="tab-content-header">
                  <div v-if="activeTab === 'create'" class="header-info">
                    <h3 class="header-title">Tạo Quiz Mới</h3>
                    <p class="header-subtitle">Điền thông tin để tạo quiz mới của bạn</p>
                  </div>
                  <div v-else class="header-info">
                    <h3 class="header-title">Import Quiz từ Excel</h3>
                    <p class="header-subtitle">Tải lên file Excel để tạo quiz nhanh chóng</p>
                  </div>
                </div>
              </div>

              <div class="card-body-custom">
                <!--  TAB 1: TẠO MỚI -->
                <div v-if="activeTab === 'create'" class="tab-content-panel">
                  <form @submit.prevent="createQuiz" class="import-form-compact">
                    <div class="row g-3">
                      <!-- Left Column -->
                      <div class="col-md-6">
                        <!-- Quiz Title -->
                        <div class="form-group-compact">
                          <label class="form-label-compact">
                            <i class="bi bi-type me-2"></i>Tên quiz
                          </label>
                          <input
                            type="text"
                            v-model="title"
                            class="form-control-compact"
                            placeholder="Nhập tên quiz thú vị..."
                            required
                          />
                        </div>

                        <!-- Category -->
                        <div class="form-group-compact">
                          <label class="form-label-compact">
                            <i class="bi bi-bookmark-fill me-2"></i>Danh mục quiz
                          </label>
                          <select
                            v-model="selectedCategoryId"
                            class="form-control-compact"
                            required
                          >
                            <option value="" disabled>Chọn danh mục...</option>
                            <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                              {{ cat.name }}
                            </option>
                          </select>
                        </div>

                        <!-- Description -->
                        <div class="form-group-compact">
                          <label class="form-label-compact">
                            <i class="bi bi-text-paragraph me-2"></i>Mô tả
                            <span class="text-muted">(Tùy chọn)</span>
                          </label>
                          <textarea
                            v-model="description"
                            class="form-control-compact"
                            placeholder="Mô tả ngắn..."
                            rows="2"
                          ></textarea>
                        </div>
                      </div>

                      <!-- Right Column -->
                      <div class="col-md-6">
                        <!-- Image Upload - Compact -->
                        <div class="form-group-compact">
                          <label class="form-label-compact">
                            <i class="bi bi-image me-2"></i>Ảnh đại diện quiz
                            <span class="text-muted">(Tùy chọn)</span>
                          </label>
                          <div class="image-upload-compact">
                            <input
                              type="file"
                              class="d-none"
                              ref="imageInput"
                              @change="handleImageUpload"
                              accept="image/*"
                            />

                            <div v-if="!selectedImage" class="image-placeholder-compact">
                              <i class="bi bi-image"></i>
                              <p>Chọn ảnh</p>
                              <button
                                type="button"
                                @click="$refs.imageInput.click()"
                                class="btn-select-image-compact"
                              >
                                <i class="bi bi-camera"></i>
                                Chọn
                              </button>
                            </div>

                            <div v-else class="image-selected-compact">
                              <img :src="previewUrl" alt="Preview" class="image-preview-compact" />
                              <button @click="clearImage" type="button" class="btn-remove-compact">
                                <i class="bi bi-x"></i>
                              </button>
                            </div>
                          </div>
                          <small class="form-text text-muted">JPG, PNG, GIF, WebP (max 5MB)</small>
                        </div>

                        <!-- Privacy Setting - Compact -->
                        <div class="form-group-compact">
                          <label class="form-label-compact">
                            <i class="bi bi-shield-check me-2"></i>Quyền riêng tư
                          </label>
                          <div class="privacy-options-compact">
                            <div class="privacy-option-compact">
                              <input
                                class="privacy-radio-compact"
                                type="radio"
                                :value="true"
                                v-model="isPublic"
                                id="publicYesCompact"
                              />
                              <label class="privacy-label-compact" for="publicYesCompact">
                                <div class="privacy-icon-compact public-icon-compact">
                                  <i class="bi bi-globe2"></i>
                                </div>
                                <div class="privacy-content-compact">
                                  <strong>Công khai</strong>
                                  <small>Mọi người có thể xem</small>
                                </div>
                                <div class="privacy-checkmark-compact">
                                  <i class="bi bi-check-lg"></i>
                                </div>
                              </label>
                            </div>
                            <div class="privacy-option-compact">
                              <input
                                class="privacy-radio-compact"
                                type="radio"
                                :value="false"
                                v-model="isPublic"
                                id="publicNoCompact"
                              />
                              <label class="privacy-label-compact" for="publicNoCompact">
                                <div class="privacy-icon-compact private-icon-compact">
                                  <i class="bi bi-lock"></i>
                                </div>
                                <div class="privacy-content-compact">
                                  <strong>Riêng tư</strong>
                                  <small>Chỉ bạn có thể xem</small>
                                </div>
                                <div class="privacy-checkmark-compact">
                                  <i class="bi bi-check-lg"></i>
                                </div>
                              </label>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>

                    <!-- Submit Button - Enhanced -->
                    <div class="form-actions-compact">
                      <button type="submit" class="btn-create-quiz-enhanced" :disabled="isCreating">
                        <div v-if="isCreating" class="spinner-border spinner-border-sm me-2"></div>
                        <i class="bi bi-magic me-2"></i>
                        {{ isCreating ? 'Đang tạo...' : 'Tạo Quiz Ngay' }}
                      </button>
                    </div>
                  </form>
                </div>

                <!--  TAB 2: IMPORT EXCEL -->
                <div v-if="activeTab === 'import'" class="tab-content-panel">
                  <div class="import-excel-section">
                    <!-- Template Download - Compact -->
                    <div class="template-section-compact">
                      <div class="template-header-compact">
                        <h4>File mẫu Excel</h4>
                        <button @click="downloadTemplate" class="template-btn-compact">
                          <i class="bi bi-download"></i>
                          Tải mẫu
                        </button>
                      </div>

                      <!-- Template Info -->
                      <div class="template-info-compact">
                        <p><strong> Cấu trúc file Excel:</strong></p>
                        <ul>
                          <li><strong>A:</strong> STT (1, 2, 3...)</li>
                          <li><strong>B:</strong> Câu hỏi</li>
                          <li><strong>C-F:</strong> Đáp án A, B, C, D</li>
                          <li><strong>G:</strong> Đáp án đúng (A/B/C/D)</li>
                          <li>
                            <strong>H:</strong> Thời gian (giây) - mặc định 30s, range 5-300s, 0 =
                            không giới hạn
                          </li>
                        </ul>
                      </div>
                    </div>

                    <!-- Import Form - Compact Layout -->
                    <form @submit.prevent="importQuiz" class="import-form-compact">
                      <div class="row g-3">
                        <!-- Left Column -->
                        <div class="col-md-6">
                          <!-- Quiz Title -->
                          <div class="form-group-compact">
                            <label class="form-label-compact">
                              <i class="bi bi-bookmark-fill me-2"></i>Tên Quiz
                            </label>
                            <input
                              v-model="importQuizTitle"
                              type="text"
                              class="form-control-compact"
                              placeholder="Nhập tên quiz..."
                              required
                            />
                          </div>

                          <!-- Category -->
                          <div class="form-group-compact">
                            <label class="form-label-compact">
                              <i class="bi bi-folder me-2"></i>Danh mục
                            </label>
                            <select
                              v-model="importCategoryId"
                              class="form-control-compact"
                              required
                            >
                              <option value="" disabled>Chọn danh mục...</option>
                              <option
                                v-for="category in categories"
                                :key="category.id"
                                :value="category.id"
                              >
                                {{ category.name }}
                              </option>
                            </select>
                          </div>

                          <!-- Description -->
                          <div class="form-group-compact">
                            <label class="form-label-compact">
                              <i class="bi bi-text-paragraph me-2"></i>Mô tả
                              <span class="text-muted">(Tùy chọn)</span>
                            </label>
                            <textarea
                              v-model="importQuizDescription"
                              class="form-control-compact"
                              placeholder="Mô tả ngắn..."
                              rows="2"
                            ></textarea>
                          </div>
                        </div>

                        <!-- Right Column -->
                        <div class="col-md-6">
                          <!-- Image Upload - Compact -->
                          <div class="form-group-compact">
                            <label class="form-label-compact">
                              <i class="bi bi-image me-2"></i>Ảnh chủ đề
                              <span class="text-muted">(Tùy chọn)</span>
                            </label>
                            <div class="image-upload-compact">
                              <input
                                type="file"
                                class="d-none"
                                ref="importImageInput"
                                @change="handleImportImageUpload"
                                accept="image/*"
                              />

                              <div v-if="!importSelectedImage" class="image-placeholder-compact">
                                <i class="bi bi-image"></i>
                                <p>Chọn ảnh</p>
                                <button
                                  type="button"
                                  @click="$refs.importImageInput.click()"
                                  class="btn-select-image-compact"
                                >
                                  <i class="bi bi-camera"></i>
                                  Chọn
                                </button>
                              </div>

                              <div v-else class="image-selected-compact">
                                <img
                                  :src="importPreviewUrl"
                                  alt="Preview"
                                  class="image-preview-compact"
                                />
                                <button
                                  @click="removeImportImage"
                                  type="button"
                                  class="btn-remove-compact"
                                >
                                  <i class="bi bi-x"></i>
                                </button>
                              </div>
                            </div>
                            <small class="form-text text-muted"
                              >JPG, PNG, GIF, WebP (max 5MB)</small
                            >
                          </div>

                          <!-- File Upload - Compact -->
                          <div class="form-group-compact">
                            <label class="form-label-compact">
                              <i class="bi bi-file-earmark-excel me-2"></i>File Excel
                            </label>
                            <div
                              class="file-upload-compact"
                              :class="{ 'drag-over': isDragOver }"
                              @drop="handleDrop"
                              @dragover.prevent="isDragOver = true"
                              @dragleave="isDragOver = false"
                            >
                              <input
                                ref="fileInput"
                                type="file"
                                @change="handleFileSelect"
                                accept=".xlsx,.xls"
                                class="d-none"
                              />

                              <div v-if="!selectedExcelFile" class="file-placeholder-compact">
                                <i class="bi bi-file-earmark-excel"></i>
                                <p>Chọn file Excel</p>
                                <button
                                  type="button"
                                  @click="$refs.fileInput.click()"
                                  class="btn-select-file-compact"
                                >
                                  <i class="bi bi-folder2-open"></i>
                                  Chọn file
                                </button>
                              </div>

                              <div v-else class="file-selected-compact">
                                <i class="bi bi-file-earmark-excel text-success"></i>
                                <div class="file-info-compact">
                                  <span class="file-name-compact">{{
                                    selectedExcelFile.name
                                  }}</span>
                                  <small class="file-size-compact">{{
                                    formatFileSize(selectedExcelFile.size)
                                  }}</small>
                                </div>
                                <button
                                  @click="removeExcelFile"
                                  type="button"
                                  class="btn-remove-compact"
                                >
                                  <i class="bi bi-x"></i>
                                </button>
                              </div>
                            </div>
                          </div>

                          <!-- Privacy Setting - Compact -->
                          <div class="form-group-compact">
                            <label class="form-label-compact">
                              <i class="bi bi-shield-check me-2"></i>Quyền riêng tư
                            </label>
                            <div class="privacy-options-compact">
                              <div class="privacy-option-compact">
                                <input
                                  class="privacy-radio-compact"
                                  type="radio"
                                  :value="true"
                                  v-model="importIsPublic"
                                  id="importPublicYes"
                                />
                                <label class="privacy-label-compact" for="importPublicYes">
                                  <div class="privacy-icon-compact public-icon-compact">
                                    <i class="bi bi-globe2"></i>
                                  </div>
                                  <div class="privacy-content-compact">
                                    <strong>Công khai</strong>
                                    <small>Mọi người có thể xem</small>
                                  </div>
                                  <div class="privacy-checkmark-compact">
                                    <i class="bi bi-check-lg"></i>
                                  </div>
                                </label>
                              </div>
                              <div class="privacy-option-compact">
                                <input
                                  class="privacy-radio-compact"
                                  type="radio"
                                  :value="false"
                                  v-model="importIsPublic"
                                  id="importPublicNo"
                                />
                                <label class="privacy-label-compact" for="importPublicNo">
                                  <div class="privacy-icon-compact private-icon-compact">
                                    <i class="bi bi-lock"></i>
                                  </div>
                                  <div class="privacy-content-compact">
                                    <strong>Riêng tư</strong>
                                    <small>Chỉ bạn có thể xem</small>
                                  </div>
                                  <div class="privacy-checkmark-compact">
                                    <i class="bi bi-check-lg"></i>
                                  </div>
                                </label>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>

                      <!-- Submit Button - Compact -->
                      <div class="form-actions-compact">
                        <button
                          type="submit"
                          class="btn-import-compact"
                          :disabled="!canImport || isImporting"
                        >
                          <div
                            v-if="isImporting"
                            class="spinner-border spinner-border-sm me-2"
                          ></div>
                          <i class="bi bi-upload me-2"></i>
                          {{ isImporting ? 'Đang import...' : 'Import Quiz' }}
                        </button>
                      </div>
                    </form>

                    <!-- Import Result - Compact -->
                    <div
                      v-if="importResult"
                      :class="['import-result-compact', importResult.success ? 'success' : 'error']"
                    >
                      <i
                        :class="
                          importResult.success ? 'bi bi-check-circle-fill' : 'bi bi-x-circle-fill'
                        "
                      ></i>
                      <div>
                        <strong>{{ importResult.success ? 'Thành công!' : 'Thất bại!' }}</strong>
                        <p class="mb-0">{{ importResult.message }}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Enhanced Quiz List Section -->
      </div>
    </div>

    <!-- Enhanced Toast Notification -->
    <div
      v-if="message"
      :class="['toast-notification-enhanced', messageType === 'success' ? 'success' : 'error']"
    >
      <div class="toast-icon">
        <i
          :class="
            messageType === 'success'
              ? 'bi bi-check-circle-fill'
              : 'bi bi-exclamation-triangle-fill'
          "
        ></i>
      </div>
      <div class="toast-content">
        <strong class="toast-title">
          {{ messageType === 'success' ? 'Thành công!' : 'Thông báo' }}
        </strong>
        <span class="toast-message">{{ message }}</span>
      </div>
      <button class="toast-close" @click="message = ''">
        <i class="bi bi-x-lg"></i>
      </button>
    </div>
  </div>

  <!--  QUIZ CODE MODAL -->
  <div v-if="showCodeModal" class="modal-overlay" @click="showCodeModal = false">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <div class="success-icon">
          <i class="bi bi-check-circle-fill"></i>
        </div>
        <h3>Quiz đã được tạo thành công!</h3>
        <button @click="showCodeModal = false" class="modal-close">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>

      <div class="modal-body">
        <div class="code-section">
          <h4>Mã code để tham gia quiz:</h4>
          <div class="code-display">
            <div class="code-container">
              <span class="quiz-code">{{ quizCode }}</span>
              <div class="code-actions">
                <button @click="copyQuizCode" class="copy-btn" title="Copy mã code">
                  <i class="bi bi-clipboard"></i>
                  Copy
                </button>
                <button @click="shareCode" class="share-btn" title="Chia sẻ">
                  <i class="bi bi-share"></i>
                  Chia sẻ
                </button>
              </div>
            </div>

            <!--  QR CODE CHO LOCALHOST -->
            <div class="qr-section">
              <h5>QR Code để tham gia</h5>
              <div class="qr-container">
                <div class="qr-placeholder">
                  <i class="bi bi-qr-code"></i>
                  <p>Scan để tham gia quiz</p>
                  <small>localhost:3000/join-quiz</small>
                </div>
              </div>
            </div>
          </div>

          <div class="code-info">
            <div class="info-item">
              <i class="bi bi-info-circle"></i>
              <span>Chia sẻ mã code này cho học sinh để họ có thể tham gia quiz</span>
            </div>
            <div class="info-item">
              <i class="bi bi-clock"></i>
              <span>Mã code có hiệu lực vĩnh viễn</span>
            </div>
          </div>
        </div>

        <div class="modal-actions">
          <button @click="goToEditAfterCreate" class="btn btn-secondary">
            <i class="bi bi-check"></i>
            Hoàn thành
          </button>
          <RouterLink to="/join-quiz" class="btn btn-primary">
            <i class="bi bi-key"></i>
            Thử nghiệm tham gia
          </RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* === ENHANCED BASE STYLES === */
.quiz-crud-container {
  min-height: 100vh;
  background: var(--app-background);
  position: relative;
  overflow: hidden;
}

/* === ANIMATED BACKGROUND === */
.background-decorations {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.floating-orb {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  animation: float 6s ease-in-out infinite;
}

.orb-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: -5%;
  animation-delay: 0s;
}

.orb-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: -5%;
  animation-delay: 2s;
}

.orb-3 {
  width: 100px;
  height: 100px;
  top: 80%;
  left: 20%;
  animation-delay: 4s;
}

.orb-4 {
  width: 80px;
  height: 80px;
  top: 30%;
  right: 30%;
  animation-delay: 1s;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0px) rotate(0deg);
  }

  33% {
    transform: translateY(-20px) rotate(5deg);
  }

  66% {
    transform: translateY(20px) rotate(-5deg);
  }
}

/* === ENHANCED HERO SECTION === */
.hero-section {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  padding: 5rem 0 3rem;
  position: relative;
  z-index: 1;
}

.hero-content {
  position: relative;
}

.hero-icon {
  width: 100px;
  height: 100px;
  background: linear-gradient(45deg, #00d4ff, #5f27cd);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 2rem;
  box-shadow: 0 20px 60px rgba(95, 39, 205, 0.4);
  animation: pulse 2s infinite;
}

.hero-icon i {
  font-size: 3rem;
  color: white;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 800;
  background: linear-gradient(45deg, #fff, #f8f9fa, #00d4ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 1.5rem;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.hero-subtitle {
  font-size: 1.3rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 2rem;
  line-height: 1.6;
}

.hero-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 2rem;
  margin-top: 2rem;
}

.stat-item {
  text-align: center;
  color: white;
}

.stat-number {
  display: block;
  font-size: 2rem;
  font-weight: 700;
  color: #00d4ff;
  text-shadow: 0 2px 10px rgba(0, 212, 255, 0.3);
}

.stat-label {
  font-size: 0.9rem;
  opacity: 0.8;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.stat-divider {
  width: 2px;
  height: 40px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 1px;
}

/* === ENHANCED LOADING === */
.loading-section {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  position: relative;
  z-index: 1;
}

.loading-spinner-container {
  text-align: center;
  color: white;
}

.modern-spinner {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 2rem;
}

.spinner-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 3px solid transparent;
  border-top-color: #00d4ff;
  border-radius: 50%;
  animation: spin 1.5s linear infinite;
}

.spinner-ring:nth-child(2) {
  width: 80%;
  height: 80%;
  top: 10%;
  left: 10%;
  border-top-color: #5f27cd;
  animation-delay: 0.5s;
}

spinner-ring:nth-child(3) {
}

.spinner-ring:nth-child(3) {
  width: 60%;
  height: 60%;
  top: 20%;
  left: 20%;
  border-top-color: #fff;
  animation-delay: 1s;
}

.loading-text {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.loading-subtitle {
  opacity: 0.8;
  font-size: 1rem;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

/* === ENHANCED CREATE QUIZ CARD === */
.create-quiz-card {
  background: var(--card-bg);
  backdrop-filter: blur(30px);
  border-radius: 30px;
  box-shadow: 0 30px 80px var(--shadow-color);
  border: 2px solid var(--border-color);
  overflow: hidden;
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
}

.create-quiz-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 40px 100px var(--shadow-color);
}

.card-glow {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, #00d4ff, #5f27cd, #764ba2, #667eea);
  border-radius: 30px;
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.create-quiz-card:hover .card-glow {
  opacity: 0.6;
}

.card-header-custom {
  background: var(--card-header-bg);
  color: var(--card-header-text);
  padding: 3rem 2rem;
  text-align: center;
  position: relative;
  overflow: hidden;
  border-bottom: 1px solid var(--border-color);
}

/* Removed pseudo-element background since header is now white */

.header-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
}

.header-icon i {
  font-size: 2rem;
  color: #ffffff;
}

.header-title {
  font-size: 2.2rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.header-subtitle {
  opacity: 1;
  font-size: 1.1rem;
  margin: 0;
  color: var(--text-secondary);
  font-weight: 500;
}

.card-body-custom {
  padding: 3rem;
  background: var(--card-bg);
  border-top: 1px solid var(--border-color);
}

/* === ENHANCED FORM ELEMENTS === */
.form-label-custom {
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  font-size: 1.1rem;
}

.label-optional {
  font-size: 0.85rem;
  color: var(--text-secondary);
  font-weight: 500;
  margin-left: auto;
}

.input-container {
  position: relative;
}

.form-control-enhanced,
.form-select-enhanced {
  border: 2px solid var(--input-border);
  border-radius: 15px;
  padding: 1rem 1.25rem;
  font-size: 1.1rem;
  transition: all 0.3s ease;
  background: var(--input-bg);
  backdrop-filter: blur(10px);
  width: 100%;
  color: var(--text-primary);
  font-weight: 500;
}

.form-control-enhanced::placeholder,
.form-select-enhanced::placeholder {
  color: var(--text-muted);
  font-weight: 400;
}

.form-control-enhanced:focus,
.form-select-enhanced:focus {
  border-color: #00d4ff;
  box-shadow: 0 0 0 0.3rem rgba(0, 212, 255, 0.2);
  outline: none;
  background: var(--input-bg);
  transform: translateY(-2px);
  color: var(--text-primary);
}

.input-border {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 3px;
  background: linear-gradient(45deg, #00d4ff, #5f27cd);
  transition: all 0.3s ease;
  transform: translateX(-50%);
  border-radius: 2px;
}

.form-control-enhanced:focus + .input-border,
.form-select-enhanced:focus + .input-border {
  width: 100%;
}

/* === ENHANCED IMAGE UPLOAD === */
.image-upload-area-enhanced {
  position: relative;
  border: 3px dashed #cbd5e0;
  border-radius: 20px;
  overflow: hidden;
  transition: all 0.4s ease;
  background: rgba(255, 255, 255, 0.5);
}

.image-upload-area-enhanced:hover {
  border-color: #00d4ff;
  background: rgba(0, 212, 255, 0.05);
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(0, 212, 255, 0.2);
}

.image-upload-label-enhanced {
  display: block;
  cursor: pointer;
  margin: 0;
}

.upload-placeholder-enhanced {
  padding: 4rem 2rem;
  text-align: center;
}

.upload-icon-container {
  width: 80px;
  height: 80px;
  background: linear-gradient(45deg, #00d4ff, #5f27cd);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
  box-shadow: 0 15px 40px rgba(95, 39, 205, 0.3);
}

.upload-icon-container i {
  font-size: 2rem;
  color: white;
}

.upload-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 0.5rem;
}

.upload-text {
  color: #4a5568;
  margin-bottom: 1.5rem;
  font-weight: 500;
}

.upload-formats {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
}

.format-tag {
  background: rgba(0, 212, 255, 0.1);
  color: #00d4ff;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.preview-container-enhanced {
  position: relative;
  height: 250px;
}

.preview-image-enhanced {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-overlay-enhanced {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  opacity: 0;
  transition: opacity 0.3s ease;
  backdrop-filter: blur(5px);
}

.preview-container-enhanced:hover .preview-overlay-enhanced {
  opacity: 1;
}

.preview-overlay-enhanced i {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.btn-remove-image-enhanced {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(220, 53, 69, 0.9);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  box-shadow: 0 5px 20px rgba(220, 53, 69, 0.4);
}

.btn-remove-image-enhanced:hover {
  background: #dc3545;
  transform: scale(1.1);
  box-shadow: 0 8px 25px rgba(220, 53, 69, 0.6);
}

/* === ENHANCED PRIVACY OPTIONS === */
.privacy-options-enhanced {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin: 2rem 0;
  padding: 0 1rem;
}

/* Ensure proper spacing and alignment */
.privacy-options-enhanced .privacy-option-enhanced {
  display: flex;
  flex-direction: column;
}

/* Force layout improvements */
.privacy-options-enhanced {
  display: grid !important;
  grid-template-columns: 1fr 1fr !important;
  gap: 1.5rem !important;
  margin: 2rem 0 !important;
  padding: 0 1rem !important;
}

.privacy-label-enhanced {
  display: flex !important;
  align-items: center !important;
  padding: 1.75rem !important;
  border: 2px solid #374151 !important;
  border-radius: 16px !important;
  cursor: pointer !important;
  transition: all 0.3s ease !important;
  background: #1f2937 !important;
  backdrop-filter: blur(10px) !important;
  position: relative !important;
  overflow: hidden !important;
  min-height: 120px !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3) !important;
  width: 100% !important;
  box-sizing: border-box !important;
}

.privacy-option-enhanced {
  position: relative;
}

.privacy-radio {
  position: absolute;
  opacity: 0;
}

.privacy-label-enhanced {
  display: flex;
  align-items: center;
  padding: 1.75rem;
  border: 2px solid var(--border-color);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: var(--card-bg);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
  min-height: 120px;
  box-shadow: 0 4px 12px var(--shadow-color);
  width: 100%;
  box-sizing: border-box;
}

.privacy-label-enhanced::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, var(--primary-bg), transparent);
  transition: left 0.5s ease;
}

.privacy-label-enhanced:hover::before {
  left: 100%;
}

.privacy-label-enhanced:hover {
  border-color: #3b82f6;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.08), rgba(59, 130, 246, 0.03));
  transform: translateY(-3px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.privacy-radio:checked + .privacy-label-enhanced {
  border-color: #3b82f6;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(59, 130, 246, 0.05));
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.2);
  transform: translateY(-2px);
}

.privacy-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1.25rem;
  flex-shrink: 0;
  font-size: 1.5rem;
  box-shadow: 0 4px 12px var(--shadow-color);
  position: relative;
  z-index: 2;
}

.public-icon {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  border: 2px solid rgba(16, 185, 129, 0.3);
}

.private-icon {
  background: linear-gradient(135deg, #6b7280, #4b5563);
  color: white;
  border: 2px solid rgba(107, 114, 128, 0.3);
}

.privacy-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
  padding-right: 1rem;
}

.privacy-content strong {
  display: block;
  color: #ffffff;
  margin-bottom: 0.5rem;
  font-size: 1.2rem;
  font-weight: 700;
  line-height: 1.3;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.privacy-content small {
  color: #e5e7eb;
  font-weight: 500;
  font-size: 0.95rem;
  line-height: 1.4;
  opacity: 0.9;
}

.privacy-checkmark {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: scale(0);
  transition: all 0.3s ease;
  margin-left: auto;
  font-size: 1rem;
  font-weight: bold;
  border: 2px solid rgba(59, 130, 246, 0.3);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.privacy-radio:checked + .privacy-label-enhanced .privacy-checkmark {
  opacity: 1;
  transform: scale(1);
}

/* Focus states for accessibility */
.privacy-label-enhanced:focus-within {
  outline: 2px solid #3b82f6;
  outline-offset: 3px;
  border-color: #3b82f6;
}

/* Smooth transitions for all interactive elements */
.privacy-label-enhanced * {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Enhanced visual feedback */
.privacy-radio:checked + .privacy-label-enhanced .privacy-icon {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.3);
}

.privacy-radio:checked + .privacy-label-enhanced .privacy-content strong {
  color: #3b82f6;
  text-shadow: 0 1px 2px rgba(59, 130, 246, 0.2);
}

/* Additional hover effects */
.privacy-label-enhanced:hover .privacy-icon {
  transform: scale(1.02);
}

.privacy-label-enhanced:hover .privacy-content strong {
  color: #60a5fa;
}

/* Dark mode specific improvements */
.privacy-label-enhanced {
  background: #1f2937;
  border-color: #374151;
}

.privacy-label-enhanced:hover {
  background: #374151;
  border-color: #3b82f6;
}

.privacy-radio:checked + .privacy-label-enhanced {
  background: #1e40af;
  border-color: #3b82f6;
}

.privacy-content strong {
  color: #f9fafb;
}

.privacy-content small {
  color: #d1d5db;
}

/* Ensure proper contrast in all states */
.privacy-label-enhanced .privacy-content strong {
  color: #f9fafb !important;
}

.privacy-label-enhanced .privacy-content small {
  color: #d1d5db !important;
}

.privacy-radio:checked + .privacy-label-enhanced .privacy-content strong {
  color: #60a5fa !important;
}

/* === ENHANCED SUBMIT BUTTON === */
.btn-create-quiz-enhanced {
  background: linear-gradient(135deg, #dc3545 0%, #c82333 50%, #a71e2a 100%) !important;
  color: white !important;
  border: 3px solid #c82333 !important;
  padding: 1.25rem 3rem;
  border-radius: 50px;
  font-weight: 700;
  font-size: 1.1rem;
  cursor: pointer;
  transition: all 0.4s ease;
  box-shadow: 0 8px 25px rgba(220, 53, 69, 0.4) !important;
  position: relative;
  overflow: hidden;
  min-width: 200px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5) !important;
  z-index: 1000 !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  gap: 0.75rem !important;
}

.btn-create-quiz-enhanced:hover:not(:disabled) {
  transform: translateY(-3px) !important;
  box-shadow: 0 15px 40px rgba(220, 53, 69, 0.6) !important;
  background: linear-gradient(135deg, #c82333 0%, #a71e2a 50%, #721c24 100%) !important;
  border-color: #a71e2a !important;
}

.btn-create-quiz-enhanced:active {
  transform: translateY(-1px);
}

.btn-create-quiz-enhanced:disabled {
  opacity: 0.8 !important;
  cursor: not-allowed !important;
  background: linear-gradient(135deg, #6c757d 0%, #5a6268 50%, #495057 100%) !important;
  border-color: #5a6268 !important;
}

/* Đảm bảo button luôn visible và không bị override */
.btn-create-quiz-enhanced {
  visibility: visible !important;
  opacity: 1 !important;
}

/* Thêm outline cho accessibility */
.btn-create-quiz-enhanced:focus {
  outline: 3px solid rgba(220, 53, 69, 0.5) !important;
  outline-offset: 2px !important;
}

/* Đảm bảo button container có spacing phù hợp */
.form-actions-compact {
  text-align: center;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-color);
}

.btn-content {
  position: relative;
  z-index: 2;
}

.btn-normal {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
}

.btn-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
}

.spinner-dots {
  display: flex;
  gap: 0.25rem;
}

.spinner-dots div {
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
  animation: bounce 1.4s ease-in-out infinite both;
}

.dot2 {
  animation-delay: 0.16s;
}

.dot3 {
  animation-delay: 0.32s;
}

@keyframes bounce {
  0%,
  80%,
  100% {
    transform: scale(0);
  }

  40% {
    transform: scale(1);
  }
}

.btn-ripple {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition:
    width 0.6s,
    height 0.6s;
}

.btn-create-quiz-enhanced:active .btn-ripple {
  width: 300px;
  height: 300px;
}

/* === ENHANCED QUIZ LIST SECTION === */
.quiz-list-section {
  position: relative;
  z-index: 1;
}

.section-header {
  text-align: center;
  margin-bottom: 3rem;
  color: white;
}

.section-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
  backdrop-filter: blur(20px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.2);
}

.section-icon i {
  font-size: 2rem;
  color: white;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
}

.quiz-count-enhanced {
  background: rgba(0, 212, 255, 0.2);
  color: #00d4ff;
  padding: 0.5rem 1rem;
  border-radius: 25px;
  font-size: 1rem;
  backdrop-filter: blur(10px);
  box-shadow: 0 5px 20px rgba(0, 212, 255, 0.2);
}

.section-subtitle {
  opacity: 0.9;
  font-size: 1.1rem;
  margin: 0;
}

/* === ENHANCED LOADING SKELETON === */
.loading-quiz-section {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 2rem;
  margin-top: 2rem;
}

.quiz-skeleton {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  overflow: hidden;
  backdrop-filter: blur(20px);
}

.skeleton-image {
  height: 200px;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.1) 25%,
    rgba(255, 255, 255, 0.2) 50%,
    rgba(255, 255, 255, 0.1) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 2s infinite;
}

.skeleton-content {
  padding: 1.5rem;
}

.skeleton-line {
  height: 1rem;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.1) 25%,
    rgba(255, 255, 255, 0.2) 50%,
    rgba(255, 255, 255, 0.1) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 2s infinite;
  border-radius: 0.5rem;
  margin-bottom: 1rem;
}

.skeleton-line.large {
  width: 80%;
}

.skeleton-line.medium {
  width: 60%;
}

.skeleton-line.small {
  width: 40%;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }

  100% {
    background-position: 200% 0;
  }
}

/* === ENHANCED EMPTY STATE === */
.empty-state-enhanced {
  text-align: center;
  padding: 5rem 2rem;
  color: white;
  position: relative;
}

.empty-icon {
  width: 120px;
  height: 120px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 2rem;
  backdrop-filter: blur(20px);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.empty-icon i {
  font-size: 3rem;
  opacity: 0.7;
}

.empty-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.empty-text {
  font-size: 1.1rem;
  opacity: 0.8;
  max-width: 400px;
  margin: 0 auto 2rem;
  line-height: 1.6;
}

.empty-decoration {
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.decoration-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  animation: pulse 2s infinite;
}

.dot-2 {
  animation-delay: 0.5s;
}

.dot-3 {
  animation-delay: 1s;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.3;
    transform: scale(1);
  }

  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

/* === ENHANCED QUIZ GRID === */
.quiz-grid-enhanced {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 2.5rem;
  margin-top: 3rem;
}

/* === ENHANCED QUIZ CARD === */
.quiz-card-enhanced {
  background: var(--card-bg);
  backdrop-filter: blur(30px);
  border-radius: 25px;
  overflow: hidden;
  box-shadow: 0 15px 50px var(--shadow-color);
  transition: all 0.4s ease;
  border: 2px solid var(--border-color);
  position: relative;
}

.quiz-card-enhanced:hover {
  transform: translateY(-10px) scale(1.02);
  box-shadow: 0 30px 80px var(--shadow-color);
  border-color: rgba(0, 212, 255, 0.5);
}

.card-inner {
  position: relative;
  height: 100%;
}

.quiz-image-container-enhanced {
  position: relative;
  height: 220px;
  overflow: hidden;
}

.quiz-image-enhanced {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.quiz-card-enhanced:hover .quiz-image-enhanced {
  transform: scale(1.1);
}

.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 50%;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.quiz-card-enhanced:hover .image-overlay {
  opacity: 1;
}

.quiz-status-enhanced {
  position: absolute;
  top: 1rem;
  right: 1rem;
  z-index: 2;
}

.status-badge-enhanced {
  padding: 0.75rem 1.25rem;
  border-radius: 25px;
  font-size: 0.8rem;
  font-weight: 700;
  backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 5px 20px var(--shadow-color);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-badge-enhanced.public {
  background: rgba(72, 187, 120, 0.9);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.status-badge-enhanced.private {
  background: rgba(160, 174, 192, 0.9);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* 🔔 Badge cảnh báo chưa có question */
.status-badge-empty {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  margin-left: 8px;
  padding: 0.5rem 0.9rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 700;
  backdrop-filter: blur(12px);
  background: rgba(245, 101, 101, 0.9);
  color: #fff;
  box-shadow: 0 4px 14px rgba(245, 101, 101, 0.35);
}

.quiz-content-enhanced {
  padding: 2rem;
  height: calc(100% - 220px);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.quiz-title-enhanced {
  margin-bottom: 1rem;
  color: var(--text-primary);
  font-weight: 800;
  font-size: 1.3rem;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.quiz-category-enhanced {
  color: var(--text-secondary);
  font-size: 0.95rem;
  margin-bottom: 1.5rem;
  font-weight: 600;
}

/* === ENHANCED QUIZ ACTIONS === */
.quiz-actions-enhanced {
  display: flex;
  gap: 0.75rem;
  margin-top: auto;
}

.action-btn-enhanced {
  flex: 1;
  padding: 0.875rem 1rem;
  border: none;
  border-radius: 15px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  position: relative;
  overflow: hidden;
}

.action-btn-enhanced::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.action-btn-enhanced:hover::before {
  left: 100%;
}

.play-btn-enhanced {
  background: linear-gradient(135deg, #48bb78, #38a169);
  color: white;
  box-shadow: 0 5px 20px rgba(72, 187, 120, 0.3);
}

.play-btn-enhanced:hover {
  background: linear-gradient(135deg, #38a169, #2f855a);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(72, 187, 120, 0.5);
}

.edit-btn-enhanced {
  background: linear-gradient(135deg, #4299e1, #3182ce);
  color: white;
  box-shadow: 0 5px 20px rgba(66, 153, 225, 0.3);
}

.edit-btn-enhanced:hover {
  background: linear-gradient(135deg, #3182ce, #2b77cb);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(66, 153, 225, 0.5);
}

.delete-btn-enhanced {
  background: linear-gradient(135deg, #f56565, #e53e3e);
  color: white;
  box-shadow: 0 5px 20px rgba(245, 101, 101, 0.3);
}

.delete-btn-enhanced:hover {
  background: linear-gradient(135deg, #e53e3e, #c53030);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(245, 101, 101, 0.5);
}

/* Disabled state for action buttons */
.action-btn-enhanced[disabled] {
  opacity: 0.6;
  cursor: not-allowed;
  filter: grayscale(0.15);
}

/* === ENHANCED TOAST NOTIFICATION === */
.toast-notification-enhanced {
  position: fixed;
  top: 2rem;
  right: 2rem;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 1.5rem;
  color: #1a202c;
  font-weight: 500;
  z-index: 1000;
  animation: slideInRight 0.4s ease;
  display: flex;
  align-items: center;
  gap: 1rem;
  max-width: 450px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.toast-notification-enhanced.success {
  border-left: 5px solid #48bb78;
}

.toast-notification-enhanced.error {
  border-left: 5px solid #f56565;
}

.toast-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.success .toast-icon {
  background: rgba(72, 187, 120, 0.1);
  color: #48bb78;
}

.error .toast-icon {
  background: rgba(245, 101, 101, 0.1);
  color: #f56565;
}

.toast-content {
  flex: 1;
}

.toast-title {
  display: block;
  font-size: 1rem;
  margin-bottom: 0.25rem;
}

.toast-message {
  font-size: 0.9rem;
  opacity: 0.8;
}

.toast-close {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.toast-close:hover {
  background: rgba(0, 0, 0, 0.2);
  transform: scale(1.1);
}

/* === ANIMATIONS === */
@keyframes slideInRight {
  from {
    transform: translateX(100%);
    opacity: 0;
  }

  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* === RESPONSIVE DESIGN === */
@media (max-width: 992px) {
  .hero-title {
    font-size: 2.5rem;
  }

  .hero-stats {
    gap: 1rem;
  }

  .quiz-grid-enhanced {
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 2rem;
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2rem;
  }

  .hero-subtitle {
    font-size: 1.1rem;
  }

  .hero-stats {
    flex-direction: column;
    gap: 1rem;
  }

  .stat-divider {
    width: 40px;
    height: 2px;
  }

  .card-header-custom,
  .card-body-custom {
    padding: 2rem 1.5rem;
  }

  .privacy-options-enhanced {
    grid-template-columns: 1fr;
  }

  .quiz-grid-enhanced {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .loading-quiz-section {
    grid-template-columns: 1fr;
  }

  .quiz-actions-enhanced {
    flex-direction: column;
  }

  .action-btn-enhanced {
    flex: none;
    width: 100%;
  }

  .toast-notification-enhanced {
    top: 1rem;
    right: 1rem;
    left: 1rem;
    max-width: none;
  }
}

@media (max-width: 480px) {
  .hero-section {
    padding: 3rem 0 2rem;
  }

  .hero-icon {
    width: 70px;
    height: 70px;
  }

  .hero-icon i {
    font-size: 2rem;
  }

  .container {
    padding: 0 1rem;
  }

  .card-body-custom {
    padding: 1.5rem;
  }

  .btn-create-quiz-enhanced {
    padding: 1rem 2rem;
    font-size: 1rem;
  }

  .privacy-options-enhanced {
    gap: 1rem;
    margin: 1.5rem 0;
  }

  .privacy-label-enhanced {
    padding: 1.25rem;
    min-height: 100px;
  }

  .privacy-icon {
    width: 48px;
    height: 48px;
    font-size: 1.25rem;
    margin-right: 1rem;
  }

  .privacy-content strong {
    font-size: 1.1rem;
    margin-bottom: 0.375rem;
  }

  .privacy-content small {
    font-size: 0.9rem;
  }
}

/* === ACCESSIBILITY === */
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/*  TAB SYSTEM STYLES */
.tab-navigation {
  display: flex;
  gap: 0;
  margin-bottom: 20px;
  background: var(--navbar-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 4px;
  backdrop-filter: blur(10px);
}

.tab-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 20px;
  background: var(--navbar-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-primary);
  font-weight: 600;
  transition: all 0.3s ease;
  cursor: pointer;
}

.tab-btn:hover {
  color: var(--text-primary);
  background: var(--bg-secondary);
  border-color: var(--border-color);
}

.tab-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border-color: var(--border-color);
}

.tab-btn i {
  font-size: 16px;
}

.tab-content-header {
  text-align: center;
  margin-bottom: 0;
}

.header-info {
  animation: fadeInUp 0.5s ease;
}

.tab-content-panel {
  animation: fadeInUp 0.5s ease;
}

/*  IMPORT EXCEL STYLES */
.import-excel-section {
  max-width: 100%;
}

.template-section-compact {
  background: var(--bg-secondary);
  border: 2px dashed var(--success-color);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px var(--shadow-color);
}

.template-header-compact {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.template-header-compact h4 {
  margin: 0;
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 600;
}

.template-btn-compact {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 2px 8px rgba(40, 167, 69, 0.2);
}

.template-btn-compact:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(40, 167, 69, 0.3);
}

.template-info-compact {
  margin-top: 16px;
  padding: 16px;
  background: var(--card-bg);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.template-info-compact p {
  margin: 0 0 12px 0;
  color: var(--text-primary);
  font-size: 14px;
}

.template-info-compact ul {
  margin: 0;
  padding-left: 20px;
  list-style: none;
}

.template-info-compact li {
  margin-bottom: 6px;
  color: var(--text-secondary);
  font-size: 13px;
  position: relative;
}

.template-info-compact li:before {
  content: '•';
  color: #28a745;
  font-weight: bold;
  position: absolute;
  left: -15px;
}

.import-form-compact {
  margin-top: 24px;
  background: var(--card-bg);
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 20px var(--shadow-color);
  border: 1px solid var(--border-color);
}

.form-group-compact {
  margin-bottom: 20px;
}

.form-label-compact {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

.form-label-compact i {
  color: var(--text-secondary);
  margin-right: 6px;
}

.form-control-compact {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid var(--input-border);
  border-radius: 8px;
  background: var(--input-bg);
  color: var(--text-primary);
  font-size: 14px;
  transition: all 0.3s ease;
  font-family: inherit;
}

.form-control-compact:focus {
  outline: none;
  border-color: var(--primary-color);
  background: var(--input-bg);
  box-shadow: 0 0 0 3px var(--primary-shadow);
}

.form-control-compact::placeholder {
  color: var(--text-muted);
}

/* Image Upload Compact - Light Theme */
.image-upload-compact {
  border: 2px dashed var(--border-color);
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  background: var(--bg-secondary);
  transition: all 0.3s ease;
  cursor: pointer;
}

.image-upload-compact:hover {
  border-color: var(--primary-color);
  background: var(--primary-bg);
}

.image-placeholder-compact {
  color: var(--text-secondary);
}

.image-placeholder-compact i {
  font-size: 32px;
  margin-bottom: 12px;
  display: block;
  color: var(--text-muted);
}

.image-placeholder-compact p {
  margin: 12px 0;
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.btn-select-image-compact {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 2px 8px var(--primary-shadow);
}

.btn-select-image-compact:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px var(--primary-shadow);
}

.image-selected-compact {
  position: relative;
  display: inline-block;
}

.image-preview-compact {
  max-width: 120px;
  max-height: 90px;
  border-radius: 8px;
  object-fit: cover;
  border: 2px solid #e9ecef;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.btn-remove-compact {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #dc3545;
  color: white;
  border: none;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(220, 53, 69, 0.3);
}

.btn-remove-compact:hover {
  background: #c82333;
  transform: scale(1.1);
}

/* File Upload Compact - Theme Aware */
.file-upload-compact {
  border: 2px dashed var(--border-color);
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  background: var(--bg-secondary);
  transition: all 0.3s ease;
  cursor: pointer;
}

.file-upload-compact:hover,
.file-upload-compact.drag-over {
  border-color: var(--success-color);
  background: var(--success-bg);
}

.file-placeholder-compact {
  color: var(--text-secondary);
}

.file-placeholder-compact i {
  font-size: 32px;
  margin-bottom: 12px;
  display: block;
  color: #28a745;
}

.file-placeholder-compact p {
  margin: 12px 0;
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.btn-select-file-compact {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 2px 8px rgba(40, 167, 69, 0.2);
}

.btn-select-file-compact:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(40, 167, 69, 0.3);
}

.file-selected-compact {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--success-bg);
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid var(--success-border);
}

.file-selected-compact i {
  font-size: 24px;
  color: #28a745;
}

.file-info-compact {
  flex: 1;
  text-align: left;
}

.file-name-compact {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--success-text);
  margin-bottom: 2px;
}

.file-size-compact {
  font-size: 12px;
  color: var(--text-secondary);
}

/* Form Actions Compact - Light Theme */
.form-actions-compact {
  margin-top: 32px;
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid var(--border-color);
}

.btn-import-compact {
  background: linear-gradient(135deg, #dc3545 0%, #c82333 50%, #a71e2a 100%) !important;
  color: white !important;
  border: 3px solid #c82333 !important;
  padding: 1.25rem 3rem !important;
  border-radius: 50px !important;
  font-weight: 700 !important;
  font-size: 1.1rem !important;
  cursor: pointer !important;
  transition: all 0.4s ease !important;
  box-shadow: 0 8px 25px rgba(220, 53, 69, 0.4) !important;
  position: relative !important;
  overflow: hidden !important;
  min-width: 200px !important;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5) !important;
  z-index: 1000 !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  gap: 0.75rem !important;
}

.btn-import-compact:hover:not(:disabled) {
  transform: translateY(-3px) !important;
  box-shadow: 0 15px 40px rgba(220, 53, 69, 0.6) !important;
  background: linear-gradient(135deg, #c82333 0%, #a71e2a 50%, #721c24 100%) !important;
  border-color: #a71e2a !important;
}

.btn-import-compact:active {
  transform: translateY(-1px) !important;
}

.btn-import-compact:disabled {
  opacity: 0.8 !important;
  cursor: not-allowed !important;
  background: linear-gradient(135deg, #6c757d 0%, #5a6268 50%, #495057 100%) !important;
  border-color: #5a6268 !important;
  transform: none !important;
  box-shadow: none !important;
}

/* Thêm hiệu ứng ripple cho nút import compact */
.btn-import-compact::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition:
    width 0.6s,
    height 0.6s;
}

.btn-import-compact:active::before {
  width: 300px;
  height: 300px;
}

/* Thêm hiệu ứng glow khi hover */
.btn-import-compact::after {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, #dc3545, #c82333, #a71e2a, #721c24);
  border-radius: 50px;
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.btn-import-compact:hover::after {
  opacity: 0.6;
}

/* Focus state cho accessibility */
.btn-import-compact:focus {
  outline: 3px solid rgba(220, 53, 69, 0.5) !important;
  outline-offset: 2px !important;
}

/* Import Result Compact - Light Theme */
.import-result-compact {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 20px;
  border-radius: 8px;
  margin-top: 20px;
  font-size: 14px;
  border-left: 4px solid;
}

.import-result-compact.success {
  background: var(--success-bg);
  border-left-color: var(--success-color);
  border: 1px solid var(--success-border);
  color: var(--success-text);
}

.import-result-compact.error {
  background: var(--danger-bg);
  border-left-color: var(--danger-color);
  border: 1px solid var(--danger-border);
  color: var(--danger-text);
}

.import-result-compact i {
  font-size: 20px;
  margin-top: 2px;
}

.import-result-compact.success i {
  color: var(--success-color);
}

.import-result-compact.error i {
  color: var(--danger-color);
}

.import-result-compact strong {
  display: block;
  margin-bottom: 4px;
  font-weight: 600;
}

/* Small text styling */
.form-text {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 6px;
}

.text-muted {
  color: var(--text-secondary) !important;
  font-weight: 400;
}

/* Responsive improvements */
@media (max-width: 768px) {
  .import-form-compact {
    padding: 16px;
  }

  .template-section-compact {
    padding: 16px;
  }

  .template-header-compact {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }

  .form-actions-compact {
    margin-top: 24px;
    padding-top: 16px;
  }

  .btn-import-compact {
    padding: 1rem 2rem !important;
    font-size: 1rem !important;
    min-width: 160px !important;
  }
}

@media (max-width: 480px) {
  .btn-import-compact {
    padding: 0.875rem 1.5rem !important;
    font-size: 0.95rem !important;
    min-width: 140px !important;
  }
}

/* === QUIZ CODE MODAL === */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: var(--card-bg);
  border-radius: 20px;
  box-shadow: 0 20px 40px var(--shadow-color);
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 25px;
  border-radius: 20px 20px 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.success-icon {
  font-size: 2rem;
  color: #28a745;
  margin-right: 15px;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  flex: 1;
}

.modal-close {
  background: none;
  border: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 5px;
  border-radius: 50%;
  transition: background 0.3s ease;
}

.modal-close:hover {
  background: rgba(255, 255, 255, 0.2);
}

.modal-body {
  padding: 30px;
}

.code-section {
  text-align: center;
  margin-bottom: 30px;
}

.code-section h4 {
  color: #333;
  margin-bottom: 20px;
  font-size: 1.2rem;
}

.code-display {
  background: #f8f9fa;
  border: 2px solid #e9ecef;
  border-radius: 15px;
  padding: 25px;
  margin-bottom: 20px;
}

.code-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.quiz-code {
  font-size: 2.5rem;
  font-weight: 700;
  color: #667eea;
  letter-spacing: 4px;
  font-family: 'Courier New', monospace;
  background: white;
  padding: 15px 25px;
  border-radius: 10px;
  border: 2px solid #e9ecef;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.code-actions {
  display: flex;
  gap: 10px;
}

.copy-btn,
.share-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.copy-btn {
  background: #667eea;
  color: white;
}

.copy-btn:hover {
  background: #5a6fd8;
  transform: translateY(-1px);
}

.share-btn {
  background: #28a745;
  color: white;
}

.share-btn:hover {
  background: #218838;
  transform: translateY(-1px);
}

.code-info {
  margin-top: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 8px;
}

.info-item i {
  color: #667eea;
  font-size: 1rem;
}

.modal-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.modal-actions .btn {
  padding: 12px 25px;
  border-radius: 10px;
  font-weight: 600;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.btn-secondary {
  background: #6c757d;
  color: white;
  border: none;
}

.btn-secondary:hover {
  background: #5a6268;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.code-instruction {
  color: #666;
  font-size: 0.9rem;
  margin: 0;
}

.qr-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
}

.qr-section h5 {
  color: #333;
  margin-bottom: 15px;
  font-size: 1rem;
}

.qr-container {
  display: flex;
  justify-content: center;
}

.qr-placeholder {
  background: #f8f9fa;
  border: 2px dashed #dee2e6;
  border-radius: 10px;
  padding: 20px;
  text-align: center;
  width: 150px;
  height: 150px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.qr-placeholder i {
  font-size: 3rem;
  color: #6c757d;
  margin-bottom: 10px;
}

.qr-placeholder p {
  margin: 5px 0;
  color: #333;
  font-weight: 600;
}

.qr-placeholder small {
  color: #6c757d;
  font-size: 0.8rem;
}

/* Giữ layout hiện tại của khối file */
.file-selected-compact {
  position: relative;
  /* tạo context cho nút bên trong */
}

/* Ghi đè riêng cho nút remove trong khối file đã chọn */
.file-selected-compact .btn-remove-compact {
  position: static;
  /* bỏ absolute */
  margin-left: auto;
  /* đẩy nút sang phải trong flex row */
  width: 28px;
  height: 28px;
}

.file-selected-compact {
  position: relative;
}

.file-selected-compact .btn-remove-compact {
  position: absolute;
  top: -8px;
  right: -8px;
}

.file-selected-compact {
  position: relative;
}

.file-selected-compact .btn-remove-compact {
  position: absolute;
  top: 0;
  right: 0;
  transform: translate(50%, -50%);
  /* kéo ra ngoài góc một nửa kích thước */
  width: 28px;
  height: 28px;
  border-radius: 50%;
  padding: 0;
  display: flex;
  /* căn giữa icon */
  align-items: center;
  justify-content: center;
  line-height: 1;
  /* tránh lệch do line-height */
  font-size: 14px;
  /* kích thước icon */
}
</style>
