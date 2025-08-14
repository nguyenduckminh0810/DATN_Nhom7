<template>
  <div class="admin-quizzes" :class="{ 'is-light': !isDarkMode, 'is-dark': isDarkMode }">
    <div class="page-header">
      <div class="title-card">
        <div class="page-title">
          <div class="icon-badge"><i class="bi bi-journal-code"></i></div>
          <div class="title-text">
            <h1>Danh sách Quiz</h1>
            <p>Quản lý quiz, trạng thái và phân loại</p>
          </div>
        </div>
      </div>
      <div class="page-actions">
        <div class="search-group">
          <i class="bi bi-search"></i>
          <input v-model="search" @keyup.enter="fetchQuizzes" placeholder="Tìm kiếm tiêu đề quiz..." />
        </div>
        <select v-model="isPublic" @change="fetchQuizzes" class="filter-select">
          <option value="">Tất cả trạng thái</option>
          <option :value="true">Công khai</option>
          <option :value="false">Riêng tư</option>
        </select>
        <select v-model="selectedTagId" @change="fetchQuizzes" class="filter-select">
          <option value="">Tất cả chủ đề</option>
          <option v-for="tag in tags" :key="tag.id" :value="tag.id">{{ tag.name }}</option>
        </select>
        <button class="btn btn-outline" @click="fetchQuizzes"><i class="bi bi-arrow-clockwise"></i> Làm mới</button>
      </div>
    </div>

    <div class="panel">
      <div class="panel-body no-padding">
        <div class="table-wrap">
          <table class="modern-table">
            <thead>
              <tr>
                <th>STT</th>
                <th>Tiêu đề</th>
                <th>Thể loại</th>
                <th>Người tạo</th>
                <th>Trạng thái</th>
                <th>Ngày tạo</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(quiz, index) in quizzes" :key="quiz.id">
                <td class="muted">{{ index + 1 + currentPage * pageSize }}</td>
                <td class="cell-title">{{ quiz.title }}</td>
                <td>{{ quiz.categoryName }}</td>
                <td class="muted">{{ quiz.creatorName }}</td>
                <td>
                  <span class="role-pill" :class="quiz.public ? 'success' : 'secondary'">{{ quiz.public ? 'Công khai' : 'Riêng tư' }}</span>
                </td>
                <td class="muted">{{ formatDate(quiz.createdAt) }}</td>
                <td>
                  <div class="row-actions">
                    <button class="chip primary" @click="editQuiz(quiz)"><i class="bi bi-pencil"></i> Sửa</button>
                    <button class="chip danger" @click="deleteQuiz(quiz.id)"><i class="bi bi-trash"></i> Xoá</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="totalPages > 1" class="pagination-bar">
        <button class="pg-btn" :disabled="currentPage === 0" @click="prevPage"><i class="bi bi-chevron-left"></i></button>
        <span>Trang {{ currentPage + 1 }} / {{ totalPages }}</span>
        <button class="pg-btn" :disabled="currentPage >= totalPages - 1" @click="nextPage"><i class="bi bi-chevron-right"></i></button>
      </div>
    </div>

    <!-- Modal Sửa Quiz -->
    <div class="modal fade show" tabindex="-1" style="display: block;" v-if="showEditModal">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Sửa Quiz</h5>
            <button type="button" class="btn-close" @click="showEditModal = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">Tiêu đề</label>
              <input v-model="selectedQuiz.title" class="form-control" />
            </div>
            <div class="mb-3">
              <label class="form-label">Thể loại</label>
              <select v-model="selectedQuiz.categoryId" class="form-select">
                <option v-for="c in categories" :key="c.id" :value="c.id">
                  {{ c.name }}
                </option>
              </select>
            </div>
            <div class="mb-3">
              <label class="form-label">Người tạo</label>
              <input v-model="selectedQuiz.creatorName" class="form-control" disabled/>
            </div>
            <div class="mb-3">
              <label class="form-label">Trạng thái</label>
              <select v-model="selectedQuiz.isPublic" class="form-select">
                <option :value="true">Công khai</option>
                <option :value="false">Riêng tư</option>
              </select>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-primary" @click="saveQuiz">Lưu</button>
            <button class="btn btn-secondary" @click="showEditModal = false">Đóng</button>
          </div>
        </div>
      </div>
    </div>
    <div class="modal-backdrop fade show" v-if="showEditModal"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { storeToRefs } from 'pinia'
import api from '@/utils/axios'
const themeStore = useThemeStore()
const { isDarkMode } = storeToRefs(themeStore)

const quizzes = ref([]);
const search = ref('');
const isPublic = ref('');
const currentPage = ref(0);
const totalPages = ref(1);
const pageSize = 10;
const categories = ref([]);
const selectedTagId = ref('');
const tags = ref([]);

const showEditModal = ref(false);
const selectedQuiz = ref({ id: null, title: '', categoryName: '', creatorName: "", isPublic: true });

function formatDate(dateString) {
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN');
}

async function fetchCategories() {
  try {
    const response = await api.get('/admin/categories');
    categories.value = response.data;
  } catch (error) {
    console.error('Lỗi khi tải thể loại:', error);
  }
}

async function fetchTags() {
  try {
    const response = await api.get('/admin/tags');
    tags.value = response.data;
  } catch (error) {
    console.error('Lỗi khi tải tag:', error);
  }
}

async function fetchQuizzes() {
  try {
    const response = await api.get('/admin/all-quizzes/filter', {
      params: {
        keyword: search.value,
        tagId: selectedTagId.value === '' ? null : selectedTagId.value,
        isPublic: isPublic.value === '' ? null : isPublic.value,
        page: currentPage.value,
        size: pageSize
      }
    });

    quizzes.value = response.data.content;
    totalPages.value = response.data.totalPages;
  } catch (error) {
    console.error('Lỗi khi tải quiz:', error);
  }
}

function prevPage() {
  if (currentPage.value > 0) {
    currentPage.value--;
    fetchQuizzes();
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++;
    fetchQuizzes();
  }
}

function goToPage(page) {
  currentPage.value = page;
  fetchQuizzes();
}

function editQuiz(quiz) {
  const category = categories.value.find(c => c.name === quiz.categoryName);

  selectedQuiz.value = {
    id: quiz.id,
    title: quiz.title,
    isPublic: quiz.public ?? quiz.isPublic,
    categoryName: quiz.categoryName,
    categoryId: category ? category.id : null
  };
  showEditModal.value = true;
}

async function saveQuiz() {
  try {
    await api.put(`/admin/quizzes/${selectedQuiz.value.id}`, {
      id: selectedQuiz.value.id,
      title: selectedQuiz.value.title,
      categoryName: selectedQuiz.value.categoryName,
      isPublic: selectedQuiz.value.isPublic,
      categoryId: selectedQuiz.value.categoryId
    });
    alert('Cập nhật quiz thành công!');
    showEditModal.value = false;
    fetchQuizzes();
  } catch (error) {
    console.error('Lỗi khi lưu quiz:', error);
  }
}

async function deleteQuiz(id) {
  if (confirm('Bạn có chắc muốn xoá quiz này không?')) {
    try {
      await api.delete(`/admin/quizzes/${id}`);
      alert('Xoá quiz thành công!');
      fetchQuizzes();
    } catch (error) {
      console.error('Lỗi khi xoá quiz:', error);
    }
  }
}

onMounted(() => {
  fetchQuizzes();
  fetchCategories();
  fetchTags();
});
</script>

<style scoped>
.admin-quizzes { padding: 24px; color: var(--text-primary); }
.page-header { display:flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.title-card { background: var(--bg-primary); border:1px solid var(--border-color); border-radius: 14px; padding: 10px 14px; box-shadow: 0 6px 18px var(--shadow-color); }
.page-title { display:flex; align-items:center; gap:12px; }
.icon-badge { width:44px; height:44px; display:flex; align-items:center; justify-content:center; border-radius:12px; }
.icon-badge i { font-size:22px; color: var(--info-color); }
.title-text h1 { font-size:22px; margin:0; text-shadow: none; }
.title-text h1::after { content:''; display:block; height:3px; width:80px; margin-top:6px; border-radius:999px; background:linear-gradient(90deg,#667eea 0%, #764ba2 100%); opacity:.6; }
.title-text p { margin:2px 0 0 0; font-size:13px; color: var(--text-secondary); }
.page-actions { display:flex; align-items:center; gap:10px; }
.search-group { display:flex; align-items:center; gap:8px; background: var(--bg-primary); border:1px solid var(--border-color); padding:8px 12px; border-radius:10px; }
.search-group input { border:0; outline:none; background:transparent; color: var(--text-primary); width: 220px; }
.filter-select { background: var(--bg-primary); color: var(--text-primary); border:1px solid var(--border-color); border-radius:10px; padding:8px 12px; }
.btn.btn-outline { background: var(--bg-primary); color: var(--text-primary); border:1px solid var(--border-color); border-radius:10px; padding:8px 12px; }

.panel { background: var(--bg-primary); border:1px solid var(--border-color); border-radius: 16px; box-shadow: 0 8px 22px var(--shadow-color); overflow:hidden; }
.panel-body.no-padding { padding: 0; }
.table-wrap { width: 100%; overflow-x: auto; }
.modern-table { width: 100%; border-collapse: collapse; }
.modern-table thead th { text-align:left; padding: 14px 16px; background: var(--card-header-bg); color: var(--card-header-text); font-weight: 700; font-size: 13px; }
.modern-table tbody td { padding: 12px 16px; border-top:1px solid var(--border-color); font-size: 14px; }
.modern-table tbody tr:hover { background: rgba(102,126,234,0.06); }
.muted { color: var(--text-muted); }
.cell-title { font-weight: 600; }
.role-pill { display:inline-flex; align-items:center; padding: 4px 10px; border-radius: 999px; font-weight:700; font-size: 12px; }
.role-pill.success { background: #16a34a; color:#fff; }
.role-pill.secondary { background: #6b7280; color:#fff; }
.row-actions { display:flex; gap:8px; }
.chip { display:inline-flex; align-items:center; gap:6px; padding:6px 10px; border-radius:10px; border:1px solid var(--border-color); background: var(--bg-primary); color: var(--text-primary); cursor:pointer; }
.chip.primary { border-color:#93c5fd; }
.chip.danger { border-color:#fda4af; }

.pagination-bar { display:flex; align-items:center; justify-content:flex-end; gap:8px; padding: 10px 12px; border-top:1px solid var(--border-color); }
.pg-btn { border:1px solid var(--border-color); background: var(--bg-primary); color: var(--text-primary); border-radius:8px; width:32px; height:28px; display:flex; align-items:center; justify-content:center; }

/* Dark/Light tweaks */
.admin-quizzes.is-light .title-card { background:#ffffff; border-color: rgba(2,6,23,0.12); }
.admin-quizzes.is-dark .title-card { background: transparent; border-color: rgba(255,255,255,0.18); box-shadow: none; }
.admin-quizzes.is-dark .page-title { background: transparent; }
.admin-quizzes.is-light .title-text h1 { color:#0b1220; }
.admin-quizzes.is-dark .title-text h1 { color:#f1f5f9; }
.admin-quizzes.is-light .title-text h1::after { opacity:.95; background:linear-gradient(90deg,#4338ca 0%, #7c3aed 100%); }
.admin-quizzes.is-dark .title-text h1::after { opacity:.7; }

@media (max-width: 900px) {
  .search-group input { width: 140px; }
}

/* Improve contrast for light mode on subtle text (creator, date) */
.admin-quizzes.is-light .muted { color: #374151; }
</style>