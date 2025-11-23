<template>
  <div style="height: 280px;">
    <Line :data="chartData" :options="chartOptions" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Line } from 'vue-chartjs';
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  LineElement,
  PointElement,
  CategoryScale,
  LinearScale
} from 'chart.js';

// Đăng ký các thành phần cho Line chart
ChartJS.register(Title, Tooltip, Legend, LineElement, PointElement, CategoryScale, LinearScale);

const props = defineProps({
  stats: Object
});

// Dữ liệu biểu đồ (reactive theo props)
const chartData = computed(() => ({
  labels: [
    'Người dùng',
    'Quiz đã tạo',
    'Lượt làm Quiz',
    'Danh mục',
    'Báo cáo vi phạm'
  ],
  datasets: [
    {
      label: 'Thống kê hệ thống',
      data: [
        props.stats?.totalUsers ?? 0,
        props.stats?.totalQuizzes ?? 0,
        props.stats?.totalAttempts ?? 0,
        props.stats?.totalCategories ?? 0,
        props.stats?.totalReports ?? 0
      ],
      borderColor: '#2563eb',
      backgroundColor: 'rgba(37, 99, 235, 0.15)',
      borderWidth: 2,
      tension: 0.35,
      fill: true,
      pointRadius: 3,
      pointHoverRadius: 5,
      pointBackgroundColor: '#2563eb'
    }
  ]
}))

// Tuỳ chọn hiển thị
// Helpers for theme-aware styling
const getVar = (name, fallback) => {
  const v = getComputedStyle(document.documentElement).getPropertyValue(name).trim()
  return v || fallback
}
const isDark = () => document.documentElement.getAttribute('data-theme') === 'dark'

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: function(context) {
          return `${context.label}: ${context.raw}`;
        }
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        precision: 0,
        color: () => (isDark() ? '#ffffff' : '#374151')
      },
      grid: {
        color: () => (isDark() ? 'rgba(255, 255, 255, 0.12)' : 'rgba(0, 0, 0, 0.08)'),
        drawBorder: true,
        borderColor: () => (isDark() ? 'rgba(255, 255, 255, 0.25)' : 'rgba(0, 0, 0, 0.25)')
      }
    },
    x: {
      ticks: {
        color: () => (isDark() ? '#ffffff' : '#374151')
      },
      grid: {
        color: () => (isDark() ? 'rgba(255, 255, 255, 0.12)' : 'rgba(0, 0, 0, 0.08)'),
        drawBorder: true,
        borderColor: () => (isDark() ? 'rgba(255, 255, 255, 0.25)' : 'rgba(0, 0, 0, 0.25)')
      }
    }
  }
};
</script>
