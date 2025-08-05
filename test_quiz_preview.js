// Script test quiz preview functionality
const axios = require("axios");

const BASE_URL = "http://localhost:8080/api";

async function testQuizPreview() {
  console.log("🧪 Testing Quiz Preview Functionality...\n");

  try {
    // 1. Test login as quiz creator
    console.log("1️⃣ Testing login as quiz creator...");
    const loginResponse = await axios.post(`${BASE_URL}/login`, {
      username: "admin", // Thay bằng username thực tế
      password: "admin123", // Thay bằng password thực tế
    });

    console.log("✅ Login successful");
    const token = loginResponse.data.token;
    console.log("Token:", token.substring(0, 20) + "...\n");

    // 2. Test getting questions as quiz creator (should work)
    console.log("2️⃣ Testing get questions as quiz creator...");
    try {
      const questionsResponse = await axios.get(`${BASE_URL}/question/1`, {
        // Thay 1 bằng quiz ID thực tế
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      console.log("✅ Quiz creator can access questions");
      console.log("Questions count:", questionsResponse.data.length);
    } catch (error) {
      if (error.response?.status === 403) {
        console.log("❌ Quiz creator cannot access questions (unexpected)");
      } else {
        console.log("❌ Unexpected error:", error.response?.status);
      }
    }

    // 3. Test getting questions as non-creator (should fail)
    console.log("\n3️⃣ Testing get questions as non-creator...");
    try {
      const questionsResponse2 = await axios.get(`${BASE_URL}/question/2`, {
        // Thay 2 bằng quiz ID khác
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      console.log("❌ Non-creator can access questions (unexpected)");
    } catch (error) {
      if (error.response?.status === 403) {
        console.log(
          "✅ Non-creator correctly blocked from accessing questions"
        );
      } else {
        console.log("❌ Unexpected error:", error.response?.status);
      }
    }

    // 4. Test play endpoint (should work for everyone)
    console.log("\n4️⃣ Testing play endpoint...");
    try {
      const playResponse = await axios.get(`${BASE_URL}/question/play/1`); // Thay 1 bằng quiz ID thực tế
      console.log("✅ Play endpoint accessible");
      console.log("Questions count:", playResponse.data.length);
    } catch (error) {
      console.log("❌ Play endpoint error:", error.response?.status);
    }
  } catch (error) {
    console.error("❌ Test failed:", error.response?.data || error.message);
  }
}

// Chạy test
testQuizPreview();
