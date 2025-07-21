<template>
  <footer class="admin-footer text-light py-3 mt-auto">
    <div class="container d-flex flex-column flex-md-row justify-content-between align-items-center">
      <div class="d-flex align-items-center mb-2 mb-md-0">
        <img src="/img/logo.png" alt="QuizMaster Logo" style="height: 32px; margin-right: 10px;" />
        <span class="fw-bold h5 mb-0">QuizMaster Admin</span>
      </div>
      <div class="small">
        © 2024 <strong>QuizMaster</strong> Admin. All Rights Reserved.
      </div>
      <div>
        <a href="https://facebook.com" class="btn btn-outline-light btn-sm rounded-circle me-1"><i class="bi bi-facebook"></i></a>
        <a href="https://linkedin.com" class="btn btn-outline-light btn-sm rounded-circle me-1"><i class="bi bi-linkedin"></i></a>
        <a href="https://github.com" class="btn btn-outline-light btn-sm rounded-circle"><i class="bi bi-github"></i></a>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.admin-footer {
  background: linear-gradient(90deg, #0072ff 0%, #00c6ff 100%);
  color: #fff;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.04);
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
}
.admin-footer a:hover {
  color: #ffc107 !important;
  text-decoration: underline;
}
</style>
