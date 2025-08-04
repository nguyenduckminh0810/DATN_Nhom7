<template>
  <div class="contact-container">
    <div class="contact-content">
      <div class="contact-header">
        <h1>Liên hệ</h1>
        <p>Chúng tôi luôn sẵn sàng hỗ trợ bạn</p>
      </div>
      
      <div class="contact-info">
        <div class="contact-item">
          <i class="bi bi-envelope"></i>
          <div>
            <h3>Email</h3>
            <p>support@quizmaster.com</p>
          </div>
        </div>
        
        <div class="contact-item">
          <i class="bi bi-telephone"></i>
          <div>
            <h3>Điện thoại</h3>
            <p>+84 123 456 789</p>
          </div>
        </div>
        
        <div class="contact-item">
          <i class="bi bi-geo-alt"></i>
          <div>
            <h3>Địa chỉ</h3>
            <p>123 Đường ABC, Quận 1, TP.HCM</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// Component logic here
</script>

<style scoped>
.contact-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.contact-content {
  background: white;
  border-radius: 20px;
  padding: 40px;
  max-width: 600px;
  width: 100%;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.contact-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 10px;
}

.contact-header p {
  font-size: 1.1rem;
  color: #666;
  margin-bottom: 40px;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 15px;
  text-align: left;
}

.contact-item i {
  font-size: 2rem;
  color: #667eea;
  width: 50px;
  text-align: center;
}

.contact-item h3 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.contact-item p {
  color: #666;
  margin: 0;
}

@media (max-width: 768px) {
  .contact-content {
    padding: 30px 20px;
  }
  
  .contact-header h1 {
    font-size: 2rem;
  }
}
</style> 