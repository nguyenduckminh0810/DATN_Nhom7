<script setup>
import { RouterView } from 'vue-router'
import Navbar from '@/components/client/Navbar.vue'
</script>

<template>
    <Navbar />
    <div> <!-- Tùy theo chiều cao Navbar thực tế -->
        <RouterView />
    </div>
</template>

<style scoped></style>
