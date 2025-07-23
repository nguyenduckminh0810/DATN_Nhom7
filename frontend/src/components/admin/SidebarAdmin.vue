<template>
  <nav class="bg-white border-end min-h-screen w-64">
    <ul class="nav flex-column py-2">
      <li v-for="(item, index) in menuItems" :key="index" class="nav-item">
        <a :href="item.link" class="d-flex align-items-center px-4 py-2 text-dark sidebar-link">
          <i :class="item.icon + ' me-2'"></i>
          <span>{{ item.label }}</span>
        </a>
      </li>
    </ul>
  </nav>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

const openedIndex = ref(null)
const userStore = useUserStore()

const toggle = (index) => {
  openedIndex.value = openedIndex.value === index ? null : index
}

const menuItems = [
  { label: 'Dashboard', icon: 'bi bi-speedometer2', link: '/admin/dashboard' },
  { label: 'Users', icon: 'bi bi-person-circle', link: '/admin/all-users' },
  { label: 'Quizzes', icon: 'bi bi-bar-chart-line', link: '/admin/all-quizzes' },
  // { label: 'Quiz Attempts', icon: 'bi bi-table', link: '/admin/quiz-attempts' },
  // { label: 'Quiz Approval', icon: 'bi bi-ui-checks-grid', link: '/admin/quiz-approval' },
  { label: 'Reports', icon: 'bi bi-exclamation-triangle', link: '/admin/reports' },
  { label: 'Categories', icon: 'bi bi-stars', link: '/admin/categories' },
]
</script>

<style scoped>
.sidebar-link {
  border-radius: 0.375rem;
  transition: all 0.2s ease;
}

.sidebar-link:hover {
  background-color: #e9ecef;
  color: #0d6efd;
  text-decoration: none;
}

.sidebar-sublink {
  display: block;
  padding: 0.5rem 1rem;
  color: #495057;
  font-size: 0.95rem;
  border-radius: 0.375rem;
  transition: background-color 0.2s;
}

.sidebar-sublink:hover {
  background-color: #f1f3f5;
  color: #0d6efd;
  text-decoration: none;
}
</style>
