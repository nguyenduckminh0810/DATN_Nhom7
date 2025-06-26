<script setup>
import { useLogin } from './useLogin'

const {
    status,
    username,
    message,
    password,
    login,
    logout
} = useLogin()

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
                    Don't have an account? <a href="/register">Register here</a>
                </p>
            </div>
        </div>
    </div>
    <!-- Đoạn này chuyển tab khác  -->

    <div class="alert alert-success" role="alert" v-else>
        Chào mừng bạn <strong>{{ username }}</strong>!
        <br />
        <h1>{{ message }}</h1>
        <button class="btn btn-danger mt-3" @click="logout">Đăng xuất</button>
    </div>
</template>