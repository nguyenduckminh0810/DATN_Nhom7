<template>
    <div class="ban-page">
        <!-- soft particles -->
        <div class="particles">
            <span></span><span></span><span></span><span></span><span></span>
        </div>

        <div class="card">
            <div class="icon-wrap">
                <!-- shield slash icon -->
                <svg viewBox="0 0 24 24" class="icon" aria-hidden="true">
                    <path d="M12 3l7 3v5c0 4.97-3.05 9.39-7 10-3.95-.61-7-5.03-7-10V6l7-3z" opacity=".25" />
                    <path
                        d="M4 4l16 16M8.5 6.5L12 5l7 3v5c0 .96-.1 1.9-.3 2.79M12 21c-3.95-.61-7-5.03-7-10V6l2.5-1.1" />
                </svg>
            </div>

            <h1>Tài khoản của bạn đã bị khóa</h1>
            <p class="subtitle">
                Bạn không thể tiếp tục sử dụng hệ thống do vi phạm chính sách hoặc bị quản trị viên hạn chế quyền.
            </p>

            <div class="info">
                <div class="row">
                    <span class="label">Tài khoản</span>
                    <span class="value">{{ username || '—' }}</span>
                </div>
                <div class="row" v-if="email">
                    <span class="label">Email</span>
                    <span class="value">{{ email }}</span>
                </div>
                <div class="row">
                    <span class="label">Vai trò</span>
                    <span class="tag">{{ (role || 'BANNED').toUpperCase() }}</span>
                </div>
            </div>

            <div class="actions">
                <button class="btn primary" @click="relogin">
                    Đăng nhập tài khoản khác
                </button>
                <button class="btn link" @click="contact">
                    Liên hệ hỗ trợ
                </button>
            </div>

            <p class="footnote">
                Nếu bạn cho rằng đây là nhầm lẫn, vui lòng liên hệ quản trị viên để được hỗ trợ khôi phục.
            </p>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useLogin } from './client/useLogin'
const { logout } = useLogin()

const router = useRouter()
const user = ref(null)

onMounted(() => {
    try { user.value = JSON.parse(localStorage.getItem('user') || 'null') } catch { user.value = null }
})

const username = computed(() => user.value?.username || '')
const email = computed(() => user.value?.email || '')
const role = computed(() => user.value?.role || 'BANNED')

const relogin = () => {
    // clear mọi thứ để đăng nhập tài khoản khác
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('userId')
    localStorage.removeItem('admin_user')
    localStorage.removeItem('banned') // cho phép vào trang login
    logout()
    router.replace({ name: 'Login' })
}

const goHome = () => router.push({ name: 'Home' })

const contact = () => {
    const subject = encodeURIComponent('Yêu cầu hỗ trợ: Tài khoản bị khóa')
    const body = encodeURIComponent(`Xin chào,\n\nTôi muốn được hỗ trợ mở khóa tài khoản.\n- Username: ${username.value || ''}\n- Email: ${email.value || ''}\n- Ghi chú: \n\nXin cảm ơn.`)
    window.location.href = `mailto:support@quizmaster.local?subject=${subject}&body=${body}`
}
</script>

<style scoped>
:root {
    --bg1: #0b1220;
    --bg2: #0e1930;
    --card: rgba(255, 255, 255, 0.06);
    --stroke: rgba(255, 255, 255, 0.12);
    --text: #e8eefb;
    --muted: #a8b3c7;
    --primary: #20d3f3;
    --danger: #ff5c7a;
}

.ban-page {
    min-height: calc(100vh - 80px);
    display: grid;
    place-items: center;
    background:
        radial-gradient(1000px 600px at 80% -10%, rgba(35, 163, 255, 0.06), transparent 60%),
        radial-gradient(800px 500px at -10% 10%, rgba(32, 211, 243, 0.07), transparent 60%),
        linear-gradient(180deg, var(--bg1), var(--bg2));
    padding: 48px 16px;
    position: relative;
    overflow: hidden;
}

/* soft particles */
.particles {
    position: absolute;
    inset: 0;
    pointer-events: none;
}

.particles span {
    position: absolute;
    width: 14px;
    height: 14px;
    border-radius: 50%;
    background: radial-gradient(circle at 30% 30%, #fff, rgba(255, 255, 255, .2) 40%, transparent 60%);
    filter: blur(1px);
    opacity: .12;
    animation: float 12s linear infinite;
}

.particles span:nth-child(1) {
    top: 12%;
    left: 8%;
    animation-delay: -1s;
}

.particles span:nth-child(2) {
    top: 30%;
    right: 12%;
    animation-delay: -3s;
}

.particles span:nth-child(3) {
    bottom: 18%;
    left: 18%;
    animation-delay: -6s;
}

.particles span:nth-child(4) {
    top: 58%;
    right: 24%;
    animation-delay: -9s;
}

.particles span:nth-child(5) {
    bottom: 8%;
    right: 10%;
    animation-delay: -12s;
}

@keyframes float {
    0% {
        transform: translateY(0)
    }

    50% {
        transform: translateY(-10px)
    }

    100% {
        transform: translateY(0)
    }
}

.card {
    width: min(680px, 92vw);
    background: var(--card);
    border: 1px solid var(--stroke);
    border-radius: 20px;
    padding: 28px;
    box-shadow:
        0 8px 40px rgba(0, 0, 0, .45),
        0 0 0 1px rgba(255, 255, 255, .03) inset;
    backdrop-filter: blur(14px) saturate(120%);
    position: relative;
}

.card::before {
    content: "";
    position: absolute;
    inset: -1px;
    border-radius: 20px;
    padding: 1px;
    background: linear-gradient(135deg, rgba(32, 211, 243, .45), rgba(255, 92, 122, .4));
    -webkit-mask:
        linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
    -webkit-mask-composite: xor;
    mask-composite: exclude;
    pointer-events: none;
}

.icon-wrap {
    width: 68px;
    height: 68px;
    border-radius: 16px;
    display: grid;
    place-items: center;
    margin: 4px 0 12px;
    background: radial-gradient(120px 120px at 20% 10%, rgba(32, 211, 243, .25), transparent 40%),
        radial-gradient(140px 120px at 80% 90%, rgba(255, 92, 122, .18), transparent 50%),
        rgba(255, 255, 255, .06);
    border: 1px solid rgba(255, 255, 255, .12);
    box-shadow: 0 8px 24px rgba(32, 211, 243, .15), 0 0 0 1px rgba(255, 255, 255, .05) inset;
}

.icon {
    width: 36px;
    height: 36px;
    fill: none;
    stroke: var(--primary);
    stroke-width: 1.8
}

.icon path:first-child {
    fill: var(--primary);
}

h1 {
    color: var(--text);
    font-size: 24px;
    letter-spacing: .2px;
    margin: 12px 0 8px;
}

.subtitle {
    color: var(--muted);
    margin: 0 0 18px;
    line-height: 1.55;
}

.info {
    background: rgba(255, 255, 255, .04);
    border: 1px solid rgba(255, 255, 255, .08);
    border-radius: 14px;
    padding: 14px 16px;
}

.row {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    padding: 10px 4px;
    color: var(--text);
    border-bottom: 1px dashed rgba(255, 255, 255, .07);
}

.row:last-child {
    border-bottom: 0
}

.label {
    color: var(--muted)
}

.value {
    font-weight: 600
}

.tag {
    background: rgba(255, 92, 122, .18);
    color: #ffc9d3;
    border: 1px solid rgba(255, 92, 122, .35);
    padding: 4px 10px;
    border-radius: 999px;
    font-weight: 700;
    letter-spacing: .4px;
}

.actions {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin: 18px 0 6px;
}

.btn {
    appearance: none;
    border: 0;
    cursor: pointer;
    padding: 10px 14px;
    border-radius: 12px;
    font-weight: 600;
    letter-spacing: .2px;
    transition: transform .15s ease, box-shadow .15s ease, background .15s ease;
}

.btn.primary {
    color: #03121a;
    background: linear-gradient(135deg, #22d3ee, #1ab9f0);
    box-shadow: 0 8px 26px rgba(32, 211, 243, .25);
}

.btn.primary:hover {
    transform: translateY(-1px);
    box-shadow: 0 12px 32px rgba(32, 211, 243, .32)
}

.btn.ghost {
    color: var(--text);
    background: rgba(255, 255, 255, .06);
    border: 1px solid rgba(255, 255, 255, .12);
}

.btn.ghost:hover {
    background: rgba(255, 255, 255, .1)
}

.btn.link {
    color: #9bd7ff;
    background: transparent;
    text-decoration: underline;
}

.btn.link:hover {
    color: #b7e6ff
}

.footnote {
    color: var(--muted);
    margin-top: 10px;
    font-size: 13.5px;
}

@media (max-width: 520px) {
    .card {
        padding: 22px
    }

    h1 {
        font-size: 20px
    }
}
</style>
