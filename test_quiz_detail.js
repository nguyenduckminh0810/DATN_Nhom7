// Script test quiz detail với creatorId
const axios = require('axios');

const BASE_URL = 'http://localhost:8080/api';

async function testQuizDetail() {
  console.log('🧪 Testing Quiz Detail with Creator ID...\n');

  try {
    // 1. Test login
    console.log('1️⃣ Testing login...');
    const loginResponse = await axios.post(`${BASE_URL}/login`, {
      username: 'admin', // Thay bằng username thực tế
      password: 'admin123' // Thay bằng password thực tế
    });
    
    console.log('✅ Login successful');
    const token = loginResponse.data.token;
    console.log('Token:', token.substring(0, 20) + '...\n');

    // 2. Test get quiz detail
    console.log('2️⃣ Testing get quiz detail...');
    try {
      const detailResponse = await axios.get(`${BASE_URL}/quiz/detail/1`, { // Thay 1 bằng quiz ID thực tế
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      console.log('✅ Quiz detail response:');
      console.log('  - ID:', detailResponse.data.id);
      console.log('  - Title:', detailResponse.data.title);
      console.log('  - Creator Name:', detailResponse.data.creatorName);
      console.log('  - Creator ID:', detailResponse.data.creatorId);
      console.log('  - Is Public:', detailResponse.data.isPublic);
      
      // Kiểm tra xem có creatorId không
      if (detailResponse.data.creatorId) {
        console.log('✅ Creator ID is present:', detailResponse.data.creatorId);
      } else {
        console.log('❌ Creator ID is missing!');
      }
      
    } catch (error) {
      console.log('❌ Quiz detail error:', error.response?.status);
      console.log('Error details:', error.response?.data);
    }

  } catch (error) {
    console.error('❌ Test failed:', error.response?.data || error.message);
  }
}

// Chạy test
testQuizDetail(); 