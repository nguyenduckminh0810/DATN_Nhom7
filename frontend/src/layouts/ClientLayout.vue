<script setup>
import { RouterView } from 'vue-router'
import Navbar from '@/components/client/Navbar.vue'
import Footer from '@/components/client/Footer.vue';
</script>

<template>
    <div>
        <Navbar />
        <div>
            <RouterView />
        </div>
        <Footer />
    </div>

</template>
<style scoped></style>
