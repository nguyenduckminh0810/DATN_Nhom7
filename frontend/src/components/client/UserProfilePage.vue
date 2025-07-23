<template>
  <div class="profile-page" style="background: linear-gradient(to bottom right, #e0f0ff, #ffffff); min-height: 100vh; padding: 2rem;">
    <div class="profile-card mx-auto p-4 rounded shadow" style="max-width: 800px; background-color: #ffffff;">
      <div class="text-center mb-4">
        <img
          :src="(baseUrl + user.avatarUrl || defaultAvatar) + '?t=' + Date.now()" alt="Avatar"
          class="rounded-circle shadow border border-white"
          style="width: 120px; height: 120px; object-fit: cover;"
        />
        <h3 class="mt-3">{{ user.fullName }}</h3>
        <p class="text-muted mb-1">{{ user.bio || "Chưa có giới thiệu" }}</p>
        <span class="badge bg-success">USER</span>
      </div>

      <!-- Tabs -->
      <ul class="nav nav-tabs justify-content-center mb-3">
        <li class="nav-item">
          <a class="nav-link" :class="{ active: activeTab === 'view' }" @click="activeTab = 'view'" href="#">
            <i class="bi bi-person-circle me-1"></i> Hồ sơ
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link" :class="{ active: activeTab === 'edit' }" @click="activeTab = 'edit'" href="#">
            <i class="bi bi-pencil-square me-1"></i> Cập nhật
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link" :class="{ active: activeTab === 'quiz' }" @click="activeTab = 'quiz'" href="#">
            <i class="bi bi-clock-history me-1"></i> Lịch sử Quiz
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link" :class="{ active: activeTab === 'editpassword' }" @click="activeTab = 'editpassword'" href="#">
            <i class="bi bi-clock-history me-1"></i> Đổi mật khẩu
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link" :class="{ active: activeTab === 'delete' }" @click="activeTab = 'delete'" href="#">
            <i class="bi bi-trash3 me-1 text-danger"></i> Xoá tài khoản
          </a>
        </li>
      </ul>

      <!-- Hồ sơ -->
      <div v-if="activeTab === 'view'" class="mt-3">
        <ul class="list-group">
          <li class="list-group-item"><strong>Email:</strong> {{ user.email }}</li>
          <li class="list-group-item"><strong>Tên tài khoản:</strong> {{ user.username }}</li>
          <li class="list-group-item"><strong>Tên người dùng:</strong> {{ user.fullName }}</li>
          <li class="list-group-item"><strong>Giới thiệu:</strong> {{ user.bio }}</li>
          <li class="list-group-item"><strong>Ngày tham gia:</strong> {{ user.createAt }}</li>
        </ul>
      </div>

      <!-- Cập nhật hồ sơ -->
      <div v-if="activeTab === 'edit'" class="mt-3">
        <form @submit.prevent="updateProfile">
          <div class="mb-3">
            <label class="form-label"><strong>Họ tên</strong></label>
            <input type="text" v-model="form.fullName" class="form-control" />
          </div>
          <div class="mb-3">
            <label class="form-label"><strong>Tài khoản</strong></label>
            <input type="text" v-model="form.username" class="form-control" />
          </div>
          <div class="mb-3">
            <label class="form-label"><strong>Giới thiệu</strong></label>
            <textarea v-model="form.bio" class="form-control" placeholder="Viết vài dòng về bạn..."></textarea>
          </div>
          <div class="mb-3">
            <label class="form-label"><strong>Ảnh đại diện</strong></label>
            <input type="file" @change="handleFileUpload" class="form-control" accept="image/*" />
          </div>
          <div class="d-flex justify-content-end">
            <button class="btn btn-primary me-2" type="submit">Lưu</button>
            <button class="btn btn-secondary" @click="activeTab = 'view'" type="button">Huỷ</button>
          </div>
        </form>
      </div>
      <!-- Lịch sử quiz -->
      <div v-if="activeTab === 'quiz'" class="mt-4">
        <quiz-history :user-id="user.id" />
      </div>

      <!-- Đổi mật khẩu -->
        <div v-if="activeTab === 'editpassword'" class="mt-3">
          <div class="mb-3">
              <label class="form-label"><strong>Mật khẩu hiện tại</strong></label>
              <input type="password" v-model="form.currentPassword" class="form-control" />
          </div>
          <div class="mb-3">
              <label class="form-label"><strong>Mật khẩu mới</strong></label>
              <input type="password" v-model="form.newPassword" class="form-control" />
          </div>
          <div class="mb-3">
              <label class="form-label"><strong>Xác nhận mật khẩu mới</strong></label>
              <input type="password" v-model="form.confirmPassword" class="form-control" />
          </div>
          <div class="d-flex justify-content-end">
            <button class="btn btn-primary me-2" @click="changePassword">Đổi mật khẩu</button>
            <button class="btn btn-secondary" @click="activeTab = 'view'" type="button">Huỷ</button>
          </div>
        </div>
    </div>
      <!-- Xoá tài khoản -->
      <div v-if="activeTab === 'delete'" class="d-flex justify-content-center mt-5">
      <div class="text-center border p-4 rounded shadow-sm" style="max-width: 500px; width: 100%;">
        <h5 class="text-danger">Xoá tài khoản</h5>
        <p>Bạn có chắc chắn muốn xoá tài khoản? Thao tác này không thể hoàn tác.</p>
        <button class="btn btn-danger" @click="confirmDelete">Xoá tài khoản</button>
      </div>
    </div>


    </div>
</template>

<script>
import axios from "axios";
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import QuizHistory from '@/components/client/QuizHistory.vue';

export default {
  name: "UserProfile",
  components: {
    QuizHistory,
  },
  setup() {
    const router = useRouter();
    const route = useRoute();
    const user = ref({});
    const form = ref({
      fullName: "",
      username: "",
      bio: "",
      avatarUrl: "",
      currentPassword: "",
      newPassword: "",
      confirmPassword: ""
    });
    const selectedFile = ref(null);
    const defaultAvatar = "/uploads/images/anh-mo-ta.jpg";
    const activeTab = ref("view");
    const baseUrl = 'http://localhost:8080';
    const handleFileUpload = (e) => {
      selectedFile.value = e.target.files[0];
    };

    const fetchUserProfile = async () => {
      try {
        const userId = route.params.id;
        const res = await axios.get(`http://localhost:8080/api/user/${userId}`);
        user.value = res.data;
        form.value.fullName = res.data.fullName;
        form.value.username = res.data.username;
        form.value.bio = res.data.bio;
        form.value.avatarUrl = res.data.avatarUrl || "";
      } catch {
        alert("Không thể lấy thông tin người dùng.");
        router.push("/");
      }
    };

    const updateProfile = async () => {
    try {
        const userId = route.params.id;
        const formData = new FormData();
        formData.append("fullName", form.value.fullName);
        formData.append("username", form.value.username);
        formData.append("bio", form.value.bio);

        if (selectedFile.value) {
        formData.append("avatar", selectedFile.value);
        }

        const response = await axios.put(`http://localhost:8080/api/user/${userId}`, formData, {
        headers: {
            "Content-Type": "multipart/form-data",
        },
        });

        // Cập nhật giao diện
        user.value.fullName = form.value.fullName;
        user.value.username = form.value.username;
        user.value.bio = form.value.bio;
        if (response.data.avatarUrl) {
        user.value.avatarUrl = response.data.avatarUrl;
        }

        alert("Cập nhật thành công!");
        activeTab.value = "view";
    } catch (err) {
        alert("Có lỗi khi cập nhật: " + (err.response?.data || err.message));
    }
    };

    const changePassword = async () => {
      if (form.value.newPassword !== form.value.confirmPassword) {
        alert("Mật khẩu mới không khớp nhau.");
        return;
      }

      try {
        const userId = route.params.id;
        const payload = {
          currentPassword: form.value.currentPassword,
          newPassword: form.value.newPassword,
        };

        const res = await axios.put(`http://localhost:8080/api/user/${userId}/change-password`, payload);
        alert(res.data || "Đổi mật khẩu thành công!");
        
        // Reset form
        form.value.currentPassword = "";
        form.value.newPassword = "";
        form.value.confirmPassword = "";
      } catch (err) {
        alert("Lỗi khi đổi mật khẩu: " + (err.response?.data || err.message));
      }
    };

    const confirmDelete = async () => {
    const confirm = window.confirm("Bạn thật sự muốn xoá tài khoản?");
    if (!confirm) return;

    try {
      const userId = route.params.id;
      await axios.delete(`http://localhost:8080/api/user/${userId}`);
      alert("Tài khoản đã bị xoá.");

      // Chuyển hướng về trang chủ hoặc đăng xuất
      router.push("/login");
    } catch (err) {
      alert("Lỗi khi xoá tài khoản: " + (err.response?.data || err.message));
    }
  };

    onMounted(async () => {
      await fetchUserProfile();

      // ⚠️ ép gán userId vào route để QuizHistory.vue đọc được
      if (!route.params.userId && user.value.id) {
        route.params.userId = user.value.id; // ép để QuizHistory dùng được
      }
    });

    return {
      user,
      form,
      defaultAvatar,
      activeTab,
      updateProfile,
      confirmDelete,
      handleFileUpload,
      baseUrl,
      changePassword,
    };
  },
};
</script>

<style scoped>
.nav-tabs .nav-link.active {
  font-weight: bold;
  border-bottom: 2px solid #007bff;
}
</style>
