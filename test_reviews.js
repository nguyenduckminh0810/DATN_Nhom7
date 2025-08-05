const axios = require("axios");

async function testReviews() {
  try {
    console.log("🔍 Testing reviews endpoint...");

    // Test public reviews endpoint
    const response = await axios.get(
      "http://localhost:8080/api/quizzes/public/6/reviews"
    );
    console.log("✅ Public reviews response:", response.data);

    // Test authenticated reviews endpoint
    const authResponse = await axios.get(
      "http://localhost:8080/api/quizzes/6/reviews",
      {
        headers: {
          Authorization: "Bearer your-token-here",
        },
      }
    );
    console.log("✅ Authenticated reviews response:", authResponse.data);
  } catch (error) {
    console.error(
      "❌ Error testing reviews:",
      error.response?.status,
      error.response?.data
    );
  }
}

testReviews();
