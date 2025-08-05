const axios = require("axios");

async function testReviews() {
  try {
    console.log("🔍 Testing public reviews endpoint in QuizController...");

    const response = await axios.get(
      "http://localhost:8080/api/quiz/public/6/reviews"
    );
    console.log("✅ Public reviews response:", response.data);
  } catch (error) {
    console.error(
      "❌ Error testing public reviews:",
      error.response?.status,
      error.response?.data
    );
  }
}

testReviews();
