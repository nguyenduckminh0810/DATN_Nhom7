<template>
  <footer class="footer text-light pt-3 pb-2">
    <div class="container">
      <div class="row gy-3">
        <!-- Logo & Contact -->
        <div class="col-lg-4 col-md-6 mb-3 brand-col">
          <div class="d-flex align-items-center mb-2 brand-block">
            <img
              src="/img/logo.png"
              alt="QuizMaster Logo"
              style="height: 32px; margin-right: 8px"
            />
            <span class="h5 mb-0 fw-bold">QuizMaster</span>
          </div>
          <p class="mb-1 small">
            <i class="bi bi-geo-alt-fill me-2"></i>V116 Cát Bi, Phường Hải An, TP Hải Phòng
          </p>
          <p class="mb-1 small"><i class="bi bi-envelope-fill me-2"></i>quizmaster@example.com</p>
          <p class="mb-2 small"><i class="bi bi-telephone-fill me-2"></i>+84 123 456 789</p>
          <div class="d-flex gap-2">
            <a href="#" class="btn btn-outline-light btn-sm rounded-circle"
              ><i class="bi bi-facebook"></i
            ></a>
            <a href="#" class="btn btn-outline-light btn-sm rounded-circle"
              ><i class="bi bi-instagram"></i
            ></a>
            <a href="#" class="btn btn-outline-light btn-sm rounded-circle"
              ><i class="bi bi-linkedin"></i
            ></a>
          </div>
        </div>

        <!-- Quick Links -->
        <div class="col-lg-3 col-md-4 mb-3 links-col">
          <h6 class="section-title fw-bold">Liên kết nhanh</h6>
          <ul class="list-unstyled">
            <li>
              <router-link
                :to="{ name: 'Dashboard' }"
                class="text-light text-decoration-none small"
              >
                Trang chủ
              </router-link>
            </li>
            <li>
              <router-link
                :to="{ name: 'ListQuizPublic' }"
                class="text-light text-decoration-none small"
              >
                Quiz công khai
              </router-link>
            </li>
            <li>
              <router-link :to="{ name: 'Contact' }" class="text-light text-decoration-none small">
                Liên hệ
              </router-link>
            </li>
          </ul>
        </div>

        <!-- About Project -->
        <div class="col-lg-5 col-md-5 mb-3 about-col">
          <h6 class="section-title fw-bold">Về QuizMaster</h6>
          <p class="small">
            QuizMaster là nền tảng thi trắc nghiệm trực tuyến, giúp bạn tạo, chia sẻ và kiểm tra
            kiến thức dễ dàng, nhanh chóng và hiệu quả.
          </p>
        </div>
      </div>

      <hr class="bg-secondary my-3" />
      <div class="text-center small">© 2025 <strong>QuizMaster</strong>. All Rights Reserved.</div>
    </div>
  </footer>
</template>

<script setup></script>

<style scoped>
/* === Footer – Modern Clean Glass === */
.footer {
  font-size: 0.95rem;
  color: #fff;
  margin: 0;
  width: 100%;
  background:
    radial-gradient(1200px 600px at 20% -10%, rgba(255, 255, 255, 0.06), transparent 60%),
    radial-gradient(1000px 500px at 120% 10%, rgba(0, 212, 255, 0.08), transparent 55%),
    var(--app-background, #0f1220);
  position: relative;
  box-shadow: 0 -8px 24px rgba(0, 0, 0, 0.35);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: saturate(120%) blur(6px);
}

.footer::before {
  content: '';
  position: absolute;
  inset: 0 0 auto 0;
  height: 2px;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(0, 212, 255, 0.6),
    rgba(95, 39, 205, 0.6),
    transparent
  );
  opacity: 0.35;
}

/* Container spacing */
.footer .container {
  padding-top: 18px;
  padding-bottom: 12px;
}

/* Row & columns */
.footer .row {
  align-items: flex-start;
}
.footer .row > [class*='col-'] {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}
.footer .col-lg-4,
.footer .col-lg-3,
.footer .col-lg-5 {
  margin-bottom: 6px;
}

/* --- Fix căn hàng: đưa logo ra khỏi flow để địa chỉ lên cao (>=992px) --- */
@media (min-width: 992px) {
  .brand-col {
    position: relative;
    padding-top: 44px;
  } /* chừa chỗ cho logo */
  .brand-block {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    margin: 0; /* bỏ mb-2 của bootstrap */
  }
}
/* Mobile/Tablet: giữ layout mặc định */
@media (max-width: 991.98px) {
  .brand-block {
    position: static;
  }
}

/* Logo shadow */
.footer img {
  filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.35));
}

/* Section title & khoảng cách với nội dung */
.section-title {
  color: #e9edf6;
  letter-spacing: 0.3px;
  text-transform: none;
  line-height: 1.2;
}
.section-title + .list-unstyled,
.section-title + p {
  margin-top: 0;
}

/* Văn bản phụ */
.footer p.small,
.footer .small {
  color: rgba(255, 255, 255, 0.88);
}

/* Links – underline trượt */
.footer a {
  color: rgba(255, 255, 255, 0.92) !important;
  text-decoration: none;
  position: relative;
  transition: color 0.2s ease;
}
.footer a::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -2px;
  width: 0;
  height: 1px;
  background: currentColor;
  opacity: 0.7;
  transition: width 0.22s ease;
}
.footer a:hover {
  color: #9fdcff !important;
}
.footer a:hover::after {
  width: 100%;
}

/* Danh sách link */
.footer .list-unstyled {
  margin: 0;
  padding: 0;
}
.footer .list-unstyled li + li {
  margin-top: 6px;
}

/* Social buttons */
.footer .btn-outline-light {
  --ring: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.28);
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(10px);
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    background 0.18s ease,
    border-color 0.18s ease;
  width: 36px;
  height: 36px;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  box-shadow: 0 0 0 0 var(--ring);
}
.footer .btn-outline-light:hover {
  background: rgba(255, 255, 255, 0.14);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
  box-shadow:
    0 6px 18px -6px rgba(0, 212, 255, 0.35),
    0 0 0 4px rgba(255, 255, 255, 0.08);
}

/* Hairline & bản quyền */
.footer hr {
  border: none;
  height: 1px;
  margin: 14px 0 10px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.28), transparent);
}
.footer .text-center.small {
  color: rgba(255, 255, 255, 0.75);
  letter-spacing: 0.2px;
}

/* Responsive tweaks */
@media (max-width: 992px) {
  .footer .container {
    padding-top: 16px;
    padding-bottom: 10px;
  }
}
@media (max-width: 576px) {
  .footer .container {
    padding-top: 14px;
    padding-bottom: 8px;
  }
  .footer .row > [class*='col-'] + [class*='col-'] {
    margin-top: 6px;
  }
  .footer .btn-outline-light {
    width: 34px;
    height: 34px;
  }
}
</style>
