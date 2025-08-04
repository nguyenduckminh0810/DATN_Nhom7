// Script test quiz ID 6
const axios = require("axios");

const BASE_URL = "http://localhost:8080/api";

async function testQuiz6() {
  console.log("🧪 Testing Quiz ID 6...\n");

  try {
    // 1. Test login với tài khoản "thành"
    console.log("1️⃣ Testing login...");
    const loginResponse = await axios.post(`${BASE_URL}/login`, {
      username: "thành", // Thử với tài khoản "thành"
      password: "password123",
    });

    console.log("✅ Login successful");
    const token = loginResponse.data.token;
    const user = loginResponse.data.user;
    console.log("User ID:", user?.id);
    console.log("Username:", user?.username);
    console.log("\n");

    // 2. Test get quiz detail cho quiz ID 6
    console.log("2️⃣ Testing get quiz detail for ID 6...");
    try {
      const detailResponse = await axios.get(`${BASE_URL}/quiz/detail/6`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      console.log("✅ Quiz detail response:");
      console.log("  - ID:", detailResponse.data.id);
      console.log("  - Title:", detailResponse.data.title);
      console.log("  - Creator Name:", detailResponse.data.creatorName);
      console.log("  - Creator ID:", detailResponse.data.creatorId);
      console.log("  - Is Public:", detailResponse.data.isPublic);

      // Kiểm tra creatorId
      if (detailResponse.data.creatorId) {
        console.log("✅ Creator ID is present:", detailResponse.data.creatorId);
      } else {
        console.log("❌ Creator ID is missing!");
      }
    } catch (error) {
      console.log("❌ Quiz detail error:", error.response?.status);
      console.log("Error details:", error.response?.data);
    }

    // 3. Test get questions cho quiz ID 6
    console.log("\n3️⃣ Testing get questions for quiz ID 6...");
    try {
      const questionsResponse = await axios.get(`${BASE_URL}/question/6`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      console.log("✅ Questions access successful");
      console.log("  - Questions count:", questionsResponse.data.length);
    } catch (error) {
      if (error.response?.status === 403) {
        console.log("❌ 403 Forbidden - User is not the quiz creator");
      } else {
        console.log("❌ Questions error:", error.response?.status);
        console.log("Error details:", error.response?.data);
      }
    }
  } catch (error) {
    console.error("❌ Test failed:", error.response?.data || error.message);
    console.error("Full error:", error);
  }
}

// Chạy test
testQuiz6();
