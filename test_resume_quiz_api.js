// ✅ TEST RESUME QUIZ API
// Chạy file này để kiểm tra các API endpoints

const BASE_URL = 'http://localhost:8080/api'
const QUIZ_ID = 1 // Thay đổi theo quiz ID thực tế

// Test data
const testData = {
  questionIndex: 2,
  timeRemaining: 1800,
  answers: {
    1: "A",
    2: "B",
    3: "C"
  }
}

// Helper function để lấy token từ localStorage (nếu test từ browser)
function getAuthToken() {
  if (typeof localStorage !== 'undefined') {
    return localStorage.getItem('token')
  }
  return 'YOUR_JWT_TOKEN_HERE' // Thay thế bằng token thực tế
}

// Test 1: Kiểm tra attempt dở
async function testCheckInProgressAttempt() {
  console.log('🧪 TEST 1: Kiểm tra attempt dở')
  
  try {
    const response = await fetch(`${BASE_URL}/quiz-resume/check/${QUIZ_ID}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${getAuthToken()}`,
        'Content-Type': 'application/json'
      }
    })
    
    const data = await response.json()
    console.log('✅ Response:', data)
    
    if (response.ok) {
      if (data.hasInProgressAttempt) {
        console.log('📋 Có attempt dở:', data.attemptData)
      } else {
        console.log('✅ Không có attempt dở')
      }
    } else {
      console.error('❌ Error:', data)
    }
  } catch (error) {
    console.error('❌ Network error:', error)
  }
}

// Test 2: Tạo attempt mới
async function testCreateNewAttempt() {
  console.log('🧪 TEST 2: Tạo attempt mới')
  
  try {
    const response = await fetch(`${BASE_URL}/quiz-resume/new-attempt/${QUIZ_ID}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${getAuthToken()}`,
        'Content-Type': 'application/json'
      }
    })
    
    const data = await response.json()
    console.log('✅ Response:', data)
    
    if (response.ok) {
      console.log('🆕 Attempt mới được tạo với ID:', data.attemptId)
      return data.attemptId
    } else {
      console.error('❌ Error:', data)
      return null
    }
  } catch (error) {
    console.error('❌ Network error:', error)
    return null
  }
}

// Test 3: Lưu tiến độ
async function testSaveProgress(attemptId) {
  if (!attemptId) {
    console.log('⚠️ Bỏ qua test save progress - không có attempt ID')
    return
  }
  
  console.log('🧪 TEST 3: Lưu tiến độ cho attempt:', attemptId)
  
  try {
    const response = await fetch(`${BASE_URL}/quiz-resume/save-progress/${attemptId}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${getAuthToken()}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(testData)
    })
    
    const data = await response.json()
    console.log('✅ Response:', data)
    
    if (response.ok) {
      console.log('💾 Tiến độ đã được lưu thành công')
    } else {
      console.error('❌ Error:', data)
    }
  } catch (error) {
    console.error('❌ Network error:', error)
  }
}

// Test 4: Resume attempt
async function testResumeAttempt(attemptId) {
  if (!attemptId) {
    console.log('⚠️ Bỏ qua test resume - không có attempt ID')
    return
  }
  
  console.log('🧪 TEST 4: Resume attempt:', attemptId)
  
  try {
    const response = await fetch(`${BASE_URL}/quiz-resume/resume/${attemptId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${getAuthToken()}`,
        'Content-Type': 'application/json'
      }
    })
    
    const data = await response.json()
    console.log('✅ Response:', data)
    
    if (response.ok) {
      console.log('🔄 Attempt đã được resume:', data.data)
    } else {
      console.error('❌ Error:', data)
    }
  } catch (error) {
    console.error('❌ Network error:', error)
  }
}

// Test 5: Lấy đáp án
async function testGetAnswers(attemptId) {
  if (!attemptId) {
    console.log('⚠️ Bỏ qua test get answers - không có attempt ID')
    return
  }
  
  console.log('🧪 TEST 5: Lấy đáp án cho attempt:', attemptId)
  
  try {
    const response = await fetch(`${BASE_URL}/quiz-resume/answers/${attemptId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${getAuthToken()}`,
        'Content-Type': 'application/json'
      }
    })
    
    const data = await response.json()
    console.log('✅ Response:', data)
    
    if (response.ok) {
      console.log('📝 Đáp án đã chọn:', data.answers)
    } else {
      console.error('❌ Error:', data)
    }
  } catch (error) {
    console.error('❌ Network error:', error)
  }
}

// Chạy tất cả tests
async function runAllTests() {
  console.log('🚀 BẮT ĐẦU TEST RESUME QUIZ API')
  console.log('=' .repeat(50))
  
  // Test 1: Kiểm tra attempt dở
  await testCheckInProgressAttempt()
  console.log('')
  
  // Test 2: Tạo attempt mới
  const attemptId = await testCreateNewAttempt()
  console.log('')
  
  // Test 3: Lưu tiến độ
  await testSaveProgress(attemptId)
  console.log('')
  
  // Test 4: Resume attempt
  await testResumeAttempt(attemptId)
  console.log('')
  
  // Test 5: Lấy đáp án
  await testGetAnswers(attemptId)
  console.log('')
  
  console.log('=' .repeat(50))
  console.log('✅ HOÀN THÀNH TẤT CẢ TESTS')
}

// Export để có thể chạy từ browser console
if (typeof window !== 'undefined') {
  window.testResumeQuiz = {
    runAllTests,
    testCheckInProgressAttempt,
    testCreateNewAttempt,
    testSaveProgress,
    testResumeAttempt,
    testGetAnswers
  }
}

// Chạy tests nếu file được execute trực tiếp
if (typeof require !== 'undefined') {
  runAllTests()
}
