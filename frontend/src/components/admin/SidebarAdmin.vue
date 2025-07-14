<template>
  <nav class="bg-white border-end min-h-screen w-64">

    <ul class="nav flex-column py-2">
      <li
        v-for="(item, index) in menuItems"
        :key="index"
        class="nav-item"
      >
        <!-- Mục không có submenu -->
        <a
          v-if="!item.children"
          :href="item.link"
          class="d-flex align-items-center px-4 py-2 text-dark sidebar-link"
        >
          <i :class="item.icon + ' me-2'"></i>
          <span>{{ item.label }}</span>
        </a>

        <!-- Mục có submenu -->
        <div v-else>
          <a
            href="#"
            class="d-flex align-items-center justify-content-between px-4 py-2 text-dark sidebar-link"
            @click.prevent="toggle(index)"
          >
            <div class="d-flex align-items-center">
              <i :class="item.icon + ' me-2'"></i>
              <span>{{ item.label }}</span>
            </div>
            <i
              class="bi"
              :class="openedIndex === index ? 'bi-chevron-down' : 'bi-chevron-right'"
            ></i>
          </a>

          <!-- Submenu -->
          <ul class="nav flex-column ms-4" v-show="openedIndex === index">
            <li
              v-for="(child, i) in item.children"
              :key="i"
              class="nav-item"
            >
              <a :href="child.link" class="sidebar-sublink">
                {{ child.label }}
              </a>
            </li>
          </ul>
        </div>
      </li>
    </ul>
  </nav>
</template>

<script setup>
import { ref } from 'vue'

const openedIndex = ref(null)

const toggle = (index) => {
  openedIndex.value = openedIndex.value === index ? null : index
}

const menuItems = [
  { label: 'Dashboard', icon: 'bi bi-speedometer2', link: '/admin/dashboard' },
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
