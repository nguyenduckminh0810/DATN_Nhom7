<template>
  <nav class="bg-white border-end min-h-screen w-64">
    <ul class="nav flex-column py-2">
      <li
        v-for="(item, index) in menuItems"
        :key="index"
        class="nav-item"
      >
        <a
          :href="item.link"
          class="d-flex align-items-center px-4 py-2 text-dark sidebar-link"
        >
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
  // Quiz Attempts chỉ hiển thị nếu là admin
  ...(userStore.isAdmin() ? [
    { label: 'Quiz Attempts', icon: 'bi bi-clock-history', link: '/admin/attempts' },
  ] : []),
  { label: 'Quiz History', icon: 'bi bi-clock-history', link: '/my-history' },
  { label: 'Category Manager', icon: 'bi bi-folder', link: '/admin/categories' },
  {
    label: 'UI Elements',
    icon: 'bi bi-ui-checks-grid',
    children: [
      { label: 'Buttons', link: '/admin/ui/buttons' },
      { label: 'Dropdowns', link: '/admin/ui/dropdowns' },
      { label: 'Typography', link: '/admin/ui/typography' },
    ],
  },
  {
    label: 'Charts',
    icon: 'bi bi-bar-chart-line',
    children: [{ label: 'ChartJs', link: '/admin/charts/chartjs' }],
  },
  {
    label: 'Tables',
    icon: 'bi bi-table',
    children: [{ label: 'Basic Table', link: '/admin/tables/basic' }],
  },
  {
    label: 'Icons',
    icon: 'bi bi-stars',
    children: [{ label: 'Bootstrap Icons', link: '/admin/icons/bootstrap' }],
  },
  {
    label: 'User Pages',
    icon: 'bi bi-person-circle',
    children: [
      { label: 'Login', link: '/admin/login' },
      { label: 'Register', link: '/admin/register' },
    ],
  },
  {
    label: 'Error Pages',
    icon: 'bi bi-exclamation-triangle',
    children: [
      { label: '404', link: '/admin/error/404' },
      { label: '500', link: '/admin/error/500' },
    ],
  },
  {
    label: 'Documentation',
    icon: 'bi bi-journal-text',
    link: '/admin/documentation',
  },

const menuItems = [
  { label: 'Dashboard', icon: 'bi bi-speedometer2', link: '/admin/dashboard' },
  { label: 'Users', icon: 'bi bi-person-circle', link: '/admin/all-users' },
  { label: 'Quizzes', icon: 'bi bi-bar-chart-line', link: '/admin/all-quizzes' },
  { label: 'Quiz Attempts', icon: 'bi bi-table', link: '/admin/quiz-attempts' },
  { label: 'Categories', icon: 'bi bi-stars', link: '/admin/categories' },
  { label: 'Quiz Approval', icon: 'bi bi-ui-checks-grid', link: '/admin/quiz-approval' },
  { label: 'Reports', icon: 'bi bi-exclamation-triangle', link: '/admin/reports' },
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
