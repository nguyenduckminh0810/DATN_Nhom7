<template>
  <div>
    <h2>Tìm kiếm người dùng</h2>
    <form @submit.prevent="searchUsers">
      <div>
        <input v-model="name" placeholder="Nhập tên" />
        <input v-model="email" placeholder="Nhập email" />
        <select v-model="status">
          <option value="">-- Tất cả vai trò --</option>
          <option value="USER">USER</option>
          <option value="ADMIN">ADMIN</option>
        </select>
        <button type="submit">Tìm kiếm</button>
      </div>
    </form>

    <ul>
      <li v-for="user in users" :key="user.id">
        {{ user.name }} | {{ user.email }} | {{ user.status }}
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

// Các biến để binding
const name = ref('')
const email = ref('')
const status = ref('')
const users = ref([])

// Hàm gọi API backend
const searchUsers = async () => {
  try {
    const response = await axios.get('/api/users', {
      params: {
        name: name.value || null,
        email: email.value || null,
        status: status.value || null,
      },
    })
    users.value = response.data
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
input,
select {
  margin: 0 8px 8px 0;
}
button {
  margin-top: 8px;
}
</style>
