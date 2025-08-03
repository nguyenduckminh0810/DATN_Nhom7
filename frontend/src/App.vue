<script setup>
import { RouterView } from 'vue-router'
import { useLogin } from '@/components/client/useLogin'
useLogin()
</script>

<template>
  <div>
    <RouterView />
  </div>
</template>

<style scoped></style>
