<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'


const reports = ref([])
const loading = ref(false)
const error = ref('')
const currentPage = ref(0)
const totalPages = ref(1)
const selectedStatus = ref('ALL')
const pageSize = 10


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
    const response = await axios.put(`http://localhost:8080/api/reports/${reportId}/status`, {
      status: newStatus
    })
    
    console.log('✅ Updated status:', response.data)
    
 
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
</script>

<template>
  <div class="container-fluid">
    
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="mb-0">
        <i class="bi bi-exclamation-triangle text-warning me-2"></i>
        Quản lý Báo cáo
      </h2>
      <button class="btn btn-outline-primary" @click="fetchReports(currentPage)">
        <i class="bi bi-arrow-clockwise me-1"></i>
        Làm mới
      </button>
    </div>

    
    <div class="card mb-4">
      <div class="card-body">
        <h6 class="card-title">Lọc theo trạng thái:</h6>
        <div class="btn-group" role="group">
          <button
            v-for="option in statusOptions"
            :key="option.value"
            type="button"
            :class="['btn', option.class, { 'active': selectedStatus === option.value }]"
            @click="filterByStatus(option.value)"
          >
            {{ option.label }}
          </button>
        </div>
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

    
    <div v-else class="card">
      <div class="card-header">
        <h5 class="mb-0">
          Danh sách báo cáo 
          <span class="badge bg-primary ms-2">{{ reports.length }} báo cáo</span>
        </h5>
      </div>
      
      <div class="card-body p-0">
        <div v-if="reports.length === 0" class="text-center py-5">
          <i class="bi bi-inbox display-1 text-muted"></i>
          <p class="text-muted mt-3">Không có báo cáo nào</p>
        </div>
        
        <div v-else class="table-responsive">
          <table class="table table-hover mb-0">
            <thead class="table-light">
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
                <td>
                  <strong>#{{ report.id }}</strong>
                </td>
                <td>
                  <div>
                    <strong>{{ report.userFullName || 'N/A' }}</strong>
                    <br>
                    <small class="text-muted">{{ report.userEmail || 'N/A' }}</small>
                  </div>
                </td>
                <td>
                  <div>
                    <strong>{{ report.quizTitle || 'N/A' }}</strong>
                    <br>
                    <small class="text-muted">ID: {{ report.quizId }}</small>
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
                <td>
                  <small>{{ formatDate(report.createdAt) }}</small>
                </td>
                <td>
                  <div class="btn-group btn-group-sm">
                    
                    <button
                      v-if="report.status === 'PENDING'"
                      class="btn btn-success btn-sm"
                      @click="updateReportStatus(report.id, 'RESOLVED')"
                      title="Đánh dấu đã xử lý"
                    >
                      <i class="bi bi-check"></i>
                    </button>
                    <button
                      v-if="report.status === 'PENDING'"
                      class="btn btn-danger btn-sm"
                      @click="updateReportStatus(report.id, 'REJECTED')"
                      title="Từ chối báo cáo"
                    >
                      <i class="bi bi-x"></i>
                    </button>
                    
                    
                    <button
                      class="btn btn-outline-danger btn-sm"
                      @click="deleteReport(report.id)"
                      title="Xóa báo cáo"
                    >
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      
      
      <div v-if="totalPages > 1" class="card-footer">
        <nav>
          <ul class="pagination pagination-sm justify-content-center mb-0">
            <li class="page-item" :class="{ disabled: currentPage === 0 }">
              <button class="page-link" @click="goToPage(currentPage - 1)">
                <i class="bi bi-chevron-left"></i>
              </button>
            </li>
            
            <li class="page-item active">
              <span class="page-link">
                {{ currentPage + 1 }} / {{ totalPages }}
              </span>
            </li>
            
            <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
              <button class="page-link" @click="goToPage(currentPage + 1)">
                <i class="bi bi-chevron-right"></i>
              </button>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>

<style scoped>
.btn-group .btn.active {
  transform: scale(1.05);
}

.table td {
  vertical-align: middle;
}

.text-wrap {
  word-break: break-word;
}
</style>