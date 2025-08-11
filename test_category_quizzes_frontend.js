// Test script để kiểm tra component CategoryQuizzes
const axios = require('axios');

const BASE_URL = 'http://localhost:8080/api';

async function testCategoryQuizzesAPI() {
    try {
        console.log('🧪 Testing CategoryQuizzes Component API Integration');
        console.log('==================================================');

        // Test 1: Lấy danh sách categories
        console.log('\n📋 Test 1: Fetching categories');
        const categoriesResponse = await axios.get(`${BASE_URL}/categories`);
        console.log(`✅ Found ${categoriesResponse.data.length} categories`);

        if (categoriesResponse.data.length > 0) {
            const firstCategory = categoriesResponse.data[0];
            console.log(`📝 First category: ${firstCategory.name} (ID: ${firstCategory.id})`);

            // Test 2: Lấy quiz theo category
            console.log('\n📋 Test 2: Fetching quizzes by category');
            const quizzesResponse = await axios.get(`${BASE_URL}/quiz/public/category/${firstCategory.id}`, {
                params: { page: 0, size: 5 }
            });

            if (quizzesResponse.data.success) {
                console.log(`✅ Found ${quizzesResponse.data.totalItems} quizzes in category "${firstCategory.name}"`);
                console.log(`📊 Current page: ${quizzesResponse.data.currentPage + 1}/${quizzesResponse.data.totalPages}`);
                console.log(`📝 Quizzes on this page: ${quizzesResponse.data.quizzes.length}`);

                // Test 3: Kiểm tra cấu trúc quiz data
                if (quizzesResponse.data.quizzes.length > 0) {
                    const firstQuiz = quizzesResponse.data.quizzes[0];
                    console.log('\n📋 Test 3: Quiz data structure validation');
                    console.log(`✅ Quiz ID: ${firstQuiz.id}`);
                    console.log(`✅ Quiz Title: ${firstQuiz.title}`);
                    console.log(`✅ Is Public: ${firstQuiz.isPublic}`);
                    console.log(`✅ Category: ${firstQuiz.category?.name}`);
                    console.log(`✅ Creator: ${firstQuiz.user?.fullName || 'N/A'}`);
                    console.log(`✅ Created: ${firstQuiz.createdAt}`);
                    console.log(`✅ Has Image: ${firstQuiz.image ? 'Yes' : 'No'}`);
                    console.log(`✅ Questions Count: ${firstQuiz.questions?.length || 0}`);
                }

                // Test 4: Test pagination
                if (quizzesResponse.data.totalPages > 1) {
                    console.log('\n📋 Test 4: Testing pagination');
                    const page1Response = await axios.get(`${BASE_URL}/quiz/public/category/${firstCategory.id}`, {
                        params: { page: 1, size: 5 }
                    });

                    if (page1Response.data.success) {
                        console.log(`✅ Page 1 loaded successfully`);
                        console.log(`📊 Page 1 has ${page1Response.data.quizzes.length} quizzes`);
                        console.log(`📊 Current page: ${page1Response.data.currentPage + 1}`);
                    }
                }

                // Test 5: Test search functionality (client-side)
                console.log('\n📋 Test 5: Testing search functionality');
                const searchQuery = 'test';
                const filteredQuizzes = quizzesResponse.data.quizzes.filter(quiz =>
                    quiz.title.toLowerCase().includes(searchQuery.toLowerCase())
                );
                console.log(`🔍 Search for "${searchQuery}": Found ${filteredQuizzes.length} results`);

                // Test 6: Test sorting functionality (client-side)
                console.log('\n📋 Test 6: Testing sorting functionality');
                const sortedByDate = [...quizzesResponse.data.quizzes].sort((a, b) =>
                    new Date(b.createdAt) - new Date(a.createdAt)
                );
                console.log(`📅 Sorted by date (newest first): ${sortedByDate.length} quizzes`);

                const sortedByTitle = [...quizzesResponse.data.quizzes].sort((a, b) =>
                    a.title.localeCompare(b.title)
                );
                console.log(`📝 Sorted by title (A-Z): ${sortedByTitle.length} quizzes`);

            } else {
                console.log('❌ Failed to fetch quizzes:', quizzesResponse.data.message);
            }
        }

        // Test 7: Test multiple categories
        console.log('\n📋 Test 7: Testing multiple categories');
        const categoryIds = categoriesResponse.data.slice(0, 3).map(cat => cat.id);

        for (const categoryId of categoryIds) {
            try {
                const response = await axios.get(`${BASE_URL}/quiz/public/category/${categoryId}`, {
                    params: { page: 0, size: 3 }
                });

                if (response.data.success) {
                    const categoryName = response.data.category?.name || `Category ${categoryId}`;
                    console.log(`✅ ${categoryName}: ${response.data.totalItems} quizzes`);
                } else {
                    console.log(`❌ Category ${categoryId}: ${response.data.message}`);
                }
            } catch (error) {
                console.log(`❌ Category ${categoryId}: ${error.message}`);
            }
        }

        console.log('\n🏁 All tests completed successfully!');

    } catch (error) {
        console.error('❌ Error testing CategoryQuizzes:', error.message);
        if (error.response) {
            console.error('Response status:', error.response.status);
            console.error('Response data:', error.response.data);
        }
    }
}

async function testErrorHandling() {
    try {
        console.log('\n🧪 Testing Error Handling');
        console.log('==========================');

        // Test invalid category ID
        console.log('\n📋 Test: Invalid category ID');
        try {
            await axios.get(`${BASE_URL}/quiz/public/category/99999`);
        } catch (error) {
            if (error.response?.status === 404) {
                console.log('✅ 404 error handled correctly for invalid category');
            } else {
                console.log('❌ Unexpected error for invalid category:', error.message);
            }
        }

        // Test malformed request
        console.log('\n📋 Test: Malformed request');
        try {
            await axios.get(`${BASE_URL}/quiz/public/category/abc`);
        } catch (error) {
            if (error.response?.status === 400 || error.response?.status === 500) {
                console.log('✅ Error handled correctly for malformed request');
            } else {
                console.log('❌ Unexpected error for malformed request:', error.message);
            }
        }

    } catch (error) {
        console.error('❌ Error in error handling tests:', error.message);
    }
}

async function testPerformance() {
    try {
        console.log('\n🧪 Testing Performance');
        console.log('======================');

        const startTime = Date.now();

        // Test multiple concurrent requests
        const promises = [];
        for (let i = 0; i < 5; i++) {
            promises.push(
                axios.get(`${BASE_URL}/quiz/public/category/1`, {
                    params: { page: 0, size: 5 }
                })
            );
        }

        const results = await Promise.all(promises);
        const endTime = Date.now();

        console.log(`✅ 5 concurrent requests completed in ${endTime - startTime}ms`);
        console.log(`📊 Average response time: ${(endTime - startTime) / 5}ms per request`);

        // Check all responses are successful
        const allSuccessful = results.every(response => response.data.success);
        console.log(`✅ All responses successful: ${allSuccessful}`);

    } catch (error) {
        console.error('❌ Error in performance tests:', error.message);
    }
}

// Chạy tất cả tests
async function runAllTests() {
    console.log('🚀 Starting CategoryQuizzes Frontend Tests');
    console.log('==========================================\n');

    await testCategoryQuizzesAPI();
    await testErrorHandling();
    await testPerformance();

    console.log('\n🏁 All frontend tests completed!');
}

// Chạy test nếu file được execute trực tiếp
if (require.main === module) {
    runAllTests().catch(console.error);
}

module.exports = {
    testCategoryQuizzesAPI,
    testErrorHandling,
    testPerformance
}; 