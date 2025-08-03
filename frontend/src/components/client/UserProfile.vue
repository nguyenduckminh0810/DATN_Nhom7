<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useLogin } from './useLogin'
import api from '@/utils/axios'

const router = useRouter()
const route = useRoute()
const { username, userId, getUserId, logout } = useLogin()

// Active tab management
const activeTab = ref('profile')

// User data
const userData = ref({
    id: '',
    username: '',
    email: '',
    fullName: '',
    bio: '',
    avatarUrl: '',
    createdAt: '',
    totalQuizzes: 0,
    totalAttempts: 0,
    averageScore: 0,
    bestScore: 0,
    achievements: []
})

// Profile editing
const isEditing = ref(false)
const editForm = ref({
    fullName: '',
    bio: '',
    avatarUrl: ''
})

// Password change
const passwordForm = ref({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
})
const showCurrentPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

// File upload
const selectedFile = ref(null)
const avatarPreview = ref('')

// Loading states
const isLoading = ref(true)
const isSaving = ref(false)
const isChangingPassword = ref(false)

// Messages
const message = ref('')
const messageType = ref('success') // success, error, info

// Recent activities
const recentActivities = ref([])
const quizHistory = ref([])
const achievements = ref([
    { id: 1, name: 'First Quiz', icon: 'bi-trophy', description: 'Hoàn thành quiz đầu tiên', earned: true, earnedAt: '2024-01-15' },
    { id: 2, name: 'Quiz Master', icon: 'bi-award', description: 'Tạo 10 quiz', earned: true, earnedAt: '2024-02-10' },
    { id: 3, name: 'Perfect Score', icon: 'bi-star-fill', description: 'Đạt điểm tuyệt đối', earned: false },
    { id: 4, name: 'Streak Master', icon: 'bi-lightning', description: 'Làm quiz 7 ngày liên tiếp', earned: false },
    { id: 5, name: 'Social Butterfly', icon: 'bi-people', description: 'Chia sẻ 5 quiz công khai', earned: true, earnedAt: '2024-01-20' },
    { id: 6, name: 'Knowledge Seeker', icon: 'bi-book', description: 'Hoàn thành 50 quiz', earned: false }
])

const earnedAchievements = computed(() => achievements.value.filter(a => a.earned))
const pendingAchievements = computed(() => achievements.value.filter(a => !a.earned))

// Password strength
const passwordStrength = computed(() => {
    if (!passwordForm.value.newPassword) return 0
    let strength = 0
    const pass = passwordForm.value.newPassword

    if (pass.length >= 8) strength++
    if (pass.length >= 12) strength++
    if (/[a-z]/.test(pass)) strength++
    if (/[A-Z]/.test(pass)) strength++
    if (/[0-9]/.test(pass)) strength++
    if (/[^A-Za-z0-9]/.test(pass)) strength++

    return Math.min(strength, 5)
})

const passwordMatch = computed(() => {
    if (!passwordForm.value.confirmPassword) return null
    return passwordForm.value.newPassword === passwordForm.value.confirmPassword
})

const getPasswordStrengthColor = (strength) => {
    switch (strength) {
        case 1: return '#ff4757'
        case 2: return '#ffa502'
        case 3: return '#2ed573'
        case 4: return '#1e90ff'
        case 5: return '#8e44ad'
        default: return '#ddd'
    }
}

const getPasswordStrengthText = (strength) => {
    switch (strength) {
        case 1: return 'Yếu'
        case 2: return 'Trung bình'
        case 3: return 'Mạnh'
        case 4: return 'Rất mạnh'
        case 5: return 'Xuất sắc'
        default: return ''
    }
}

// Methods
onMounted(async () => {
    isLoading.value = true
    try {
        // ✅ Đảm bảo có userId trước khi fetch data
        if (!userId.value) await getUserId()

        await Promise.all([
            fetchUserData(),
            fetchQuizHistory(),
            fetchRecentActivities()
        ])
    } finally {
        isLoading.value = false
    }
})

const fetchUserData = async () => {
    try {
        const token = localStorage.getItem('token')

        // ✅ Lấy thông tin user cơ bản
        const userRes = await api.get('/user/profile', {
            headers: { Authorization: `Bearer ${token}` }
        })

        // ✅ Lấy danh sách quiz đã tạo
        let totalQuizzes = 0
        try {
            if (userId.value) {
                const quizzesRes = await api.get(`/quiz/user/${userId.value}/paginated?page=0&size=1000`, {
                    headers: { Authorization: `Bearer ${token}` }
                })
                totalQuizzes = quizzesRes.data.totalItems || 0
            }
        } catch (error) {
            console.error('Error fetching user quizzes:', error)
        }

        // ✅ Lấy lịch sử làm bài
        let quizAttempts = []
        let totalAttempts = 0
        try {
            if (userId.value) {
                const attemptsRes = await api.get(`/result/user/${userId.value}`)
                quizAttempts = attemptsRes.data || []
                totalAttempts = quizAttempts.length
            }
        } catch (error) {
            console.error('Error fetching quiz attempts:', error)
        }

        userData.value = {
            ...userRes.data,
            totalQuizzes,
            totalAttempts,
            averageScore: calculateAverageScore(quizAttempts),
            bestScore: calculateBestScore(quizAttempts)
        }

        // Update edit form
        editForm.value = {
            fullName: userData.value.fullName,
            bio: userData.value.bio,
            avatarUrl: userData.value.avatarUrl
        }
    } catch (error) {
        showMessage('Không thể tải thông tin người dùng', 'error')
        console.error('Error fetching user data:', error)
    }
}

const fetchQuizHistory = async () => {
    try {
        if (!userId.value) return
        const res = await api.get(`/result/user/${userId.value}`)
        quizHistory.value = res.data.slice(0, 10) // Latest 10 attempts
    } catch (error) {
        console.error('Error fetching quiz history:', error)
    }
}

const fetchRecentActivities = async () => {
    try {
        // Mock recent activities - in real app, this would be an API call
        recentActivities.value = [
            { id: 1, type: 'quiz_created', message: 'Bạn đã tạo quiz "JavaScript Basics"', time: '2 giờ trước', icon: 'bi-plus-circle' },
            { id: 2, type: 'quiz_completed', message: 'Hoàn thành quiz "Vue.js Advanced" với điểm 95', time: '1 ngày trước', icon: 'bi-check-circle' },
            { id: 3, type: 'achievement', message: 'Đạt được thành tựu "Quiz Master"', time: '3 ngày trước', icon: 'bi-award' },
            { id: 4, type: 'profile_updated', message: 'Cập nhật thông tin hồ sơ', time: '1 tuần trước', icon: 'bi-person-gear' }
        ]
    } catch (error) {
        console.error('Error fetching activities:', error)
    }
}

const calculateAverageScore = (attempts) => {
    if (!attempts.length) return 0
    const total = attempts.reduce((sum, attempt) => sum + (attempt.score || 0), 0)
    return Math.round(total / attempts.length)
}

const calculateBestScore = (attempts) => {
    if (!attempts.length) return 0
    return Math.max(...attempts.map(attempt => attempt.score || 0))
}

const handleFileUpload = (event) => {
    const file = event.target.files[0]
    if (file) {
        selectedFile.value = file
        const reader = new FileReader()
        reader.onload = (e) => {
            avatarPreview.value = e.target.result
            editForm.value.avatarUrl = e.target.result
        }
        reader.readAsDataURL(file)
    }
}

const startEditing = () => {
    isEditing.value = true
    editForm.value = {
        fullName: userData.value.fullName,
        bio: userData.value.bio,
        avatarUrl: userData.value.avatarUrl
    }
    avatarPreview.value = userData.value.avatarUrl
}

const cancelEdit = () => {
    isEditing.value = false
    editForm.value = {
        fullName: userData.value.fullName,
        bio: userData.value.bio,
        avatarUrl: userData.value.avatarUrl
    }
    avatarPreview.value = ''
    selectedFile.value = null
}

const saveProfile = async () => {
    isSaving.value = true
    try {
        const token = localStorage.getItem('token')
        const formData = new FormData()

        formData.append('fullName', editForm.value.fullName)
        formData.append('bio', editForm.value.bio)

        if (selectedFile.value) {
            formData.append('avatar', selectedFile.value)
        } else if (editForm.value.avatarUrl !== userData.value.avatarUrl) {
            formData.append('avatarUrl', editForm.value.avatarUrl)
        }

        const res = await api.put('/user/profile', formData, {
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'multipart/form-data'
            }
        })

        userData.value = { ...userData.value, ...res.data }
        isEditing.value = false
        showMessage('Cập nhật hồ sơ thành công!', 'success')
    } catch (error) {
        showMessage('Không thể cập nhật hồ sơ', 'error')
        console.error('Error updating profile:', error)
    } finally {
        isSaving.value = false
    }
}

const changePassword = async () => {
    if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
        showMessage('Mật khẩu xác nhận không khớp', 'error')
        return
    }

    if (passwordStrength.value < 3) {
        showMessage('Mật khẩu cần đạt mức độ mạnh trở lên', 'error')
        return
    }

    isChangingPassword.value = true
    try {
        const token = localStorage.getItem('token')
        await api.put('/user/change-password', {
            currentPassword: passwordForm.value.currentPassword,
            newPassword: passwordForm.value.newPassword
        }, {
            headers: { Authorization: `Bearer ${token}` }
        })

        passwordForm.value = {
            currentPassword: '',
            newPassword: '',
            confirmPassword: ''
        }
        showMessage('Đổi mật khẩu thành công!', 'success')
    } catch (error) {
        showMessage(error.response?.data?.message || 'Không thể đổi mật khẩu', 'error')
    } finally {
        isChangingPassword.value = false
    }
}

const showMessage = (text, type = 'success') => {
    message.value = text
    messageType.value = type
    setTimeout(() => {
        message.value = ''
    }, 5000)
}

const setActiveTab = (tab) => {
    activeTab.value = tab
}

const deleteAccount = async () => {
    if (confirm('Bạn có chắc chắn muốn xóa tài khoản? Hành động này không thể hoàn tác.')) {
        try {
            const token = localStorage.getItem('token')
            await api.delete('/user/account', {
                headers: { Authorization: `Bearer ${token}` }
            })

            logout()
            router.push('/login')
            showMessage('Tài khoản đã được xóa', 'info')
        } catch (error) {
            showMessage('Không thể xóa tài khoản', 'error')
        }
    }
}

const clearAvatar = () => {
    avatarPreview.value = ''
    editForm.value.avatarUrl = ''
    selectedFile.value = null
}

const getScoreClass = (score) => {
    if (score >= 80) return 'excellent'
    if (score >= 60) return 'good'
    return 'average'
}
</script>

<template>
    <div class="user-profile-container">
        <!-- Animated Background -->
        <div class="background-decorations">
            <div class="floating-orb orb-1"></div>
            <div class="floating-orb orb-2"></div>
            <div class="floating-orb orb-3"></div>
            <div class="floating-orb orb-4"></div>
        </div>

        <!-- Loading State -->
        <div v-if="isLoading" class="loading-section">
            <div class="loading-container">
                <div class="modern-spinner">
                    <div class="spinner-ring"></div>
                    <div class="spinner-ring"></div>
                    <div class="spinner-ring"></div>
                </div>
                <h3 class="loading-text">Đang tải hồ sơ...</h3>
                <p class="loading-subtitle">Vui lòng chờ trong giây lát</p>
            </div>
        </div>

        <!-- Main Content -->
        <div v-else class="profile-content">
            <!-- Profile Header -->
            <div class="profile-header">
                <div class="container">
                    <div class="header-content">
                        <div class="user-info">
                            <div class="avatar-section">
                                <img :src="userData.avatarUrl || '/img/default-avatar.png'" alt="User Avatar"
                                    class="user-avatar" />
                                <div class="avatar-status online"></div>
                            </div>
                            <div class="user-details">
                                <h1 class="user-name">{{ userData.fullName || userData.username }}</h1>
                                <p class="user-username">@{{ userData.username }}</p>
                                <p class="user-bio">{{ userData.bio || 'Chưa có mô tả về bản thân' }}</p>
                                <div class="user-meta">
                                    <span class="meta-item">
                                        <i class="bi bi-calendar-plus"></i>
                                        Tham gia {{ new Date(userData.createdAt).toLocaleDateString('vi-VN') }}
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div class="user-stats">
                            <div class="stat-card">
                                <div class="stat-icon quiz-icon">
                                    <i class="bi bi-collection"></i>
                                </div>
                                <div class="stat-info">
                                    <span class="stat-number">{{ userData.totalQuizzes }}</span>
                                    <span class="stat-label">Quiz đã tạo</span>
                                </div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-icon attempt-icon">
                                    <i class="bi bi-play-circle"></i>
                                </div>
                                <div class="stat-info">
                                    <span class="stat-number">{{ userData.totalAttempts }}</span>
                                    <span class="stat-label">Lần làm bài</span>
                                </div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-icon score-icon">
                                    <i class="bi bi-trophy"></i>
                                </div>
                                <div class="stat-info">
                                    <span class="stat-number">{{ userData.averageScore }}%</span>
                                    <span class="stat-label">Điểm TB</span>
                                </div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-icon best-icon">
                                    <i class="bi bi-star-fill"></i>
                                </div>
                                <div class="stat-info">
                                    <span class="stat-number">{{ userData.bestScore }}%</span>
                                    <span class="stat-label">Điểm cao nhất</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Navigation Tabs -->
            <div class="profile-nav">
                <div class="container">
                    <div class="nav-tabs">
                        <button :class="['nav-tab', { active: activeTab === 'profile' }]"
                            @click="setActiveTab('profile')">
                            <i class="bi bi-person"></i>
                            <span>Hồ sơ</span>
                        </button>
                        <button :class="['nav-tab', { active: activeTab === 'activity' }]"
                            @click="setActiveTab('activity')">
                            <i class="bi bi-activity"></i>
                            <span>Hoạt động</span>
                        </button>
                        <button :class="['nav-tab', { active: activeTab === 'achievements' }]"
                            @click="setActiveTab('achievements')">
                            <i class="bi bi-award"></i>
                            <span>Thành tựu</span>
                        </button>
                        <button :class="['nav-tab', { active: activeTab === 'settings' }]"
                            @click="setActiveTab('settings')">
                            <i class="bi bi-gear"></i>
                            <span>Cài đặt</span>
                        </button>
                    </div>
                </div>
            </div>

            <!-- Tab Content -->
            <div class="tab-content">
                <div class="container">
                    <!-- Profile Tab -->
                    <div v-if="activeTab === 'profile'" class="tab-panel profile-panel">
                        <div class="row">
                            <!-- Profile Info -->
                            <div class="col-lg-8">
                                <div class="content-card">
                                    <div class="card-header">
                                        <h3 class="card-title">
                                            <i class="bi bi-person-gear"></i>
                                            Thông tin cá nhân
                                        </h3>
                                        <button v-if="!isEditing" @click="startEditing" class="edit-btn">
                                            <i class="bi bi-pencil"></i>
                                            Chỉnh sửa
                                        </button>
                                    </div>

                                    <div class="card-body">
                                        <!-- View Mode -->
                                        <div v-if="!isEditing" class="profile-view">
                                            <div class="info-group">
                                                <label class="info-label">Tên đầy đủ</label>
                                                <p class="info-value">{{ userData.fullName || 'Chưa cập nhật' }}</p>
                                            </div>
                                            <div class="info-group">
                                                <label class="info-label">Email</label>
                                                <p class="info-value">{{ userData.email }}</p>
                                            </div>
                                            <div class="info-group">
                                                <label class="info-label">Tên người dùng</label>
                                                <p class="info-value">{{ userData.username }}</p>
                                            </div>
                                            <div class="info-group">
                                                <label class="info-label">Giới thiệu</label>
                                                <p class="info-value">{{ userData.bio || 'Chưa có mô tả về bản thân' }}
                                                </p>
                                            </div>
                                        </div>

                                        <!-- Edit Mode -->
                                        <div v-else class="profile-edit">
                                            <form @submit.prevent="saveProfile">
                                                <!-- Avatar Upload -->
                                                <div class="form-group">
                                                    <label class="form-label">Ảnh đại diện</label>
                                                    <div class="avatar-upload">
                                                        <div class="current-avatar">
                                                            <img :src="avatarPreview || userData.avatarUrl || '/img/default-avatar.png'"
                                                                alt="Avatar Preview" class="avatar-preview" />
                                                        </div>
                                                        <div class="upload-controls">
                                                            <input type="file" id="avatarFile"
                                                                @change="handleFileUpload" accept="image/*"
                                                                class="file-input" />
                                                            <label for="avatarFile" class="upload-btn">
                                                                <i class="bi bi-camera"></i>
                                                                Thay đổi ảnh
                                                            </label>
                                                            <button v-if="avatarPreview" type="button"
                                                                @click="clearAvatar" class="clear-btn">
                                                                <i class="bi bi-x-lg"></i>
                                                            </button>
                                                        </div>
                                                    </div>
                                                </div>

                                                <div class="form-group">
                                                    <label class="form-label">Tên đầy đủ</label>
                                                    <input type="text" v-model="editForm.fullName" class="form-input"
                                                        placeholder="Nhập tên đầy đủ của bạn" />
                                                </div>

                                                <div class="form-group">
                                                    <label class="form-label">Giới thiệu</label>
                                                    <textarea v-model="editForm.bio" class="form-textarea" rows="4"
                                                        placeholder="Viết vài dòng về bản thân..."
                                                        maxlength="500"></textarea>
                                                    <div class="char-count">{{ (editForm.bio || '').length }}/500</div>
                                                </div>

                                                <div class="form-actions">
                                                    <button type="submit" class="save-btn" :disabled="isSaving">
                                                        <div v-if="isSaving" class="btn-loading">
                                                            <div class="spinner"></div>
                                                            <span>Đang lưu...</span>
                                                        </div>
                                                        <div v-else class="btn-content">
                                                            <i class="bi bi-check-lg"></i>
                                                            <span>Lưu thay đổi</span>
                                                        </div>
                                                    </button>
                                                    <button type="button" @click="cancelEdit" class="cancel-btn">
                                                        <i class="bi bi-x-lg"></i>
                                                        Hủy
                                                    </button>
                                                </div>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Quick Stats -->
                            <div class="col-lg-4">
                                <div class="content-card">
                                    <div class="card-header">
                                        <h3 class="card-title">
                                            <i class="bi bi-graph-up"></i>
                                            Thống kê nhanh
                                        </h3>
                                    </div>
                                    <div class="card-body">
                                        <div class="quick-stats">
                                            <div class="quick-stat">
                                                <div class="stat-icon">📊</div>
                                                <div class="stat-details">
                                                    <span class="stat-number">{{ userData.totalQuizzes }}</span>
                                                    <span class="stat-label">Quiz đã tạo</span>
                                                </div>
                                            </div>
                                            <div class="quick-stat">
                                                <div class="stat-icon">🎯</div>
                                                <div class="stat-details">
                                                    <span class="stat-number">{{ userData.totalAttempts }}</span>
                                                    <span class="stat-label">Lần tham gia</span>
                                                </div>
                                            </div>
                                            <div class="quick-stat">
                                                <div class="stat-icon">⭐</div>
                                                <div class="stat-details">
                                                    <span class="stat-number">{{ earnedAchievements.length }}</span>
                                                    <span class="stat-label">Thành tựu</span>
                                                </div>
                                            </div>
                                            <div class="quick-stat">
                                                <div class="stat-icon">🏆</div>
                                                <div class="stat-details">
                                                    <span class="stat-number">{{ userData.bestScore }}%</span>
                                                    <span class="stat-label">Điểm cao nhất</span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Activity Tab -->
                    <div v-if="activeTab === 'activity'" class="tab-panel activity-panel">
                        <div class="row">
                            <div class="col-lg-8">
                                <div class="content-card">
                                    <div class="card-header">
                                        <h3 class="card-title">
                                            <i class="bi bi-clock-history"></i>
                                            Hoạt động gần đây
                                        </h3>
                                    </div>
                                    <div class="card-body">
                                        <div class="activity-timeline">
                                            <div v-for="activity in recentActivities" :key="activity.id"
                                                class="timeline-item">
                                                <div class="timeline-icon">
                                                    <i :class="activity.icon"></i>
                                                </div>
                                                <div class="timeline-content">
                                                    <p class="timeline-message">{{ activity.message }}</p>
                                                    <span class="timeline-time">{{ activity.time }}</span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="col-lg-4">
                                <div class="content-card">
                                    <div class="card-header">
                                        <h3 class="card-title">
                                            <i class="bi bi-list-ol"></i>
                                            Lịch sử làm bài
                                        </h3>
                                    </div>
                                    <div class="card-body">
                                        <div class="quiz-history">
                                            <div v-for="quiz in quizHistory.slice(0, 5)" :key="quiz.id"
                                                class="history-item">
                                                <div class="quiz-info">
                                                    <h4 class="quiz-title">{{ quiz.quizTitle }}</h4>
                                                    <span class="quiz-score" :class="getScoreClass(quiz.score)">
                                                        {{ quiz.score }}%
                                                    </span>
                                                </div>
                                                <div class="quiz-time">
                                                    {{ new Date(quiz.completedAt).toLocaleDateString('vi-VN') }}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Achievements Tab -->
                    <div v-if="activeTab === 'achievements'" class="tab-panel achievements-panel">
                        <div class="content-card">
                            <div class="card-header">
                                <h3 class="card-title">
                                    <i class="bi bi-trophy"></i>
                                    Thành tựu của bạn
                                </h3>
                                <div class="achievement-stats">
                                    <span class="earned-count">{{ earnedAchievements.length }}</span>
                                    /
                                    <span class="total-count">{{ achievements.length }}</span> đã đạt được
                                </div>
                            </div>
                            <div class="card-body">
                                <!-- Earned Achievements -->
                                <div class="achievement-section">
                                    <h4 class="section-title">🏆 Đã đạt được</h4>
                                    <div class="achievements-grid">
                                        <div v-for="achievement in earnedAchievements" :key="achievement.id"
                                            class="achievement-card earned">
                                            <div class="achievement-icon">
                                                <i :class="achievement.icon"></i>
                                            </div>
                                            <div class="achievement-info">
                                                <h5 class="achievement-name">{{ achievement.name }}</h5>
                                                <p class="achievement-desc">{{ achievement.description }}</p>
                                                <span class="achievement-date">
                                                    Đạt được ngày {{ new
                                                        Date(achievement.earnedAt).toLocaleDateString('vi-VN') }}
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- Pending Achievements -->
                                <div class="achievement-section">
                                    <h4 class="section-title">🎯 Chưa đạt được</h4>
                                    <div class="achievements-grid">
                                        <div v-for="achievement in pendingAchievements" :key="achievement.id"
                                            class="achievement-card pending">
                                            <div class="achievement-icon">
                                                <i :class="achievement.icon"></i>
                                            </div>
                                            <div class="achievement-info">
                                                <h5 class="achievement-name">{{ achievement.name }}</h5>
                                                <p class="achievement-desc">{{ achievement.description }}</p>
                                                <span class="achievement-hint">Tiếp tục cố gắng để mở khóa!</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Settings Tab -->
                    <div v-if="activeTab === 'settings'" class="tab-panel settings-panel">
                        <div class="row">
                            <!-- Security Settings -->
                            <div class="col-lg-8">
                                <div class="content-card">
                                    <div class="card-header">
                                        <h3 class="card-title">
                                            <i class="bi bi-shield-lock"></i>
                                            Bảo mật tài khoản
                                        </h3>
                                    </div>
                                    <div class="card-body">
                                        <form @submit.prevent="changePassword">
                                            <div class="form-group">
                                                <label class="form-label">Mật khẩu hiện tại</label>
                                                <div class="password-input">
                                                    <input :type="showCurrentPassword ? 'text' : 'password'"
                                                        v-model="passwordForm.currentPassword" class="form-input"
                                                        placeholder="Nhập mật khẩu hiện tại" required />
                                                    <button type="button"
                                                        @click="showCurrentPassword = !showCurrentPassword"
                                                        class="password-toggle">
                                                        <div class="eye-icon"
                                                            :class="{ 'eye-hidden': showCurrentPassword }"></div>
                                                    </button>
                                                </div>
                                            </div>

                                            <div class="form-group">
                                                <label class="form-label">Mật khẩu mới</label>
                                                <div class="password-input">
                                                    <input :type="showNewPassword ? 'text' : 'password'"
                                                        v-model="passwordForm.newPassword" class="form-input"
                                                        placeholder="Nhập mật khẩu mới" required minlength="8" />
                                                    <button type="button" @click="showNewPassword = !showNewPassword"
                                                        class="password-toggle">
                                                        <div class="eye-icon"
                                                            :class="{ 'eye-hidden': showNewPassword }"></div>
                                                    </button>
                                                </div>

                                                <!-- Password Strength -->
                                                <div v-if="passwordForm.newPassword" class="password-strength">
                                                    <div class="strength-bar">
                                                        <div class="strength-fill" :style="{
                                                            width: (passwordStrength * 20) + '%',
                                                            backgroundColor: getPasswordStrengthColor(passwordStrength)
                                                        }"></div>
                                                    </div>
                                                    <span class="strength-text"
                                                        :style="{ color: getPasswordStrengthColor(passwordStrength) }">
                                                        {{ getPasswordStrengthText(passwordStrength) }}
                                                    </span>
                                                </div>
                                            </div>

                                            <div class="form-group">
                                                <label class="form-label">Xác nhận mật khẩu mới</label>
                                                <div class="password-input">
                                                    <input :type="showConfirmPassword ? 'text' : 'password'"
                                                        v-model="passwordForm.confirmPassword" class="form-input"
                                                        :class="{
                                                            'match-success': passwordMatch === true,
                                                            'match-error': passwordMatch === false
                                                        }" placeholder="Nhập lại mật khẩu mới" required />
                                                    <button type="button"
                                                        @click="showConfirmPassword = !showConfirmPassword"
                                                        class="password-toggle">
                                                        <div class="eye-icon"
                                                            :class="{ 'eye-hidden': showConfirmPassword }"></div>
                                                    </button>
                                                </div>

                                                <div v-if="passwordForm.confirmPassword" class="password-match">
                                                    <i :class="[
                                                        passwordMatch ? 'bi bi-check-lg' : 'bi bi-x-lg',
                                                        passwordMatch ? 'text-success' : 'text-danger'
                                                    ]"></i>
                                                    <span :class="passwordMatch ? 'text-success' : 'text-danger'">
                                                        {{ passwordMatch ? 'Mật khẩu khớp' : 'Mật khẩu không khớp' }}
                                                    </span>
                                                </div>
                                            </div>

                                            <button type="submit" class="change-password-btn"
                                                :disabled="isChangingPassword || !passwordMatch || passwordStrength < 3">
                                                <div v-if="isChangingPassword" class="btn-loading">
                                                    <div class="spinner"></div>
                                                    <span>Đang thay đổi...</span>
                                                </div>
                                                <div v-else class="btn-content">
                                                    <i class="bi bi-shield-check"></i>
                                                    <span>Đổi mật khẩu</span>
                                                </div>
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            </div>

                            <!-- Account Settings -->
                            <div class="col-lg-4">
                                <div class="content-card">
                                    <div class="card-header">
                                        <h3 class="card-title">
                                            <i class="bi bi-gear"></i>
                                            Cài đặt tài khoản
                                        </h3>
                                    </div>
                                    <div class="card-body">
                                        <div class="settings-list">
                                            <div class="setting-item">
                                                <div class="setting-info">
                                                    <h5>Thông báo email</h5>
                                                    <p>Nhận thông báo về hoạt động</p>
                                                </div>
                                                <div class="setting-control">
                                                    <label class="toggle-switch">
                                                        <input type="checkbox" checked />
                                                        <span class="slider"></span>
                                                    </label>
                                                </div>
                                            </div>

                                            <div class="setting-item">
                                                <div class="setting-info">
                                                    <h5>Hồ sơ công khai</h5>
                                                    <p>Cho phép người khác xem hồ sơ</p>
                                                </div>
                                                <div class="setting-control">
                                                    <label class="toggle-switch">
                                                        <input type="checkbox" checked />
                                                        <span class="slider"></span>
                                                    </label>
                                                </div>
                                            </div>
                                        </div>

                                        <div class="danger-zone">
                                            <h4 class="danger-title">
                                                <i class="bi bi-exclamation-triangle"></i>
                                                Vùng nguy hiểm
                                            </h4>
                                            <p class="danger-desc">Những hành động này không thể hoàn tác</p>
                                            <button @click="deleteAccount" class="delete-account-btn">
                                                <i class="bi bi-trash3"></i>
                                                Xóa tài khoản
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Toast Message -->
        <div v-if="message" :class="['toast-message', messageType]">
            <div class="toast-icon">
                <i :class="{
                    'bi bi-check-circle-fill': messageType === 'success',
                    'bi bi-exclamation-triangle-fill': messageType === 'error',
                    'bi bi-info-circle-fill': messageType === 'info'
                }"></i>
            </div>
            <span class="toast-text">{{ message }}</span>
            <button @click="message = ''" class="toast-close">
                <i class="bi bi-x-lg"></i>
            </button>
        </div>
    </div>
</template>

<style scoped>
/* === BASE STYLES === */
.user-profile-container {
    min-height: 100vh;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    position: relative;
    overflow: hidden;
}

/* === ANIMATED BACKGROUND === */
.background-decorations {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 0;
}

.floating-orb {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(20px);
    animation: float 8s ease-in-out infinite;
}

.orb-1 {
    width: 180px;
    height: 180px;
    top: 15%;
    left: -8%;
    animation-delay: 0s;
}

.orb-2 {
    width: 120px;
    height: 120px;
    top: 70%;
    right: -6%;
    animation-delay: 3s;
}

.orb-3 {
    width: 80px;
    height: 80px;
    top: 40%;
    right: 25%;
    animation-delay: 6s;
}

.orb-4 {
    width: 100px;
    height: 100px;
    top: 85%;
    left: 30%;
    animation-delay: 2s;
}

@keyframes float {

    0%,
    100% {
        transform: translateY(0px) rotate(0deg) scale(1);
    }

    33% {
        transform: translateY(-25px) rotate(3deg) scale(1.05);
    }

    66% {
        transform: translateY(15px) rotate(-3deg) scale(0.95);
    }
}

/* === LOADING SECTION === */
.loading-section {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    position: relative;
    z-index: 1;
}

.loading-container {
    text-align: center;
    color: white;
}

.modern-spinner {
    position: relative;
    width: 120px;
    height: 120px;
    margin: 0 auto 2rem;
}

.spinner-ring {
    position: absolute;
    width: 100%;
    height: 100%;
    border: 4px solid transparent;
    border-top-color: #00d4ff;
    border-radius: 50%;
    animation: spin 1.8s linear infinite;
}

.spinner-ring:nth-child(2) {
    width: 85%;
    height: 85%;
    top: 7.5%;
    left: 7.5%;
    border-top-color: #5f27cd;
    animation-delay: 0.6s;
}

.spinner-ring:nth-child(3) {
    width: 70%;
    height: 70%;
    top: 15%;
    left: 15%;
    border-top-color: #fff;
    animation-delay: 1.2s;
}

.loading-text {
    font-size: 1.8rem;
    font-weight: 700;
    margin-bottom: 0.5rem;
}

.loading-subtitle {
    opacity: 0.8;
    font-size: 1.1rem;
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}

/* === PROFILE CONTENT === */
.profile-content {
    position: relative;
    z-index: 1;
}

/* === PROFILE HEADER === */
.profile-header {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(30px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);
    padding: 3rem 0;
}

.header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 3rem;
}

.user-info {
    display: flex;
    align-items: center;
    gap: 2rem;
}

.avatar-section {
    position: relative;
}

.user-avatar {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    object-fit: cover;
    border: 4px solid rgba(255, 255, 255, 0.3);
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.avatar-status {
    position: absolute;
    bottom: 8px;
    right: 8px;
    width: 24px;
    height: 24px;
    border-radius: 50%;
    border: 3px solid white;
}

.avatar-status.online {
    background: #2ed573;
}

.user-details {
    color: white;
}

.user-name {
    font-size: 2.5rem;
    font-weight: 800;
    margin-bottom: 0.5rem;
    background: linear-gradient(45deg, #fff, #00d4ff);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.user-username {
    font-size: 1.2rem;
    opacity: 0.8;
    margin-bottom: 1rem;
}

.user-bio {
    font-size: 1.1rem;
    opacity: 0.9;
    margin-bottom: 1rem;
    max-width: 400px;
    line-height: 1.6;
}

.user-meta {
    display: flex;
    gap: 2rem;
}

.meta-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.95rem;
    opacity: 0.8;
}

.user-stats {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 1.5rem;
}

.stat-card {
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(20px);
    border-radius: 20px;
    padding: 1.5rem;
    display: flex;
    align-items: center;
    gap: 1rem;
    border: 1px solid rgba(255, 255, 255, 0.2);
    transition: all 0.3s ease;
}

.stat-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 15px 40px rgba(0, 0, 0, 0.2);
}

.stat-icon {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.5rem;
    color: white;
}

.quiz-icon {
    background: linear-gradient(45deg, #00d4ff, #0099cc);
}

.attempt-icon {
    background: linear-gradient(45deg, #5f27cd, #8e44ad);
}

.score-icon {
    background: linear-gradient(45deg, #2ed573, #27ae60);
}

.best-icon {
    background: linear-gradient(45deg, #ffa502, #ff7675);
}

.stat-info {
    color: white;
}

.stat-number {
    display: block;
    font-size: 1.8rem;
    font-weight: 700;
    margin-bottom: 0.25rem;
}

.stat-label {
    font-size: 0.9rem;
    opacity: 0.8;
}

/* === NAVIGATION TABS === */
.profile-nav {
    background: rgba(255, 255, 255, 0.05);
    backdrop-filter: blur(20px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    padding: 1rem 0;
}

.nav-tabs {
    display: flex;
    gap: 0.5rem;
}

.nav-tab {
    background: transparent;
    border: none;
    color: rgba(255, 255, 255, 0.7);
    padding: 1rem 2rem;
    border-radius: 15px;
    display: flex;
    align-items: center;
    gap: 0.75rem;
    font-weight: 600;
    font-size: 1rem;
    cursor: pointer;
    transition: all 0.3s ease;
}

.nav-tab:hover {
    background: rgba(255, 255, 255, 0.1);
    color: white;
    transform: translateY(-2px);
}

.nav-tab.active {
    background: rgba(255, 255, 255, 0.2);
    color: white;
    box-shadow: 0 5px 20px rgba(0, 0, 0, 0.2);
}

/* === TAB CONTENT === */
.tab-content {
    padding: 3rem 0;
}

.tab-panel {
    animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* === CONTENT CARDS === */
.content-card {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(30px);
    border-radius: 25px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
    border: 2px solid rgba(255, 255, 255, 0.3);
    margin-bottom: 2rem;
    overflow: hidden;
}

.card-header {
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(95, 39, 205, 0.1));
    padding: 2rem;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.card-title {
    font-size: 1.5rem;
    font-weight: 700;
    color: #1a202c;
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin: 0;
}

.card-body {
    padding: 2rem;
}

/* === FORM ELEMENTS === */
.form-group {
    margin-bottom: 2rem;
}

.form-label {
    display: block;
    font-weight: 700;
    color: #1a202c;
    margin-bottom: 0.75rem;
    font-size: 1rem;
}

.form-input,
.form-textarea {
    width: 100%;
    padding: 1rem 1.25rem;
    border: 2px solid #e2e8f0;
    border-radius: 15px;
    font-size: 1rem;
    font-weight: 500;
    color: #1a202c;
    background: rgba(255, 255, 255, 0.9);
    transition: all 0.3s ease;
}

.form-input:focus,
.form-textarea:focus {
    outline: none;
    border-color: #00d4ff;
    box-shadow: 0 0 0 0.3rem rgba(0, 212, 255, 0.2);
    background: white;
    transform: translateY(-2px);
}

.form-textarea {
    resize: vertical;
    min-height: 120px;
}

.char-count {
    text-align: right;
    font-size: 0.9rem;
    color: #718096;
    margin-top: 0.5rem;
}

/* === PROFILE VIEW/EDIT === */
.profile-view .info-group {
    margin-bottom: 2rem;
    padding-bottom: 1.5rem;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.profile-view .info-group:last-child {
    border-bottom: none;
    margin-bottom: 0;
}

.info-label {
    font-weight: 700;
    color: #4a5568;
    font-size: 0.9rem;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-bottom: 0.5rem;
}

.info-value {
    font-size: 1.1rem;
    color: #1a202c;
    font-weight: 500;
    margin: 0;
}

.edit-btn {
    background: linear-gradient(45deg, #00d4ff, #5f27cd);
    color: white;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: 12px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.edit-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(95, 39, 205, 0.4);
}

/* === AVATAR UPLOAD === */
.avatar-upload {
    display: flex;
    align-items: center;
    gap: 2rem;
}

.current-avatar .avatar-preview {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    object-fit: cover;
    border: 3px solid #e2e8f0;
}

.upload-controls {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.file-input {
    display: none;
}

.upload-btn {
    background: linear-gradient(45deg, #00d4ff, #5f27cd);
    color: white;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: 12px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.upload-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(95, 39, 205, 0.4);
}

.clear-btn {
    background: #ff4757;
    color: white;
    border: none;
    padding: 0.5rem 1rem;
    border-radius: 8px;
    font-size: 0.9rem;
    cursor: pointer;
    transition: all 0.3s ease;
}

.clear-btn:hover {
    background: #ff3838;
    transform: translateY(-1px);
}

/* === FORM ACTIONS === */
.form-actions {
    display: flex;
    gap: 1rem;
    margin-top: 2rem;
}

.save-btn {
    background: linear-gradient(45deg, #2ed573, #27ae60);
    color: white;
    border: none;
    padding: 1rem 2rem;
    border-radius: 15px;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    gap: 0.75rem;
}

.save-btn:hover:not(:disabled) {
    transform: translateY(-3px);
    box-shadow: 0 12px 30px rgba(46, 213, 115, 0.4);
}

.save-btn:disabled {
    opacity: 0.7;
    cursor: not-allowed;
}

.cancel-btn {
    background: rgba(255, 71, 87, 0.1);
    color: #ff4757;
    border: 2px solid #ff4757;
    padding: 1rem 2rem;
    border-radius: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    gap: 0.75rem;
}

.cancel-btn:hover {
    background: #ff4757;
    color: white;
    transform: translateY(-2px);
}

/* === PASSWORD FEATURES === */
.password-input {
    position: relative;
}

.password-toggle {
    position: absolute;
    right: 15px;
    top: 50%;
    transform: translateY(-50%);
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 8px;
    color: rgba(0, 0, 0, 0.6);
    cursor: pointer;
    transition: all 0.3s ease;
    z-index: 100;
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(10px);
}

.password-toggle:hover {
    background: rgba(0, 212, 255, 0.1);
    border-color: #00d4ff;
    color: #00d4ff;
}

/* Eye Icon */
.eye-icon {
    position: relative;
    width: 18px;
    height: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.eye-icon:not(.eye-hidden)::before {
    content: '';
    position: absolute;
    width: 16px;
    height: 8px;
    border: 2px solid currentColor;
    border-radius: 16px;
    background: transparent;
}

.eye-icon:not(.eye-hidden)::after {
    content: '';
    position: absolute;
    width: 4px;
    height: 4px;
    background: currentColor;
    border-radius: 50%;
    transition: all 0.2s ease;
}

.eye-icon.eye-hidden::before {
    content: '';
    position: absolute;
    width: 16px;
    height: 8px;
    border: 2px solid currentColor;
    border-radius: 16px;
    background: transparent;
    opacity: 0.6;
}

.eye-icon.eye-hidden::after {
    content: '';
    position: absolute;
    width: 2px;
    height: 20px;
    background: currentColor;
    transform: rotate(45deg);
    opacity: 0.8;
}

.password-strength {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-top: 0.75rem;
}

.strength-bar {
    flex: 1;
    height: 8px;
    background: rgba(0, 0, 0, 0.1);
    border-radius: 4px;
    overflow: hidden;
}

.strength-fill {
    height: 100%;
    transition: all 0.3s ease;
    border-radius: 4px;
}

.strength-text {
    font-size: 0.9rem;
    font-weight: 600;
    min-width: 80px;
}

.password-match {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-top: 0.75rem;
    font-size: 0.95rem;
    font-weight: 500;
}

.text-success {
    color: #2ed573;
}

.text-danger {
    color: #ff4757;
}

.match-success {
    border-color: #2ed573 !important;
}

.match-error {
    border-color: #ff4757 !important;
}

.change-password-btn {
    background: linear-gradient(45deg, #5f27cd, #8e44ad);
    color: white;
    border: none;
    padding: 1rem 2rem;
    border-radius: 15px;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-top: 1rem;
}

.change-password-btn:hover:not(:disabled) {
    transform: translateY(-3px);
    box-shadow: 0 12px 30px rgba(95, 39, 205, 0.4);
}

.change-password-btn:disabled {
    opacity: 0.7;
    cursor: not-allowed;
}

/* === QUICK STATS === */
.quick-stats {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.quick-stat {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 1rem;
    background: rgba(102, 126, 234, 0.05);
    border-radius: 15px;
    border: 1px solid rgba(102, 126, 234, 0.1);
}

.quick-stat .stat-icon {
    font-size: 1.5rem;
}

.quick-stat .stat-details {
    display: flex;
    flex-direction: column;
}

.quick-stat .stat-number {
    font-size: 1.3rem;
    font-weight: 700;
    color: #1a202c;
}

.quick-stat .stat-label {
    font-size: 0.9rem;
    color: #718096;
}

/* === ACTIVITY TIMELINE === */
.activity-timeline {
    position: relative;
}

.activity-timeline::before {
    content: '';
    position: absolute;
    left: 25px;
    top: 0;
    bottom: 0;
    width: 2px;
    background: linear-gradient(to bottom, #00d4ff, #5f27cd);
}

.timeline-item {
    display: flex;
    align-items: flex-start;
    gap: 1.5rem;
    margin-bottom: 2rem;
    position: relative;
}

.timeline-icon {
    width: 50px;
    height: 50px;
    background: linear-gradient(45deg, #00d4ff, #5f27cd);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 1.2rem;
    z-index: 1;
    box-shadow: 0 5px 20px rgba(95, 39, 205, 0.3);
}

.timeline-content {
    flex: 1;
    background: rgba(102, 126, 234, 0.05);
    padding: 1.5rem;
    border-radius: 15px;
    border: 1px solid rgba(102, 126, 234, 0.1);
}

.timeline-message {
    margin: 0 0 0.5rem;
    color: #1a202c;
    font-weight: 500;
}

.timeline-time {
    font-size: 0.9rem;
    color: #718096;
}

/* === QUIZ HISTORY === */
.quiz-history {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.history-item {
    padding: 1rem;
    background: rgba(102, 126, 234, 0.05);
    border-radius: 12px;
    border: 1px solid rgba(102, 126, 234, 0.1);
}

.quiz-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.5rem;
}

.quiz-title {
    font-size: 1rem;
    font-weight: 600;
    color: #1a202c;
    margin: 0;
}

.quiz-score {
    padding: 0.25rem 0.75rem;
    border-radius: 20px;
    font-size: 0.9rem;
    font-weight: 700;
}

.quiz-score.excellent {
    background: #2ed573;
    color: white;
}

.quiz-score.good {
    background: #ffa502;
    color: white;
}

.quiz-score.average {
    background: #ff4757;
    color: white;
}

.quiz-time {
    font-size: 0.9rem;
    color: #718096;
}

/* === ACHIEVEMENTS === */
.achievement-stats {
    color: #718096;
    font-size: 1rem;
}

.earned-count {
    color: #2ed573;
    font-weight: 700;
}

.total-count {
    font-weight: 600;
}

.achievement-section {
    margin-bottom: 3rem;
}

.section-title {
    font-size: 1.3rem;
    font-weight: 700;
    color: #1a202c;
    margin-bottom: 1.5rem;
}

.achievements-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 1.5rem;
}

.achievement-card {
    background: rgba(255, 255, 255, 0.8);
    border-radius: 20px;
    padding: 2rem;
    display: flex;
    align-items: center;
    gap: 1.5rem;
    transition: all 0.3s ease;
    border: 2px solid transparent;
}

.achievement-card.earned {
    border-color: rgba(46, 213, 115, 0.3);
    box-shadow: 0 10px 30px rgba(46, 213, 115, 0.1);
}

.achievement-card.earned:hover {
    transform: translateY(-5px);
    box-shadow: 0 15px 40px rgba(46, 213, 115, 0.2);
}

.achievement-card.pending {
    opacity: 0.6;
    border-color: rgba(0, 0, 0, 0.1);
}

.achievement-card .achievement-icon {
    width: 60px;
    height: 60px;
    background: linear-gradient(45deg, #00d4ff, #5f27cd);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 1.5rem;
    flex-shrink: 0;
}

.achievement-card.pending .achievement-icon {
    background: #e2e8f0;
    color: #a0aec0;
}

.achievement-info {
    flex: 1;
}

.achievement-name {
    font-size: 1.2rem;
    font-weight: 700;
    color: #1a202c;
    margin-bottom: 0.5rem;
}

.achievement-desc {
    color: #718096;
    margin-bottom: 0.5rem;
    line-height: 1.4;
}

.achievement-date {
    font-size: 0.9rem;
    color: #2ed573;
    font-weight: 600;
}

.achievement-hint {
    font-size: 0.9rem;
    color: #a0aec0;
    font-style: italic;
}

/* === SETTINGS === */
.settings-list {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
    margin-bottom: 3rem;
}

.setting-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem;
    background: rgba(102, 126, 234, 0.05);
    border-radius: 15px;
    border: 1px solid rgba(102, 126, 234, 0.1);
}

.setting-info h5 {
    font-size: 1.1rem;
    font-weight: 600;
    color: #1a202c;
    margin-bottom: 0.25rem;
}

.setting-info p {
    color: #718096;
    margin: 0;
    font-size: 0.95rem;
}

.toggle-switch {
    position: relative;
    display: inline-block;
    width: 50px;
    height: 28px;
}

.toggle-switch input {
    opacity: 0;
    width: 0;
    height: 0;
}

.slider {
    position: absolute;
    cursor: pointer;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: #ccc;
    transition: .4s;
    border-radius: 28px;
}

.slider:before {
    position: absolute;
    content: "";
    height: 22px;
    width: 22px;
    left: 3px;
    bottom: 3px;
    background-color: white;
    transition: .4s;
    border-radius: 50%;
}

input:checked+.slider {
    background: linear-gradient(45deg, #00d4ff, #5f27cd);
}

input:checked+.slider:before {
    transform: translateX(22px);
}

.danger-zone {
    padding: 2rem;
    background: rgba(255, 71, 87, 0.05);
    border-radius: 15px;
    border: 2px solid rgba(255, 71, 87, 0.2);
}

.danger-title {
    color: #ff4757;
    font-size: 1.2rem;
    font-weight: 700;
    margin-bottom: 0.5rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.danger-desc {
    color: #718096;
    margin-bottom: 1.5rem;
}

.delete-account-btn {
    background: #ff4757;
    color: white;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: 12px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.delete-account-btn:hover {
    background: #ff3838;
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(255, 71, 87, 0.4);
}

/* === LOADING STATES === */
.btn-loading {
    display: flex;
    align-items: center;
    gap: 0.75rem;
}

.btn-content {
    display: flex;
    align-items: center;
    gap: 0.75rem;
}

.spinner {
    width: 16px;
    height: 16px;
    border: 2px solid rgba(255, 255, 255, 0.3);
    border-radius: 50%;
    border-top-color: white;
    animation: spin 1s linear infinite;
}

/* === TOAST MESSAGE === */
.toast-message {
    position: fixed;
    top: 2rem;
    right: 2rem;
    background: rgba(255, 255, 255, 0.98);
    backdrop-filter: blur(20px);
    border-radius: 15px;
    padding: 1rem 1.5rem;
    display: flex;
    align-items: center;
    gap: 1rem;
    box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
    border: 2px solid rgba(255, 255, 255, 0.3);
    z-index: 1000;
    animation: slideInRight 0.4s ease;
    min-width: 300px;
}

.toast-message.success {
    border-left: 4px solid #2ed573;
}

.toast-message.error {
    border-left: 4px solid #ff4757;
}

.toast-message.info {
    border-left: 4px solid #00d4ff;
}

.toast-icon {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.success .toast-icon {
    background: rgba(46, 213, 115, 0.1);
    color: #2ed573;
}

.error .toast-icon {
    background: rgba(255, 71, 87, 0.1);
    color: #ff4757;
}

.info .toast-icon {
    background: rgba(0, 212, 255, 0.1);
    color: #00d4ff;
}

.toast-text {
    flex: 1;
    color: #1a202c;
    font-weight: 500;
}

.toast-close {
    background: none;
    border: none;
    color: #a0aec0;
    cursor: pointer;
    padding: 0.25rem;
    border-radius: 4px;
    transition: all 0.3s ease;
}

.toast-close:hover {
    background: rgba(0, 0, 0, 0.1);
    color: #718096;
}

@keyframes slideInRight {
    from {
        transform: translateX(100%);
        opacity: 0;
    }

    to {
        transform: translateX(0);
        opacity: 1;
    }
}

/* === UTILITY FUNCTIONS === */
.getScoreClass {
    /* This will be handled by JavaScript */
}

/* === RESPONSIVE DESIGN === */
@media (max-width: 1200px) {
    .header-content {
        flex-direction: column;
        gap: 2rem;
    }

    .user-stats {
        grid-template-columns: repeat(4, 1fr);
    }
}

@media (max-width: 992px) {
    .user-info {
        flex-direction: column;
        text-align: center;
        gap: 1.5rem;
    }

    .user-stats {
        grid-template-columns: repeat(2, 1fr);
    }

    .nav-tabs {
        flex-wrap: wrap;
        justify-content: center;
    }

    .achievements-grid {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 768px) {
    .profile-header {
        padding: 2rem 0;
    }

    .user-avatar {
        width: 80px;
        height: 80px;
    }

    .user-name {
        font-size: 2rem;
    }

    .user-stats {
        grid-template-columns: 1fr;
        gap: 1rem;
    }

    .nav-tab {
        padding: 0.75rem 1rem;
        font-size: 0.9rem;
    }

    .card-header,
    .card-body {
        padding: 1.5rem;
    }

    .avatar-upload {
        flex-direction: column;
        align-items: center;
        gap: 1rem;
    }

    .form-actions {
        flex-direction: column;
    }

    .toast-message {
        top: 1rem;
        right: 1rem;
        left: 1rem;
        min-width: auto;
    }
}

@media (max-width: 480px) {
    .tab-content {
        padding: 2rem 0;
    }

    .user-name {
        font-size: 1.5rem;
    }

    .user-bio {
        font-size: 1rem;
    }

    .timeline-item {
        gap: 1rem;
    }

    .timeline-icon {
        width: 40px;
        height: 40px;
        font-size: 1rem;
    }

    .achievement-card {
        padding: 1.5rem;
        flex-direction: column;
        text-align: center;
    }
}

/* === ACCESSIBILITY === */
@media (prefers-reduced-motion: reduce) {
    * {
        animation-duration: 0.01ms !important;
        animation-iteration-count: 1 !important;
        transition-duration: 0.01ms !important;
    }
}

/* Focus styles for keyboard navigation */
.nav-tab:focus,
.edit-btn:focus,
.save-btn:focus,
.cancel-btn:focus,
.upload-btn:focus,
.change-password-btn:focus,
.delete-account-btn:focus {
    outline: 2px solid #00d4ff;
    outline-offset: 2px;
}
</style>