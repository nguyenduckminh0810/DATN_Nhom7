<script setup>
import { RouterView } from 'vue-router'
import Navbar from '@/components/client/Navbar.vue'
import Footer from '@/components/client/Footer.vue';
import { useLogin } from '@/components/client/useLogin'
useLogin()
</script>

<template>
    <div class="app-layout">
        <Navbar />
        <div class="content-container">
            <RouterView />
        </div>
        <Footer />
    </div>
</template>

<style scoped>
.app-layout {
    min-height: 100vh;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    flex-direction: column;
}


.content-container {
    padding-top: 100px;
    flex: 1;
}


@media (max-width: 991px) {
    .content-container {
        padding-top: 120px;
        padding-bottom: 50px;
    }
}
</style>
