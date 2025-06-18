<script setup>
import { useLogin } from './useLogin'

const {
    status,
    username,
    password,
    message,
    redirectToServer,
    logout
} = useLogin()
</script>

<template>
    <div class="container" v-if="status === 'loggedOut'">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <h1 class="text-center mb-4">Login</h1>
                <form @submit.prevent="redirectToServer">
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
            </div>
        </div>
    </div>

    <div class="alert alert-primary" role="alert" v-else-if="status === 'loggingIn'">
        Đang đăng nhập...
    </div>

    <div class="alert alert-success" role="alert" v-else>
        Chào mừng bạn <strong>{{ username }}</strong>!
        <br />
        <h1>{{ message }}</h1>
        <button class="btn btn-danger mt-3" @click="logout">Đăng xuất</button>
    </div>
</template>