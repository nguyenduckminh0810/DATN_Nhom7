<template>
  <div>
    <h2>Danh mục quiz</h2>
    <ul>
      <li v-for="cat in categories" :key="cat.id">
        {{ cat.name }} - {{ cat.description }}
      </li>
    </ul>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { categoryService } from '@/services/categoryService'
const categories = ref([])
onMounted(async () => {
  const res = await categoryService.getAll()
  categories.value = res.data.content
})
</script> 