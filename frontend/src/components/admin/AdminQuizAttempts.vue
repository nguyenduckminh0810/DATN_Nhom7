<template>
  <div class="admin-quiz-attempts">
    <div class="page-header">
      <h1>Lịch sử làm quiz (Tất cả người dùng)</h1>
      <p>Xem lịch sử làm quiz của tất cả người dùng, có thể lọc theo user hoặc quiz.</p>
    </div>
    <QuizHistoryTable 
      title="Lịch sử làm quiz - Admin"
      :showUserFilter="true"
    />
  </div>
</template>

<script setup>
import QuizHistoryTable from '@/components/QuizHistoryTable.vue'
</script>

<style scoped>
.admin-quiz-attempts {
  padding: 20px;
}
.page-header {
  margin-bottom: 30px;
}
.page-header h1 {
  color: #333;
  margin-bottom: 10px;
}
.page-header p {
  color: #666;
  margin: 0;
}
</style> 