<script setup>
import { useRegin } from './useRegin';
import { useLogin } from './useLogin';
const { logout } = useLogin();
const { status, username, email, password, confirmPassword, fullName, avatarUrl, bio, message, isLoading, register } = useRegin();
</script>
<template>
    <div class="content-container">
        <div class="container" v-if="status === 'loggedOut'">
            <h1>Đăng ký</h1>
            <form id="registerForm" class="container mt-4" method="post" @submit.prevent="register">
                <!-- Tên đăng nhập -->
                <div class="mb-3">
                    <label for="username" class="form-label">Tên đăng nhập</label>
                    <input type="text" class="form-control" v-model="username" required maxlength="50">
                </div>
                <!-- Email -->
                <div class="mb-3">
                    <label for="email" class="form-label">Email</label>
                    <input type="email" class="form-control" v-model="email" id="email" name="email" required
                        maxlength="100">
                </div>
                <!-- Mật khẩu -->
                <div class="mb-3">
                    <label for="password" class="form-label">Mật khẩu</label>
                    <input type="password" class="form-control" v-model="password" id="password" name="password"
                        required maxlength="255">
                </div>

                <!-- Mật khẩu -->
                <div class="mb-3">
                    <label for="password" class="form-label">Nhập lại mật khẩu</label>
                    <input type="password" class="form-control" v-model="confirmPassword" id="confirmPassword"
                        name="password" required maxlength="255">
                </div>
                <!-- Họ và tên -->
                <div class="mb-3">
                    <label for="fullName" class="form-label">Họ và tên</label>
                    <input type="text" class="form-control" v-model="fullName" id="fullName" name="fullName"
                        maxlength="100">
                </div>
                <!-- Ảnh đại diện -->
                <div class="mb-3">
                    <label for="avatarUrl" class="form-label">Ảnh đại diện (URL)</label>
                    <input type="url" class="form-control" v-model="avatarUrl" name="avatarUrl" maxlength="255">
                </div>
                <!-- Giới thiệu -->
                <div class="mb-3">
                    <label for="bio" class="form-label">Giới thiệu</label>
                    <textarea class="form-control" v-model="bio" id="bio" name="bio" rows="3"></textarea>
                </div>

                <button type="submit" class="btn btn-primary">Đăng ký</button>
            </form>
        </div>
        <!-- Đoạn này chuyển tab khác  -->

        <div class="alert alert-success" role="alert" v-else-if="status === 'EMAIL_EXISTS'">
            <h1>{{ message }}</h1>
            <button class="btn btn-danger mt-3" @click="logout">Đăng ký</button>
        </div>
        <div class="alert alert-success" role="alert" v-else-if="status === 'USERNAME_EXISTS'">
            <h1>{{ message }}</h1>
            <button class="btn btn-danger mt-3" @click="logout">Đăng ký</button>
        </div>
    </div>
</template>