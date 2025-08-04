// Script test quiz detail để debug creatorId
const axios = require("axios");

const BASE_URL = "http://localhost:8080/api";

async function testQuizDetailDebug() {
  console.log("🔍 Testing Quiz Detail Debug...\n");

  try {
    // 1. Login với tài khoản "thanh"
    console.log("1️⃣ Login...");
    const loginResponse = await axios.post(`${BASE_URL}/login`, {
      username: "thanh",
      password: "123456@A", // Thử với password đơn giản
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
      console.log(
        "  - Full response:",
        JSON.stringify(detailResponse.data, null, 2)
      );

      // Kiểm tra từng field
      console.log("\n🔍 Checking fields:");
      console.log("  - ID:", detailResponse.data.id);
      console.log("  - Title:", detailResponse.data.title);
      console.log("  - Creator Name:", detailResponse.data.creatorName);
      console.log("  - Creator ID:", detailResponse.data.creatorId);
      console.log("  - Creator Avatar:", detailResponse.data.creatorAvatar);
      console.log("  - Is Public:", detailResponse.data.isPublic);

      // Kiểm tra creatorId
      if (detailResponse.data.creatorId) {
        console.log("✅ Creator ID is present:", detailResponse.data.creatorId);
      } else {
        console.log("❌ Creator ID is missing!");
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
      console.log("❌ Quiz detail error:", error.response?.status);
      console.log("Error details:", error.response?.data);
    }
  } catch (error) {
    console.error("❌ Test failed:", error.response?.data || error.message);
  }
}

// Chạy test
testQuizDetailDebug();
