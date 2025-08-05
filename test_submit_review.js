const axios = require("axios");

async function testSubmitReview() {
  try {
    console.log("🔍 Testing submit review...");

    const reviewData = {
      userId: 1,
      rating: 5,
      reviewText: "Test review from script",
    };

    const response = await axios.post(
      "http://localhost:8080/api/quizzes/6/review",
      reviewData,
      {
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    console.log("✅ Submit review response:", response.data);

    // Test getting reviews after submit
    const reviewsResponse = await axios.get(
      "http://localhost:8080/api/quizzes/6/reviews"
    );
    console.log("✅ Reviews after submit:", reviewsResponse.data);
  } catch (error) {
    console.error(
      "❌ Error testing submit review:",
      error.response?.status,
      error.response?.data
    );
  }
}

testSubmitReview();
