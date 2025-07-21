<template>
  <div style="height: 280px;">
    <Line :data="chartData" :options="chartOptions" />
  </div>
</template>

<script setup>
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

// Dữ liệu biểu đồ
const chartData = {
  labels: [
    'Người dùng',
    'Quiz đã tạo',
    'Lượt làm Quiz',
    'Danh mục',
    'Quiz chờ duyệt',
    'Báo cáo vi phạm'
  ],
  datasets: [
    {
      label: 'Thống kê hệ thống',
      data: [
        props.stats.totalUsers,
        props.stats.totalQuizzes,
        props.stats.totalAttempts,
        props.stats.totalCategories,
        props.stats.pendingApproval,
        props.stats.totalReports
      ],
      borderColor: '#42A5F5',
      backgroundColor: 'rgba(66, 165, 245, 0.2)',
      tension: 0.3,
      fill: true,
      pointBackgroundColor: '#007bff'
    }
  ]
};

// Tuỳ chọn hiển thị
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
        precision: 0
      }
    }
  }
};
</script>
