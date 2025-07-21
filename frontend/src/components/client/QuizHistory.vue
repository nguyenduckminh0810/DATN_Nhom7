<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const userId = route.params.userId

const history = ref([])
const loading = ref(true)
const error = ref(null)

onMounted(async () => {
    try {
        const res = await axios.get(`http://localhost:8080/api/result/user/${userId}`)
        history.value = res.data
    } catch (err) {
        error.value = err.response?.data?.message || 'Lỗi khi tải lịch sử làm bài.'
    } finally {
        loading.value = false
    }
})
</script>

<template>
    <div class="container mt-5">
        <div class="card shadow-sm border-0 rounded-4">
            <div class="card-header bg-primary text-white rounded-top-4">
                <h4 class="mb-0">📜 Lịch sử làm bài</h4>
            </div>
            <div class="card-body">

                <div v-if="loading" class="alert alert-info d-flex align-items-center">
                    <div class="spinner-border spinner-border-sm me-2" role="status"></div>
                    <div>Đang tải dữ liệu...</div>
                </div>

                <div v-else-if="error" class="alert alert-danger">
                    {{ error }}
                </div>

                <div v-else>
                    <table class="table table-bordered table-hover align-middle text-center">
                        <thead class="table-light">
                            <tr>
                                <th>QuizID</th>
                                <th>Tên Quiz</th>
                                <th>Điểm số</th>
                                <th>Hoàn thành lúc</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(item, index) in history" :key="item.id">
                                <td>{{ item.id }}</td>
                                <td class="text-start">{{ item.quizTitle }}</td>
                                <td><span class="badge bg-success fs-6">{{ item.score }}</span></td>
                                <td>{{ new Date(item.completedAt).toLocaleString() }}</td>
                            </tr>
                        </tbody>
                    </table>

                    <div v-if="history.length === 0" class="text-center text-muted">
                        Không có lịch sử làm bài nào.
                    </div>
                </div>

            </div>
        </div>
    </div>
</template>
