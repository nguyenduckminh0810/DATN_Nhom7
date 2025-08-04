// Script test authentication
const axios = require("axios");

const BASE_URL = "http://localhost:8080/api";

async function testAuth() {
  console.log("🧪 Testing Authentication Flow...\n");

  try {
    // 1. Test login
    console.log("1️⃣ Testing login...");
    const loginResponse = await axios.post(`${BASE_URL}/login`, {
      username: "admin", // Thay bằng username thực tế
      password: "admin123", // Thay bằng password thực tế
    });

    console.log("✅ Login successful");
    const token = loginResponse.data.token;
    console.log("Token:", token.substring(0, 20) + "...\n");

    // 2. Test profile endpoint với token
    console.log("2️⃣ Testing profile endpoint with token...");
    const profileResponse = await axios.get(`${BASE_URL}/user/profile`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    console.log("✅ Profile fetch successful");
    console.log("User data:", profileResponse.data);

    // 3. Test profile endpoint không có token
    console.log("\n3️⃣ Testing profile endpoint without token...");
    try {
      await axios.get(`${BASE_URL}/user/profile`);
      console.log("❌ Should have failed but succeeded");
    } catch (error) {
      if (error.response?.status === 401) {
        console.log("✅ Correctly rejected without token");
      } else {
        console.log("❌ Unexpected error:", error.response?.status);
      }
    }
  } catch (error) {
    console.error("❌ Test failed:", error.response?.data || error.message);
  }
}

// Chạy test
testAuth();
