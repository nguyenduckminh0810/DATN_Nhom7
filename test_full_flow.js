// Script test toàn bộ flow
const axios = require('axios');

const BASE_URL = 'http://localhost:8080/api';

async function testFullFlow() {
  console.log('🧪 Testing Full Flow: Login -> Quiz Detail -> Questions...\n');

  try {
    // 1. Test login
    console.log('1️⃣ Testing login...');
    const loginResponse = await axios.post(`${BASE_URL}/login`, {
      username: 'admin', // Thay bằng username thực tế
      password: 'admin123' // Thay bằng password thực tế
    });
    
    console.log('✅ Login successful');
    const token = loginResponse.data.token;
    const user = loginResponse.data.user;
    console.log('Token:', token.substring(0, 20) + '...');
    console.log('User ID:', user?.id);
    console.log('Username:', user?.username);
    console.log('\n');

    // 2. Test get user profile
    console.log('2️⃣ Testing get user profile...');
    try {
      const profileResponse = await axios.get(`${BASE_URL}/user/profile`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      console.log('✅ User profile:');
      console.log('  - ID:', profileResponse.data.id);
      console.log('  - Username:', profileResponse.data.username);
      console.log('  - Full Name:', profileResponse.data.fullName);
      console.log('\n');
      
    } catch (error) {
      console.log('❌ Profile error:', error.response?.status);
    }

    // 3. Test get quiz detail
    console.log('3️⃣ Testing get quiz detail...');
    try {
      const detailResponse = await axios.get(`${BASE_URL}/quiz/detail/1`, { // Thay 1 bằng quiz ID thực tế
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      console.log('✅ Quiz detail:');
      console.log('  - Quiz ID:', detailResponse.data.id);
      console.log('  - Title:', detailResponse.data.title);
      console.log('  - Creator Name:', detailResponse.data.creatorName);
      console.log('  - Creator ID:', detailResponse.data.creatorId);
      console.log('  - Is Public:', detailResponse.data.isPublic);
      console.log('\n');
      
    } catch (error) {
      console.log('❌ Quiz detail error:', error.response?.status);
    }

    // 4. Test get questions (should work if user is creator)
    console.log('4️⃣ Testing get questions...');
    try {
      const questionsResponse = await axios.get(`${BASE_URL}/question/1`, { // Thay 1 bằng quiz ID thực tế
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      console.log('✅ Questions access successful');
      console.log('  - Questions count:', questionsResponse.data.length);
      console.log('\n');
      
    } catch (error) {
      if (error.response?.status === 403) {
        console.log('❌ 403 Forbidden - User is not the quiz creator');
      } else {
        console.log('❌ Questions error:', error.response?.status);
      }
    }

    // 5. Test play endpoint (should work for everyone)
    console.log('5️⃣ Testing play endpoint...');
    try {
      const playResponse = await axios.get(`${BASE_URL}/question/play/1`); // Thay 1 bằng quiz ID thực tế
      
      console.log('✅ Play endpoint accessible');
      console.log('  - Questions count:', playResponse.data.length);
      console.log('\n');
      
    } catch (error) {
      console.log('❌ Play endpoint error:', error.response?.status);
    }

  } catch (error) {
    console.error('❌ Test failed:', error.response?.data || error.message);
  }
}

// Chạy test
testFullFlow(); 