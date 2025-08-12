// Test luồng đăng ký tài khoản
// Chạy file này để kiểm tra xem luồng đăng ký có hoạt động đúng không

const testRegistrationFlow = async () => {
  console.log('🧪 Testing Registration Flow...')
  
  // Test 1: Kiểm tra endpoint đăng ký
  try {
    const response = await fetch('http://localhost:8080/api/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: 'testuser_' + Date.now(),
        email: 'testuser_' + Date.now() + '@example.com',
        password: 'TestPassword123!',
        fullName: 'Test User',
        bio: 'Test bio',
        role: 'USER'
      })
    })
    
    const data = await response.json()
    console.log('📝 Registration Response:', data)
    
    if (data.status === 'SUCCESS') {
      console.log('✅ Registration successful!')
      console.log('🔑 Token:', data.token ? 'Present' : 'Missing')
      console.log('👤 User:', data.user ? 'Present' : 'Missing')
      
      // Test 2: Kiểm tra xem có thể truy cập dashboard không
      if (data.token) {
        try {
          const dashboardResponse = await fetch('http://localhost:8080/api/user/profile', {
            headers: {
              'Authorization': `Bearer ${data.token}`
            }
          })
          
          if (dashboardResponse.ok) {
            console.log('✅ Dashboard access successful!')
          } else {
            console.log('❌ Dashboard access failed:', dashboardResponse.status)
          }
        } catch (error) {
          console.log('❌ Dashboard access error:', error.message)
        }
      }
    } else {
      console.log('❌ Registration failed:', data.message)
    }
  } catch (error) {
    console.log('❌ Registration error:', error.message)
  }
}

// Chạy test
testRegistrationFlow()
