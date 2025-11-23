<template>
  <div class="admin-quiz-history">
    <div class="page-header">
      <h1>Quản lý lịch sử làm quiz</h1>
      <p>Xem lịch sử làm quiz của tất cả người dùng</p>
    </div>
    
    <div class="test-content">
      <h3>Test: Component đang hoạt động</h3>
      <p>Nếu bạn thấy dòng này, component đã load thành công.</p>
      <p>Thời gian hiện tại: {{ currentTime }}</p>
    </div>
    
    <!-- Tạm thời comment để test -->
    <!--
    <QuizHistoryTable 
      title="Lịch sử làm quiz - Quản trị"
      :showUserFilter="true"
    />
    -->
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
// import QuizHistoryTable from '@/components/QuizHistoryTable.vue'

const currentTime = ref('')

onMounted(() => {
  currentTime.value = new Date().toLocaleString('vi-VN')
})
</script>

<style scoped>
.admin-quiz-history {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  color: var(--text-primary);
  margin-bottom: 10px;
}

.page-header p {
  color: var(--text-secondary);
  margin: 0;
}
</style> 