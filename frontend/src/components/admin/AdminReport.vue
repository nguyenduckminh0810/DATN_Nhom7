<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useThemeStore } from '@/stores/theme'
import { storeToRefs } from 'pinia'


const reports = ref([])
const loading = ref(false)
const error = ref('')
const currentPage = ref(0)
const totalPages = ref(1)
const selectedStatus = ref('ALL')
const pageSize = 10
// ✅ THÊM REACTIVE DATA CHO ADMIN NOTES
const adminNotes = ref({})


const statusOptions = [
  { value: 'ALL', label: 'Tất cả', class: 'btn-secondary' },
  { value: 'PENDING', label: 'Đang chờ', class: 'btn-warning' },
  { value: 'RESOLVED', label: 'Đã xử lý', class: 'btn-success' },
  { value: 'REJECTED', label: 'Từ chối', class: 'btn-danger' }
]


async function fetchReports(page = 0) {
  loading.value = true
  error.value = ''
  
  try {
    let url = `http://localhost:8080/api/reports?page=${page}&size=${pageSize}`
    
 
    if (selectedStatus.value !== 'ALL') {
      url = `http://localhost:8080/api/reports/status/${selectedStatus.value}?page=${page}&size=${pageSize}`
    }
    
    const response = await axios.get(url)
    
    reports.value = response.data.reports
    currentPage.value = response.data.currentPage
    totalPages.value = response.data.totalPages
    
    console.log('📊 Fetched reports:', response.data)
    
  } catch (err) {
    error.value = 'Không thể tải danh sách báo cáo'
    console.error('❌ Error fetching reports:', err)
  } finally {
    loading.value = false
  }
}


async function updateReportStatus(reportId, newStatus) {
  try {
    // ✅ SỬ DỤNG ADMIN NOTE TỪ REACTIVE DATA
    const adminNote = adminNotes.value[reportId] || '';
    const adminResponse = newStatus === 'RESOLVED' 
      ? (adminNote || 'Báo cáo hợp lệ, quiz đã bị ẩn')
      : (adminNote || 'Báo cáo không hợp lệ');
    
    // ✅ MAP RESOLVED THÀNH APPROVED CHO BACKEND
    const backendAction = newStatus === 'RESOLVED' ? 'APPROVED' : newStatus;
    
    console.log('🔧 Sending report action:', {
      reportId,
      action: newStatus,
      backendAction: backendAction,
      adminResponse,
      adminNote
    });
    
    const response = await axios.put(`http://localhost:8080/api/reports/${reportId}/action`, {
      reportId: reportId,
      action: backendAction,
      adminResponse: adminResponse,
      adminNote: adminNote
    })
    
    console.log('✅ Updated status:', response.data)
    
    // Reload reports
    await fetchReports(currentPage.value)

    alert('Cập nhật trạng thái thành công!')
    
  } catch (err) {
    console.error('❌ Error updating status:', err)
    alert('Lỗi khi cập nhật trạng thái!')
  }
}


async function deleteReport(reportId) {
  if (!confirm('Bạn có chắc chắn muốn xóa báo cáo này?')) {
    return
  }
  
  try {
    const response = await axios.delete(`http://localhost:8080/api/reports/${reportId}`)
    
    console.log('🗑️ Deleted report:', response.data)
    

    await fetchReports(currentPage.value)
    
    alert('Xóa báo cáo thành công!')
    
  } catch (err) {
    console.error('❌ Error deleting report:', err)
    alert('Lỗi khi xóa báo cáo!')
  }
}


function filterByStatus(status) {
  selectedStatus.value = status
  currentPage.value = 0
  fetchReports(0)
}


function goToPage(page) {
  if (page >= 0 && page < totalPages.value) {
    fetchReports(page)
  }
}


function formatDate(dateString) {
  return new Date(dateString).toLocaleString('vi-VN')
}


function getStatusClass(status) {
  switch (status) {
    case 'PENDING': return 'badge bg-warning'
    case 'RESOLVED': return 'badge bg-success'
    case 'REJECTED': return 'badge bg-danger'
    default: return 'badge bg-secondary'
  }
}


function getStatusText(status) {
  switch (status) {
    case 'PENDING': return 'Đang chờ'
    case 'RESOLVED': return 'Đã xử lý'
    case 'REJECTED': return 'Từ chối'
    default: return status
  }
}


onMounted(() => {
  fetchReports()
})
const themeStore = useThemeStore()
const { isDarkMode } = storeToRefs(themeStore)
</script>

<template>
  <div class="admin-reports" :class="{ 'is-light': !isDarkMode, 'is-dark': isDarkMode }">
    <div class="page-header">
      <div class="title-card">
        <div class="page-title">
          <div class="icon-badge"><i class="bi bi-flag"></i></div>
          <div class="title-text">
            <h1>Quản lý Báo cáo</h1>
            <p>Theo dõi và xử lý các báo cáo vi phạm</p>
          </div>
        </div>
      </div>
      <div class="page-actions">
        <div class="btn-group" role="group">
          <button v-for="option in statusOptions" :key="option.value" type="button"
                  :class="['btn', option.class, { 'active': selectedStatus === option.value }]"
                  @click="filterByStatus(option.value)">{{ option.label }}</button>
        </div>
        <button class="btn btn-outline" @click="fetchReports(currentPage)"><i class="bi bi-arrow-clockwise"></i> Làm mới</button>
      </div>
    </div>

    
    <div v-if="loading" class="text-center py-4">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Đang tải...</span>
      </div>
      <p class="mt-2">Đang tải danh sách báo cáo...</p>
    </div>

    
    <div v-else-if="error" class="alert alert-danger">
      <i class="bi bi-exclamation-circle me-2"></i>
      {{ error }}
    </div>

    
    <div v-else class="panel">
      <div class="panel-body no-padding">
        <div v-if="reports.length === 0" class="text-center py-5">
          <i class="bi bi-inbox display-1 text-muted"></i>
          <p class="text-muted mt-3">Không có báo cáo nào</p>
        </div>
        
        <div v-else class="table-wrap">
          <table class="modern-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Người báo cáo</th>
                <th>Quiz</th>
                <th>Lý do</th>
                <th>Trạng thái</th>
                <th>Ngày tạo</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="report in reports" :key="report.id">
                <td class="muted"><strong>#{{ report.id }}</strong></td>
                <td>
                  <div>
                    <strong>{{ report.userFullName || 'N/A' }}</strong>
                    <br>
                    <small class="muted">{{ report.userEmail || 'N/A' }}</small>
                  </div>
                </td>
                <td>
                  <div>
                    <strong>{{ report.quizTitle || 'N/A' }}</strong>
                    <br>
                    <small class="muted">ID: {{ report.quizId }}</small>
                  </div>
                </td>
                <td>
                  <span class="text-wrap" style="max-width: 200px;">
                    {{ report.reason }}
                  </span>
                </td>
                <td>
                  <span :class="getStatusClass(report.status)">
                    {{ getStatusText(report.status) }}
                  </span>
                </td>
                <td class="muted"><small>{{ formatDate(report.createdAt) }}</small></td>
                <td>
                  <div class="row-actions">
                    <!-- ✅ THÊM ADMIN NOTE INPUT -->
                    <div v-if="report.status === 'PENDING'" class="admin-note-section">
                      <textarea 
                        v-model="adminNotes[report.id]"
                        placeholder="Ghi chú của admin (tùy chọn)"
                        class="admin-note-input"
                        rows="2"
                      ></textarea>
                    </div>
                    
                    <button
                      v-if="report.status === 'PENDING'"
                      class="chip primary"
                      @click="updateReportStatus(report.id, 'RESOLVED')"
                      title="Đánh dấu đã xử lý"
                    >
                      <i class="bi bi-check"></i> Duyệt
                    </button>
                    <button
                      v-if="report.status === 'PENDING'"
                      class="chip warning"
                      @click="updateReportStatus(report.id, 'REJECTED')"
                      title="Từ chối báo cáo"
                    >
                      <i class="bi bi-x"></i> Từ chối
                    </button>
                    
                    <button
                      class="chip danger"
                      @click="deleteReport(report.id)"
                      title="Xóa báo cáo"
                    >
                      <i class="bi bi-trash"></i> Xóa
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div v-if="totalPages > 1" class="pagination-bar">
        <button class="pg-btn" :disabled="currentPage === 0" @click="goToPage(currentPage - 1)"><i class="bi bi-chevron-left"></i></button>
        <span>Trang {{ currentPage + 1 }} / {{ totalPages }}</span>
        <button class="pg-btn" :disabled="currentPage === totalPages - 1" @click="goToPage(currentPage + 1)"><i class="bi bi-chevron-right"></i></button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-reports { padding: 24px; color: var(--text-primary); }
.page-header { display:flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.title-card { background: var(--bg-primary); border:1px solid var(--border-color); border-radius: 14px; padding: 10px 14px; box-shadow: 0 6px 18px var(--shadow-color); }
.page-title { display:flex; align-items:center; gap:12px; }
.icon-badge { width:44px; height:44px; display:flex; align-items:center; justify-content:center; border-radius:12px; }
.icon-badge i { font-size:22px; color: var(--info-color); }
.title-text h1 { font-size:22px; margin:0; }
.title-text h1::after { content:''; display:block; height:3px; width:80px; margin-top:6px; border-radius:999px; background:linear-gradient(90deg,#667eea 0%, #764ba2 100%); opacity:.6; }
.title-text p { margin:2px 0 0 0; font-size:13px; color: var(--text-secondary); }
.page-actions { display:flex; align-items:center; gap:10px; }
.btn.btn-outline { background: var(--bg-primary); color: var(--text-primary); border:1px solid var(--border-color); border-radius:10px; padding:8px 12px; }

.panel { background: var(--bg-primary); border:1px solid var(--border-color); border-radius: 16px; box-shadow: 0 8px 22px var(--shadow-color); overflow:hidden; }
.panel-body.no-padding { padding: 0; }
.table-wrap { width: 100%; overflow-x: auto; }
.modern-table { width: 100%; border-collapse: collapse; }
.modern-table thead th { text-align:left; padding: 14px 16px; background: var(--card-header-bg); color: var(--card-header-text); font-weight: 700; font-size: 13px; }
.modern-table tbody td { padding: 12px 16px; border-top:1px solid var(--border-color); font-size: 14px; vertical-align: middle; }
.modern-table tbody tr:hover { background: rgba(102,126,234,0.06); }
.muted { color: var(--text-muted); }
.row-actions { display:flex; gap:8px; }
.chip { display:inline-flex; align-items:center; gap:6px; padding:6px 10px; border-radius:10px; border:1px solid var(--border-color); background: var(--bg-primary); color: var(--text-primary); cursor:pointer; }
.chip.primary { border-color:#93c5fd; }
.chip.danger { border-color:#fda4af; }
.chip.warning { border-color:#fbbf24; }

.pagination-bar { display:flex; align-items:center; justify-content:flex-end; gap:8px; padding: 10px 12px; border-top:1px solid var(--border-color); }
.pg-btn { border:1px solid var(--border-color); background: var(--bg-primary); color: var(--text-primary); border-radius:8px; width:32px; height:28px; display:flex; align-items:center; justify-content:center; }

/* ✅ CSS CHO ADMIN NOTE */
.admin-note-section { margin-bottom: 8px; }
.admin-note-input { 
  width: 100%; 
  padding: 6px 8px; 
  border: 1px solid var(--border-color); 
  border-radius: 6px; 
  background: var(--bg-primary); 
  color: var(--text-primary); 
  font-size: 12px; 
  resize: vertical; 
}

/* Dark/Light adjustments */
.admin-reports.is-light .title-card { background:#ffffff; border-color: rgba(2,6,23,0.12); }
.admin-reports.is-dark .title-card { background: transparent !important; border-color: rgba(255,255,255,0.18); box-shadow: none !important; }
.admin-reports.is-dark .page-title { background: transparent; }
.admin-reports.is-light .title-text h1 { color:#0b1220; }
.admin-reports.is-dark .title-text h1 { color:#f1f5f9; text-shadow: none; }
.admin-reports.is-light .title-text h1::after { opacity:.95; background:linear-gradient(90deg,#4338ca 0%, #7c3aed 100%); }
.admin-reports.is-dark .title-text h1::after { opacity:.7; }

.text-wrap { word-break: break-word; }
</style>