<template>
    <div class="min-vh-100 d-flex justify-content-center align-items-center gradient-bg text-white"
        style="padding-top: 80px; padding-bottom: 60px">
        <div class="text-center p-5 bg-dark bg-opacity-50 rounded-4 shadow-lg">
            <h1 class="mb-4 fw-bold display-5">Chào mừng đến với Hệ thống Quiz</h1>
            <p class="mb-4 fs-5">
                Khám phá và tham gia các bài kiểm tra thú vị để nâng cao kiến thức của bạn!
            </p>

            <div class="d-flex justify-content-center gap-3">
                <RouterLink to="/login" class="btn btn-outline-light fw-semibold">
                    🔐 Đăng nhập
                </RouterLink>
            </div>
        </div>
    </div>
</template>

<script setup></script>

<style scoped>
.gradient-bg {
    background: linear-gradient(135deg, #007bff, #6f42c1, #e83e8c);
}
</style>
