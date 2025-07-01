// useQuizCRUD.js
import { ref, onMounted } from 'vue'
import { useLogin } from './useLogin'
import { useRouter } from 'vue-router'
import axios from 'axios'

export function useQuizCRUD() {
    const { token } = useLogin()
    const router = useRouter()
    const categories = ref([])

    const toQuizCRUD = () => {
        const currentToken = token?.value
        if (currentToken) {
            router.push('/quiz-crud')
        } else {
            alert('Bạn cần đăng nhập để truy cập Quiz CRUD.')
        }
    }

    onMounted(async () => {
        try {
            const res = await axios.get('http://localhost:8080/api/categories')
            categories.value = res.data
        } catch (error) {
            console.error('Không thể tải danh sách danh mục:', error)
        }
    })

    return {
        onMounted,
        token,
        toQuizCRUD,
        categories
    }
}
