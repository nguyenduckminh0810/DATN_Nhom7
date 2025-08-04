// Script kiểm tra quiz của user "thanh"
const axios = require("axios");

const BASE_URL = "http://localhost:8080/api";

async function checkUserQuizzes() {
  console.log("🔍 Checking quizzes for user 'thanh'...\n");

  try {
    // 1. Login
    console.log("1️⃣ Login...");
    const loginResponse = await axios.post(`${BASE_URL}/login`, {
      username: "thanh",
      password: "password123", // Thay bằng password thực tế
    });

    console.log("✅ Login successful");
    const token = loginResponse.data.token;
    const user = loginResponse.data.user;
    console.log("User ID:", user?.id);
    console.log("Username:", user?.username);
    console.log("\n");

    // 2. Lấy danh sách quiz của user
    console.log("2️⃣ Getting user quizzes...");
    try {
      const quizzesResponse = await axios.get(`${BASE_URL}/quiz/user`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      console.log("✅ User quizzes:");
      console.log("  - Total quizzes:", quizzesResponse.data.length);
      
      if (quizzesResponse.data.length > 0) {
        console.log("  - Quiz IDs:", quizzesResponse.data.map(q => q.id));
        
        // Test với quiz đầu tiên
        const firstQuiz = quizzesResponse.data[0];
        console.log(`\n3️⃣ Testing quiz ID ${firstQuiz.id}...`);
        
        // Test quiz detail
        const detailResponse = await axios.get(`${BASE_URL}/quiz/detail/${firstQuiz.id}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        
        console.log("✅ Quiz detail:");
        console.log("  - ID:", detailResponse.data.id);
        console.log("  - Title:", detailResponse.data.title);
        console.log("  - Creator ID:", detailResponse.data.creatorId);
        
        // Test questions
        const questionsResponse = await axios.get(`${BASE_URL}/question/${firstQuiz.id}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        
        console.log("✅ Questions access successful");
        console.log("  - Questions count:", questionsResponse.data.length);
        
      } else {
        console.log("❌ User has no quizzes");
      }
      
    } catch (error) {
      console.log("❌ Error getting user quizzes:", error.response?.status);
    }

  } catch (error) {
    console.error("❌ Test failed:", error.response?.data || error.message);
  }
}

// Chạy test
checkUserQuizzes(); 