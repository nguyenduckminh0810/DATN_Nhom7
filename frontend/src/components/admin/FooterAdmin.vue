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
  background: var(--bg-secondary);
  color: var(--text-primary);
  box-shadow: 0 -4px 20px var(--shadow-color);
  border-top-left-radius: 12px;
  border-top-right-radius: 12px;
}
.admin-footer a:hover {
  color: var(--warning-color) !important;
  text-decoration: underline;
  transform: translateY(-2px);
  transition: all 0.3s ease;
}

/* Enhanced styling for admin footer */
.admin-footer .btn-outline-light {
  border-color: var(--border-color);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.admin-footer .btn-outline-light:hover {
  background: var(--card-header-bg);
  border-color: var(--border-color);
  transform: translateY(-2px);
}
</style>
