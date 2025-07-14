<template>
  <footer class="bg-white border-top py-3 mt-auto text-center text-muted small shadow-sm">
    <div>
      © {{ currentYear }} <span class="fw-bold text-dark">Admin Dashboard</span> — All rights reserved.
    </div>
  </footer>
</template>

<script setup>
const currentYear = new Date().getFullYear()
</script>

<style scoped>
footer {
  font-size: 0.875rem;
}
</style>
