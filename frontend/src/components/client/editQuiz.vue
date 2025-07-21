<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const token = localStorage.getItem('token')
const userId = route.params.userId
const quizId = route.params.quizId

const questions = ref([])
const answersMap = ref({})
const quizInfo = ref({ id: quizId, title: '' })

const newQuestion = ref({
    content: '',
    point: 1,
    answers: Array(4).fill().map((_, i) => ({ content: '', correct: i === 0 }))
})

function setNewCorrectAnswer(index) {
    newQuestion.value.answers.forEach((a, i) => a.correct = i === index)
}

function setCorrectAnswer(questionId, answerId) {
    if (answersMap.value[questionId]) {
        answersMap.value[questionId].forEach((a) => {
            a.correct = a.id === answerId
        })
    }
}

async function fetchQuizInfo() {
    try {
        const res = await axios.get(`http://localhost:8080/api/quiz/${quizId}`, {
            headers: { Authorization: `Bearer ${token}` }
        })
        quizInfo.value = res.data
    } catch (err) {
        console.error('Lỗi khi lấy thông tin quiz:', err)
    }
}

async function fetchAnswersForQuestion(questionId) {
    try {
        const res = await axios.get(`http://localhost:8080/api/answer/${questionId}`, {
            headers: { Authorization: `Bearer ${token}` }
        })
        answersMap.value[questionId] = res.data
    } catch (error) {
        console.error('Lỗi khi lấy câu trả lời:', error)
    }
}

async function fetchQuestionsByQuizId() {
    try {
        const res = await axios.get(`http://localhost:8080/api/question/${quizId}`, {
            headers: { Authorization: `Bearer ${token}` }
        })
        questions.value = res.data

        await Promise.all(res.data.map(q => fetchAnswersForQuestion(q.id)))
    } catch (error) {
        console.error('Lỗi khi lấy câu hỏi:', error)
    }
}

onMounted(async () => {
    await Promise.all([fetchQuizInfo(), fetchQuestionsByQuizId()])
})

async function updateQuizTitle() {
    if (!quizInfo.value.title.trim()) {
        return alert('Tiêu đề quiz không được để trống!')
    }
    try {
        const payload = {
            ...quizInfo.value,
            category: quizInfo.value.category || null,
            image: quizInfo.value.image || null
        }
        await axios.put(`http://localhost:8080/api/quiz/${quizId}`, payload, {
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        })
        alert('Cập nhật tiêu đề quiz thành công!')
    } catch (err) {
        console.error('Lỗi khi cập nhật tiêu đề quiz:', err)
        alert('Cập nhật thất bại!')
    }
}

async function createQuestion() {
    // Kiểm tra nội dung câu hỏi
    if (!newQuestion.value.content.trim()) {
        alert('Nội dung câu hỏi không được để trống!');
        return;
    }

    // Kiểm tra câu trả lời
    const answers = newQuestion.value.answers;
    const hasEmptyAnswer = answers.some(a => !a.content.trim());
    if (hasEmptyAnswer) {
        alert('Tất cả câu trả lời phải có nội dung!');
        return;
    }

    // Kiểm tra có đúng 1 câu trả lời được chọn
    const correctCount = answers.filter(a => a.correct).length;
    if (correctCount !== 1) {
        alert('Phải chọn đúng một câu trả lời đúng!');
        return;
    }

    try {
        const questionPayload = {
            content: newQuestion.value.content.trim(),
            point: newQuestion.value.point,
            quiz: { id: quizId },
            image: null
        };

        const questionRes = await axios.post(`http://localhost:8080/api/question/create`, questionPayload, {
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const createdQuestion = questionRes.data;

        const answersPayload = answers.map(a => ({
            content: a.content.trim(),
            correct: a.correct,
            question: { id: createdQuestion.id }
        }));

        const answerRes = await axios.post(`http://localhost:8080/api/answer/create-multiple`, answersPayload, {
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!Array.isArray(questions.value)) {
            questions.value = []
        }
        questions.value.push(createdQuestion)

        answersMap.value[createdQuestion.id] = answerRes.data;

        // Reset form
        newQuestion.value = {
            content: '',
            point: 1,
            answers: Array(4).fill().map((_, i) => ({ content: '', correct: i === 0 }))
        };

        alert('Thêm câu hỏi và câu trả lời thành công!');
    } catch (err) {
        console.error('Lỗi khi tạo câu hỏi + câu trả lời:', err.response?.data || err.message || err);

        alert('Tạo câu hỏi thất bại!');
    }
}


async function saveQuestion(question) {
    try {
        const payload = {
            id: question.id,
            content: question.content,
            point: question.point,
            quiz: { id: quizId },
            image: null
        }

        await axios.put(`http://localhost:8080/api/question/update/${question.id}`, payload, {
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        })

        alert('Cập nhật câu hỏi thành công!')
    } catch (err) {
        console.error('Lỗi khi cập nhật câu hỏi:', err)
        alert('Cập nhật thất bại!')
    }
}

async function saveAnswers(questionId) {
    try {
        const answers = answersMap.value[questionId]

        const payload = answers.map((a) => ({
            id: a.id,
            content: a.content,
            correct: a.correct,
            question: { id: questionId }
        }))

        await axios.put(`http://localhost:8080/api/answer/update`, payload, {
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        })

        alert('Cập nhật câu trả lời thành công!')
    } catch (error) {
        console.error('Lỗi khi cập nhật câu trả lời:', error)
        alert('Cập nhật câu trả lời thất bại!')
    }
}

async function deleteQuestion(questionId) {
    if (!confirm('Bạn có chắc chắn muốn xoá câu hỏi này?')) return

    try {
        await axios.delete(`http://localhost:8080/api/question/delete/${questionId}`, {
            headers: { Authorization: `Bearer ${token}` }
        })

        questions.value = questions.value.filter(q => q.id !== questionId)
        delete answersMap.value[questionId]

        alert('Xoá câu hỏi thành công!')
    } catch (err) {
        console.error('Lỗi khi xoá câu hỏi:', err)
        alert('Xoá câu hỏi thất bại!')
    }
}
</script>


<template>
    <div class="container mt-4">
        <h1 class="mb-4">Chỉnh sửa Quiz</h1>
        <p><strong>User ID:</strong> {{ userId }}</p>
        <div class="card mb-4 shadow-sm">
            <div class="card-body">
                <h5 class="card-title">Chỉnh sửa tên Quiz</h5>
                <form @submit.prevent="updateQuizTitle">
                    <div class="mb-3">
                        <label class="form-label">Tiêu đề Quiz</label>
                        <input type="text" class="form-control" v-model="quizInfo.title" required />
                    </div>
                    <button type="submit" class="btn btn-success">Lưu tiêu đề</button>
                </form>
            </div>
        </div>

        <!-- Form thêm câu hỏi -->
        <div class="card mb-4 shadow-sm">
            <div class="card-body">
                <h5 class="card-title">Thêm mới câu hỏi</h5>
                <form @submit.prevent="createQuestion">
                    <div class="mb-3">
                        <label class="form-label">Nội dung câu hỏi</label>
                        <textarea class="form-control" v-model="newQuestion.content" rows="2" required></textarea>
                    </div>
                    <!-- Danh sách câu trả lời -->
                    <div class="mb-3">
                        <label class="form-label">Câu trả lời</label>
                        <div v-for="(answer, i) in newQuestion.answers" :key="i" class="input-group mb-2">
                            <div class="input-group-text">
                                <input class="form-check-input mt-0" type="radio" :name="'new-question-correct'"
                                    :checked="answer.correct" @change="setNewCorrectAnswer(i)" />
                            </div>
                            <input type="text" class="form-control" v-model="answer.content" required />
                        </div>
                        <small class="text-muted">Chọn một câu trả lời đúng</small>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Điểm</label>
                        <input type="number" class="form-control" v-model="newQuestion.point" min="1" required />
                    </div>

                    <button type="submit" class="btn btn-success">Thêm câu hỏi</button>
                </form>
            </div>
        </div>

        <div v-if="questions.length > 0">
            <h3 class="mt-4">Danh sách câu hỏi</h3>
            <div v-for="(q, index) in questions" :key="q.id" class="card mb-3 shadow-sm border-primary">
                <div class="card-body">
                    <form @submit.prevent="saveQuestion(q)">
                        <!-- Nội dung câu hỏi -->
                        <div class="mb-3">
                            <label class="form-label">Nội dung câu hỏi</label>
                            <textarea class="form-control" v-model="q.content" rows="2" required></textarea>
                        </div>

                        <!-- Danh sách câu trả lời -->
                        <div v-if="answersMap[q.id] && answersMap[q.id].length" class="mb-3">
                            <h6 class="fw-bold">Câu trả lời</h6>
                            <div v-for="(a, i) in answersMap[q.id]" :key="a.id" class="input-group mb-2">
                                <div class="input-group-text">
                                    <input class="form-check-input mt-0" type="radio" :name="'correctAnswer-' + q.id"
                                        :checked="a.correct" @change="setCorrectAnswer(q.id, a.id)" />
                                </div>
                                <input type="text" class="form-control" v-model="a.content" required />
                            </div>
                            <small class="text-muted">Chọn một câu trả lời đúng</small>


                            <!-- 👇 Thêm nút Lưu câu trả lời -->
                            <div class="mt-2">
                                <button type="button" class="btn btn-secondary btn-sm" @click="saveAnswers(q.id)">
                                    Lưu câu trả lời
                                </button>
                            </div>
                        </div>


                        <!-- Điểm -->
                        <div class="mb-3">
                            <label class="form-label">Điểm</label>
                            <input type="number" class="form-control" v-model="q.point" min="1" required />
                        </div>

                        <button type="submit" class="btn btn-primary me-2">Lưu câu hỏi</button>
                        <button type="button" class="btn btn-danger" @click="deleteQuestion(q.id)">Xoá câu hỏi</button>

                    </form>
                </div>
            </div>
        </div>


        <div v-else>
            <p>Không có câu hỏi nào cho quiz này.</p>
        </div>
    </div>
</template>
