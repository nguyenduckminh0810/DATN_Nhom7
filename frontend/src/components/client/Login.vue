<script setup>
import { RouterLink, useRouter } from 'vue-router'
import { useLogin } from './useLogin'
import { useQuizCRUD } from './useQuizCRUD'
import { watch } from 'vue'

const router = useRouter() // ✅ Lấy router
const {
    status,
    username,
    message,
    password,
    userId,
    getUserId,
    login,
    logout
} = useLogin()
//cho sang client dashboard
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
watch(status, (message) => {
    if (message === 'loggedIn') {
        getUserId().then(id => {
            if (id) router.push({ name: 'ClientDashboard', params: { userId: id } })
            else alert("Không thể lấy thông tin người dùng.")
        })
    }
})

</script>

<template>
    <div class="container">
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
</template>
