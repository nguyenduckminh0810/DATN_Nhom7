<script setup>
import { RouterView } from 'vue-router'
import Navbar from './components/client/Navbar.vue'
import Footer from './components/client/Footer.vue'
</script>

<template>
  <Navbar />
  <div style="margin-top: 150px; margin-bottom: 100px;"> <!-- Tùy theo chiều cao Navbar thực tế -->
    <RouterView />
  </div>
  <Footer />
</template>

<style scoped></style>
