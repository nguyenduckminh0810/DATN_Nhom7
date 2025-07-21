<template>
  <div class="container my-4">
    <h4 class="mb-4">Danh sách Quiz</h4>
    <button class="btn btn-primary btn-sm" @click="addNewQuiz">Thêm Quiz</button>
    <hr>
    <!-- Tìm kiếm và lọc -->
    <div class="row mb-3">
      <div class="col-md-4">
        <input v-model="search" @keyup.enter="fetchQuizzes" class="form-control" placeholder="Tìm kiếm tiêu đề quiz..." />
      </div>
      <div class="col-md-3">
        <select v-model="isPublic" @change="fetchQuizzes" class="form-select">
          <option value="">Tất cả trạng thái</option>
          <option :value="true">Công khai</option>
          <option :value="false">Riêng tư</option>
        </select>
      </div>
      <div class="col-md-3">
        <select v-model="selectedTagId" @change="fetchQuizzes" class="form-select">
          <option value="">Tất cả chủ đề</option>
          <option v-for="tag in tags" :key="tag.id" :value="tag.id">
            {{ tag.name }}
          </option>
        </select>
      </div>

    </div>

    <!-- Bảng -->
    <div class="table-responsive">
      <table class="table align-middle table-hover table-bordered shadow-sm">
        <thead class="table-light">
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
            <td>{{ index + 1 + currentPage * pageSize }}</td>
            <td>{{ quiz.title }}</td>
            <td>{{ quiz.categoryName }}</td>
            <td>{{ quiz.creatorName }}</td>
            <td>
              <span :class="['badge', quiz.public ? 'bg-success' : 'bg-secondary']">
                {{ quiz.public ? 'Công khai' : 'Riêng tư' }}
              </span>
            </td>
            <td>{{ formatDate(quiz.createdAt) }}</td>
            <td>
              <button class="btn btn-sm btn-outline-primary" @click="editQuiz(quiz)">Sửa</button>
              <button class="btn btn-sm btn-outline-danger ms-1" @click="deleteQuiz(quiz.id)">Xoá</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Phân trang -->
    <nav v-if="totalPages > 1" class="mt-3">
      <ul class="pagination">
        <li class="page-item" :class="{ disabled: currentPage === 0 }">
          <button class="page-link" @click="prevPage">«</button>
        </li>
        <li
          v-for="n in totalPages"
          :key="n"
          class="page-item"
          :class="{ active: currentPage === n - 1 }"
        >
          <button class="page-link" @click="goToPage(n - 1)">{{ n }}</button>
        </li>
        <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
          <button class="page-link" @click="nextPage">»</button>
        </li>
      </ul>
    </nav>

    <!-- Modal Sửa Quiz -->
    <div class="modal fade show" tabindex="-1" style="display: block;" v-if="showEditModal">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ selectedQuiz.id === null ? 'Thêm Quiz mới' : 'Sửa Quiz' }}
            </h5>
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
              <input v-model="selectedQuiz.creatorName" class="form-control" />
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
import { ref, onMounted } from 'vue';
import axios from 'axios';

const quizzes = ref([]);
const search = ref('');
const isPublic = ref(''); // true / false / ''
const currentPage = ref(0);
const totalPages = ref(1);
const pageSize = 5;
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
    const response = await axios.get('/api/admin/categories');
    categories.value = response.data;
  } catch (error) {
    console.error('Lỗi khi tải thể loại:', error);
  }
}

async function fetchTags() {
  try {
    const response = await axios.get('/api/admin/tags'); // endpoint em viết
    tags.value = response.data;
  } catch (error) {
    console.error('Lỗi khi tải tag:', error);
  }
}

async function fetchQuizzes() {
  try {
    const response = await axios.get('/api/admin/all-quizzes/filter', {
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

function addNewQuiz() {
  selectedQuiz.value = {
    id: null,
    title: '',
    categoryName: '',
    isPublic: true
  };
  showEditModal.value = true;
}

async function saveQuiz() {
  try {
    if (selectedQuiz.value.id === null) {
      // ✅ Thêm mới
      await axios.post('/api/admin/quizzes', {
        title: selectedQuiz.value.title,
        categoryName: selectedQuiz.value.categoryName,
        isPublic: selectedQuiz.value.isPublic,
        categoryId: selectedQuiz.value.categoryId,
      });
      alert('Thêm quiz thành công!');
    } else {
      // ✅ Sửa
      await axios.put(`/api/admin/quizzes/${selectedQuiz.value.id}`, {
        id: selectedQuiz.value.id,
        title: selectedQuiz.value.title,
        categoryName: selectedQuiz.value.categoryName,
        isPublic: selectedQuiz.value.isPublic,
        categoryId: selectedQuiz.value.categoryId
      });
      alert('Cập nhật quiz thành công!'); 
    }

    showEditModal.value = false;
    fetchQuizzes();
  } catch (error) {
    console.error('Lỗi khi lưu quiz:', error);
  }
}

async function deleteQuiz(id) {
  if (confirm('Bạn có chắc muốn xoá quiz này không?')) {
    try {
      await axios.delete(`/api/admin/quizzes/${id}`);
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
