<template>
  <div class="user-quiz-history">
    <div class="page-header">
      <h1>Lịch sử làm quiz của tôi</h1>
      <p>Xem lại các lần làm quiz và kết quả của bạn</p>
    </div>
    
    <QuizHistoryTable 
      title="Lịch sử làm quiz cá nhân"
      :showUserFilter="false"
    />
  </div>
</template>

<script setup>
import QuizHistoryTable from '@/components/QuizHistoryTable.vue'
</script>

<style scoped>
.user-quiz-history {
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