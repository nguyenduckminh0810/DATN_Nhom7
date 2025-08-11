// Test Quiz Attempts API
// Chạy trong browser console để test

const API_BASE_URL = 'http://localhost:8080/api'

// Test function để kiểm tra API
async function testQuizAttemptsAPI() {
    console.log('🧪 Testing Quiz Attempts API...')
    
    // 1. Kiểm tra token
    const token = localStorage.getItem('token')
    console.log('🔑 Token:', token ? '✅ Exists' : '❌ Missing')
    
    if (!token) {
        console.log('❌ No token found. Please login first.')
        return
    }
    
    // 2. Test startAttempt API
    try {
        console.log('\n📝 Testing startAttempt API...')
        const quizId = 12 // Thay đổi quiz ID nếu cần
        
        const response = await fetch(`${API_BASE_URL}/quiz-attempts/start?quizId=${quizId}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        })
        
        console.log('📡 Response status:', response.status)
        const data = await response.json()
        console.log('📄 Response data:', data)
        
        if (data.attemptId) {
            console.log('✅ Attempt created successfully with ID:', data.attemptId)
            
            // 3. Test getAttemptStatus API
            console.log('\n📊 Testing getAttemptStatus API...')
            const statusResponse = await fetch(`${API_BASE_URL}/quiz-attempts/${data.attemptId}/status`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            })
            
            console.log('📡 Status response status:', statusResponse.status)
            const statusData = await statusResponse.json()
            console.log('📄 Status data:', statusData)
            
            // 4. Test getAttemptQuestions API
            console.log('\n❓ Testing getAttemptQuestions API...')
            const questionsResponse = await fetch(`${API_BASE_URL}/quiz-attempts/${data.attemptId}/questions`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            })
            
            console.log('📡 Questions response status:', questionsResponse.status)
            const questionsData = await questionsResponse.json()
            console.log('📄 Questions data:', questionsData)
            
        } else {
            console.log('❌ Failed to create attempt:', data.error)
        }
        
    } catch (error) {
        console.error('❌ Error testing API:', error)
    }
}

// Test function để kiểm tra authentication
async function testAuthentication() {
    console.log('🔐 Testing Authentication...')
    
    const token = localStorage.getItem('token')
    const user = localStorage.getItem('user')
    const userId = localStorage.getItem('userId')
    
    console.log('🔑 Token:', token ? '✅ Exists' : '❌ Missing')
    console.log('👤 User:', user ? '✅ Exists' : '❌ Missing')
    console.log('🆔 User ID:', userId ? '✅ Exists' : '❌ Missing')
    
    if (user) {
        try {
            const userData = JSON.parse(user)
            console.log('📋 User data:', userData)
        } catch (e) {
            console.log('❌ Error parsing user data:', e)
        }
    }
}

// Test function để kiểm tra network connectivity
async function testNetworkConnectivity() {
    console.log('🌐 Testing Network Connectivity...')
    
    try {
        const response = await fetch(`${API_BASE_URL}/quiz-attempts/public/test`)
        console.log('📡 Test endpoint response status:', response.status)
        
        if (response.ok) {
            const data = await response.text()
            console.log('📄 Test endpoint response:', data)
        }
    } catch (error) {
        console.error('❌ Network connectivity error:', error)
    }
}

// Run all tests
async function runAllTests() {
    console.log('🚀 Starting Quiz Attempts API Tests...')
    console.log('=' .repeat(50))
    
    await testAuthentication()
    console.log('')
    
    await testNetworkConnectivity()
    console.log('')
    
    await testQuizAttemptsAPI()
    console.log('')
    
    console.log('🏁 Tests completed!')
}

// Export functions for manual testing
window.testQuizAttemptsAPI = testQuizAttemptsAPI
window.testAuthentication = testAuthentication
window.testNetworkConnectivity = testNetworkConnectivity
window.runAllTests = runAllTests

console.log('🧪 Quiz Attempts API Test loaded!')
console.log('📝 Run runAllTests() to test everything')
console.log('🔐 Run testAuthentication() to check auth')
console.log('🌐 Run testNetworkConnectivity() to check network')
console.log('📝 Run testQuizAttemptsAPI() to test API')
