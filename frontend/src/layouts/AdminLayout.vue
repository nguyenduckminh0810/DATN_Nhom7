<template>
  <div class="d-flex flex-column min-vh-100 bg-light">
    <!-- Header -->
    <HeaderAdmin />
    <!-- Body: Sidebar + Content -->
    <div class="d-flex flex-grow-1">
      <SidebarAdmin />
      <main class="flex-grow-1 p-4 overflow-auto">
        <router-view />
      </main>
    </div>

    <!-- Footer -->
    <FooterAdmin />
  </div>
</template>

<script setup>
import FooterAdmin from '@/components/admin/FooterAdmin.vue'
import HeaderAdmin from '@/components/admin/HeaderAdmin.vue'
import SidebarAdmin from '@/components/admin/SidebarAdmin.vue'
</script>
