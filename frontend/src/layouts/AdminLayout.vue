<template>
  <div class="flex flex-col min-h-screen bg-gray-50">
    <HeaderAdmin />
    <main class="flex-1 p-6">
      <router-view />
    </main>
    <FooterAdmin />
  </div>
</template>

<script setup>
import FooterAdmin from '@/components/admin/FooterAdmin.vue';
import HeaderAdmin from '@/components/admin/HeaderAdmin.vue';

console.log("AdminLayout mounted")
</script>
