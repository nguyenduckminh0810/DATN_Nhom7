<script setup>
import { RouterView } from 'vue-router'
import Navbar from '@/components/client/Navbar.vue'
import Footer from '@/components/client/Footer.vue';
</script>

<template>
    <div class="flex flex-col min-h-screen bg-gray-50">
         <Navbar />
    <div> <!-- Tùy theo chiều cao Navbar thực tế -->
        <RouterView />
    </div>
        <Footer/>
    </div>
   
</template>

<style scoped></style>
