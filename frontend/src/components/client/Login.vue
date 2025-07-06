<script setup>
import { RouterLink, useRouter } from 'vue-router'
import { useLogin } from './useLogin'
import { useQuizCRUD } from './useQuizCRUD'

const router = useRouter() // ✅ Lấy router
const {
    status,
    username,
    message,
    password,
    userId,
    login,
    logout
} = useLogin()
const { toQuizCRUD } = useQuizCRUD()

function toQuizHistory() {
    if (!userId.value) {
        console.warn("Chưa có userId. Đang thử gọi getUserId() lại...")
        getUserId().then(id => {
            if (id) router.push({ name: 'quizHistory', params: { userId: id } })
            else alert("Không thể lấy thông tin người dùng.")
        })
    } else {
        router.push({ name: 'quizHistory', params: { userId: userId.value } })
    }
}

</script>

<template>
    <div class="container" v-if="status === 'loggedOut'">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <h1 class="text-center mb-4">Login</h1>
                <form @submit.prevent="login" class="bg-light p-4 rounded shadow">
                    <div class="mb-3">
                        <label for="username" class="form-label">Username</label>
                        <input type="text" id="username" v-model="username" class="form-control" required />
                    </div>
                    <div class="mb-3">
                        <label for="password" class="form-label">Password</label>
                        <input type="password" id="password" v-model="password" class="form-control" required />
                    </div>
                    <h1>{{ message }}</h1>
                    <button type="submit" class="btn btn-primary w-100">Login</button>
                </form>
                <p class="mt-3 text-center">
                    Don't have an account? <RouterLink to="/register">Register here</RouterLink>
                </p>
            </div>
        </div>
    </div>

    <!-- Logged in view -->
    <div class="alert alert-success" role="alert" v-else-if="status === 'loggedIn'">
        Chào mừng bạn <strong>{{ username }}</strong>!
        <br />
        <h1>{{ message }}</h1>
        <button class="btn btn-danger mt-3 me-3" @click="logout">Đăng xuất</button>
        <button class="btn btn-primary mt-3 me-3" @click="toQuizCRUD">Quiz CRUD</button>
        <button class="btn btn-secondary mt-3 me-3" @click="toQuizHistory">Lịch sử làm</button>
    </div>
</template>
