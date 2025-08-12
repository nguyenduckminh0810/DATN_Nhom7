// Test script để kiểm tra API lấy quiz public theo category
const axios = require('axios');

const BASE_URL = 'http://localhost:8080/api';

async function testPublicQuizzesByCategory() {
    try {
        console.log('🧪 Testing API: Get Public Quizzes by Category');
        console.log('==============================================');

        // Test với category ID = 1 (thay đổi theo database của bạn)
        const categoryId = 1;
        const page = 0;
        const size = 5;

        console.log(`📋 Requesting public quizzes for category ID: ${categoryId}`);
        console.log(`📄 Page: ${page}, Size: ${size}`);

        const response = await axios.get(`${BASE_URL}/quiz/public/category/${categoryId}`, {
            params: {
                page: page,
                size: size
            }
        });

        console.log('✅ Response received successfully!');
        console.log('📊 Response data:');
        console.log(JSON.stringify(response.data, null, 2));

        // Kiểm tra cấu trúc response
        const data = response.data;
        if (data.success) {
            console.log('\n🎯 Response validation:');
            console.log(`- Success: ${data.success}`);
            console.log(`- Message: ${data.message}`);
            console.log(`- Category: ${data.category?.name} (ID: ${data.category?.id})`);
            console.log(`- Total quizzes: ${data.totalItems}`);
            console.log(`- Current page: ${data.currentPage}`);
            console.log(`- Total pages: ${data.totalPages}`);
            console.log(`- Page size: ${data.pageSize}`);

            if (data.quizzes && data.quizzes.length > 0) {
                console.log('\n📝 Quizzes found:');
                data.quizzes.forEach((quiz, index) => {
                    console.log(`${index + 1}. ${quiz.title}`);
                    console.log(`   - ID: ${quiz.id}`);
                    console.log(`   - Public: ${quiz.isPublic}`);
                    console.log(`   - Category: ${quiz.category?.name}`);
                    console.log(`   - Has Image: ${quiz.image ? 'Yes' : 'No'}`);
                    console.log(`   - Created: ${quiz.createdAt}`);
                    console.log('');
                });
            } else {
                console.log('\n❌ No quizzes found for this category');
            }
        } else {
            console.log('❌ API returned error:', data.message);
        }

    } catch (error) {
        console.error('❌ Error testing API:');
        if (error.response) {
            console.error('Status:', error.response.status);
            console.error('Data:', error.response.data);
        } else {
            console.error('Error:', error.message);
        }
    }
}

async function testMultipleCategories() {
    try {
        console.log('\n🧪 Testing Multiple Categories');
        console.log('==============================');

        // Test với các category ID khác nhau
        const categoryIds = [1, 2, 3]; // Thay đổi theo database của bạn

        for (const categoryId of categoryIds) {
            console.log(`\n📋 Testing category ID: ${categoryId}`);

            try {
                const response = await axios.get(`${BASE_URL}/quiz/public/category/${categoryId}`, {
                    params: { page: 0, size: 3 }
                });

                const data = response.data;
                if (data.success) {
                    console.log(`✅ Category: ${data.category?.name}`);
                    console.log(`   - Found ${data.totalItems} quizzes`);
                    console.log(`   - First quiz: ${data.quizzes[0]?.title || 'None'}`);
                } else {
                    console.log(`❌ Error: ${data.message}`);
                }
            } catch (error) {
                if (error.response?.status === 404) {
                    console.log(`❌ Category ID ${categoryId} not found`);
                } else {
                    console.log(`❌ Error: ${error.message}`);
                }
            }
        }

    } catch (error) {
        console.error('❌ Error testing multiple categories:', error.message);
    }
}

async function testPagination() {
    try {
        console.log('\n🧪 Testing Pagination');
        console.log('=====================');

        const categoryId = 1; // Thay đổi theo database của bạn
        const pageSize = 2;

        console.log(`📋 Testing pagination for category ID: ${categoryId} with page size: ${pageSize}`);

        // Test page 0
        const page0Response = await axios.get(`${BASE_URL}/quiz/public/category/${categoryId}`, {
            params: { page: 0, size: pageSize }
        });

        const page0Data = page0Response.data;
        console.log(`\n📄 Page 0:`);
        console.log(`- Total items: ${page0Data.totalItems}`);
        console.log(`- Total pages: ${page0Data.totalPages}`);
        console.log(`- Quizzes on this page: ${page0Data.quizzes.length}`);

        if (page0Data.totalPages > 1) {
            // Test page 1
            const page1Response = await axios.get(`${BASE_URL}/quiz/public/category/${categoryId}`, {
                params: { page: 1, size: pageSize }
            });

            const page1Data = page1Response.data;
            console.log(`\n📄 Page 1:`);
            console.log(`- Quizzes on this page: ${page1Data.quizzes.length}`);
            console.log(`- Current page: ${page1Data.currentPage}`);
        }

    } catch (error) {
        console.error('❌ Error testing pagination:', error.message);
    }
}

// Chạy các test
async function runAllTests() {
    console.log('🚀 Starting API Tests for Public Quizzes by Category');
    console.log('==================================================\n');

    await testPublicQuizzesByCategory();
    await testMultipleCategories();
    await testPagination();

    console.log('\n🏁 All tests completed!');
}

// Chạy test nếu file được execute trực tiếp
if (require.main === module) {
    runAllTests().catch(console.error);
}

module.exports = {
    testPublicQuizzesByCategory,
    testMultipleCategories,
    testPagination
}; 