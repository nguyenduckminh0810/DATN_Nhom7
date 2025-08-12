<template>
  <div class="review-overlay" @click.self="$emit('close')">
    <div class="review-modal">
      <div class="modal-header">
        <div class="title">
          <i class="bi bi-journal-text"></i>
          Xem câu trả lời
        </div>
        <button class="close-btn" @click="$emit('close')" aria-label="Đóng">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>

      <div class="modal-body" v-if="!loading">
        <div class="summary">
          <h3 class="quiz-title">{{ quizTitle }}</h3>
          <div class="meta">
            <span><i class="bi bi-trophy-fill"></i> {{ score }}/100</span>
            <span><i class="bi bi-clock"></i> {{ formatTime(timeTaken) }}</span>
            <span><i class="bi bi-calendar-event"></i> {{ formatDateTime(attemptedAt) }}</span>
          </div>
          <div class="legend">
            <span class="legend-item right"><i class="bi bi-check-lg"></i> Đáp án đúng</span>
            <span class="legend-item chosen"><i class="bi bi-hand-index"></i> Bạn đã chọn</span>
            <span class="legend-item wrong"><i class="bi bi-x-lg"></i> Chọn sai</span>
          </div>
        </div>

        <div v-if="questions.length" class="questions">
          <div
            v-for="(q, idx) in questions"
            :key="q.id"
            class="question-item"
          >
            <div class="q-head">
              <span class="q-number">{{ idx + 1 }}</span>
              <div class="q-content">{{ q.content }}</div>
              <div class="q-status" :class="{ correct: isCorrect(q.id), incorrect: !isCorrect(q.id) }">
                <i v-if="isCorrect(q.id)" class="bi bi-check-lg"></i>
                <i v-else-if="selectedIdByQuestion[q.id] != null" class="bi bi-x-lg"></i>
              </div>
            </div>
            <ul class="answers">
              <li
                v-for="(ans, i) in (q.answers || [])"
                :key="ans.id"
                :class="{
                  correct: ans.id === correctIdByQuestion[q.id],
                  'selected-right': ans.id === selectedIdByQuestion[q.id] && ans.id === correctIdByQuestion[q.id],
                  'selected-wrong': ans.id === selectedIdByQuestion[q.id] && ans.id !== correctIdByQuestion[q.id]
                }"
              >
                <span class="badge">{{ String.fromCharCode(65 + i) }}</span>
                <span class="content">{{ ans.content }}</span>
                <span class="icon" v-if="ans.id === correctIdByQuestion[q.id]"><i class="bi bi-check-lg"></i></span>
                <span class="icon wrong" v-else-if="selectedIdByQuestion[q.id] != null && ans.id === selectedIdByQuestion[q.id]"><i class="bi bi-x-lg"></i></span>
              </li>
            </ul>
          </div>
        </div>

        <div v-else class="empty">Không có dữ liệu câu hỏi.</div>
      </div>

      <div class="loading" v-else>
        Đang tải dữ liệu...
      </div>

      <div class="modal-footer">
        <button class="primary" @click="$emit('close')">
          <i class="bi bi-check2"></i>
          Đóng
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import api from '@/utils/axios'

const props = defineProps({
  attempt: { type: Object, required: true }
})

const userStore = useUserStore()

const loading = ref(true)
const quizId = ref(null)
const quizTitle = ref('')
const score = ref(0)
const timeTaken = ref(0)
const attemptedAt = ref(null)
const questions = ref([])
const correctIdByQuestion = ref({})
const selectedIdByQuestion = ref({})
const resultIdRef = ref(null)

const currentUserId = computed(() => userStore.user?.id)

onMounted(async () => {
  try {
    quizTitle.value = props.attempt?.quizTitle || ''
    score.value = props.attempt?.score || 0
    timeTaken.value = props.attempt?.timeTaken || 0
    attemptedAt.value = props.attempt?.attemptedAt || null

    // 1) Xác định resultId
    let resultId = props.attempt?.resultId || null
    if (!resultId && currentUserId.value) {
      try {
        const res = await api.get(`/result/user/${currentUserId.value}`)
        const results = Array.isArray(res.data) ? res.data : []
        // Ưu tiên khớp theo quizTitle và thời điểm gần nhất
        const sameQuiz = results.filter(r => r.quizTitle === quizTitle.value)
        if (sameQuiz.length) {
          const target = sameQuiz.reduce((best, cur) => {
            const diff = Math.abs(new Date(cur.completedAt).getTime() - new Date(attemptedAt.value).getTime())
            if (!best) return { item: cur, diff }
            return diff < best.diff ? { item: cur, diff } : best
          }, null)
          resultId = target?.item?.id || null
        }
      } catch {}
    }
    resultIdRef.value = resultId

    // 2) Lấy chi tiết kết quả (để có selected/correct và quizId)
    let selectedAnswers = []
    let correctAnswers = []
    if (resultId) {
      try {
        const resDetail = await api.get(`/result/${resultId}`)
        quizId.value = resDetail.data.quizId || resDetail.data.quiz?.id || null
        selectedAnswers = resDetail.data.selectedAnswers || []
        correctAnswers = resDetail.data.correctAnswers || []
      } catch {}
    }

    // 3) Tải câu hỏi + đáp án để render đẹp
    if (quizId.value) {
      const resQ = await api.get(`/question/play/${quizId.value}`)
      const list = resQ.data || []
      const enriched = await Promise.all(list.map(async q => {
        try {
          const ans = await api.get(`/answer/${q.id}`)
          return { ...q, answers: ans.data || [] }
        } catch {
          return { ...q, answers: [] }
        }
      }))
      questions.value = enriched
    }

    // 4) Dựng map correct và selected
    const cMap = {}
    // Ưu tiên dữ liệu từ BE; nếu thiếu thì suy ra từ danh sách answers
    if (Array.isArray(correctAnswers) && correctAnswers.length > 0) {
      for (const c of correctAnswers) {
        if (c?.questionId != null) cMap[Number(c.questionId)] = Number(c.correctAnswerId)
      }
    } else {
      for (const q of questions.value) {
        const found = (q.answers || []).find(
          a => a?.isCorrect === true || a?.correct === true || a?.is_correct === true,
        )
        if (found) cMap[Number(q.id)] = Number(found.id)
      }
    }

    let sMap = {}
    if (Array.isArray(selectedAnswers) && selectedAnswers.length > 0) {
      // Dữ liệu BE
      for (const s of selectedAnswers) {
        const qid = s?.questionId ?? s?.question_id ?? s?.question?.id ?? s?.qid
        const aid = s?.answerId ?? s?.answer_id ?? s?.answer?.id ?? s?.selectedAnswerId
        if (qid != null && aid != null) sMap[Number(qid)] = Number(aid)
      }
    } else if (resultIdRef.value) {
      // Fallback: localStorage theo resultId (giống QuizResult.vue)
      try {
        const raw = localStorage.getItem(`result_selected_${resultIdRef.value}`)
        if (raw) {
          const parsed = JSON.parse(raw)
          if (Array.isArray(parsed)) {
            for (const item of parsed) {
              const qid = item?.questionId ?? item?.question_id ?? item?.question?.id ?? item?.qid
              const aid = item?.answerId ?? item?.answer_id ?? item?.answer?.id ?? item?.selectedAnswerId
              if (qid != null && aid != null) sMap[Number(qid)] = Number(aid)
            }
          } else if (parsed && typeof parsed === 'object') {
            for (const [qid, val] of Object.entries(parsed)) {
              if (val == null) continue
              if (typeof val === 'number') sMap[Number(qid)] = Number(val)
              else if (typeof val === 'object' && val.id != null) sMap[Number(qid)] = Number(val.id)
              else {
                const n = parseInt(val, 10)
                if (!Number.isNaN(n)) sMap[Number(qid)] = n
              }
            }
          }
        }
      } catch {}
    }
    // Sanity: nếu không xác định được selected, đánh dấu null thay vì undefined
    correctIdByQuestion.value = cMap
    selectedIdByQuestion.value = sMap
  } finally {
    loading.value = false
  }
})

const isCorrect = (qid) => {
  const cId = Number(correctIdByQuestion.value[qid])
  const sId = Number(selectedIdByQuestion.value[qid])
  return cId != null && sId != null && cId === sId
}

const formatTime = (seconds) => {
  const m = Math.floor((seconds || 0) / 60)
  const s = (seconds || 0) % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}

const formatDateTime = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('vi-VN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.review-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.5); display:flex; align-items:center; justify-content:center; z-index: 99999; padding: 12px; }
.review-modal { width: 100%; max-width: 960px; background: var(--rv-bg, #fff); color: var(--rv-text, #111827); border-radius: 16px; overflow: hidden; box-shadow: 0 30px 80px rgba(0,0,0,.3); }
.modal-header { display:flex; align-items:center; justify-content:space-between; padding: 12px 16px; border-bottom: 1px solid var(--rv-border, #eee); background: var(--rv-header-bg, #f8fafc); }
.title { font-weight: 800; display:flex; gap:8px; align-items:center; }
.close-btn { background: transparent; border: none; cursor: pointer; color: var(--rv-muted, #555); }
.modal-body { max-height: 70vh; overflow: auto; padding: 16px; }
.summary { margin-bottom: 12px; }
.quiz-title { margin: 0 0 6px 0; font-size: 18px; font-weight: 800; }
.meta { display:flex; gap: 12px; color: var(--rv-muted-text, #666); font-size: 13px; flex-wrap: wrap; }
.legend { display:flex; gap: 12px; margin-top: 8px; flex-wrap: wrap; font-size: 12px; }
.legend-item { padding: 4px 8px; border-radius: 8px; background: var(--rv-chip-bg, #f1f5f9); color: var(--rv-chip-text, #334155); display:flex; align-items:center; gap:6px; }
.legend-item.right { background:#ecfdf5; color:#16a34a; }
.legend-item.chosen { background:#eef2ff; color:#6366f1; }
.legend-item.wrong { background:#fee2e2; color:#ef4444; }
.questions { display:flex; flex-direction: column; gap: 12px; }
.question-item { padding: 12px; border: 1px solid var(--rv-border, #eee); border-radius: 10px; background: var(--rv-card-bg, #fafafa); }
.q-head { display:flex; align-items: center; gap: 10px; }
.q-number { width: 28px; height: 28px; border-radius: 50%; background: #667eea; color: #fff; display:flex; align-items:center; justify-content:center; font-weight: 700; }
.q-content { flex: 1; font-weight: 600; }
.q-status { width: 26px; height: 26px; border-radius: 50%; display:flex; align-items:center; justify-content:center; }
.q-status.correct { background: #22c55e; color:#fff; }
.q-status.incorrect { background: #ef4444; color:#fff; }
.answers { list-style: none; padding-left: 0; margin: 10px 0 0 0; display:flex; flex-direction: column; gap: 8px; }
.answers li { display:flex; align-items:center; gap:10px; padding:8px 10px; border-radius: 8px; border:1px solid var(--rv-border, #e5e7eb); background: var(--rv-card-bg, #fff); position: relative; }
.answers li.correct { border-color: #16a34a; background: rgba(22,163,74,.08); }
.answers li.selected-right { box-shadow: inset 0 0 0 2px rgba(22,163,74,.4); }
.answers li.selected-wrong { box-shadow: inset 0 0 0 2px rgba(220,38,38,.35); background: rgba(220,38,38,.06); }
.answers .badge { width: 24px; height: 24px; border-radius: 6px; background: #667eea; color:#fff; display:flex; align-items:center; justify-content:center; font-size: 12px; font-weight: 700; }
.answers .content { flex:1; }
.answers .icon { width: 20px; height:20px; display:flex; align-items:center; justify-content:center; color:#16a34a; }
.answers .icon.wrong { color:#dc2626; }
.loading { padding: 24px; text-align: center; }
.modal-footer { display:flex; justify-content:flex-end; padding: 12px 16px; border-top: 1px solid var(--rv-border, #eee); background: var(--rv-header-bg, #f8fafc); }
.modal-footer .primary { border: none; border-radius: 10px; background: #667eea; color:#fff; padding: 10px 14px; font-weight: 700; cursor: pointer; }

/* Dark theme variables */
:root.dark .review-modal, .dark .review-modal { --rv-bg:#1f2937; --rv-text:#e5e7eb; --rv-border:#374151; --rv-header-bg:#111827; --rv-muted:#9ca3af; --rv-muted-text:#9ca3af; --rv-chip-bg:#111827; --rv-chip-text:#e5e7eb; --rv-card-bg:#111827; }
</style>


