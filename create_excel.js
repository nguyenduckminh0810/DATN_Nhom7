const fs = require('fs');
const path = require('path');

// Dữ liệu câu hỏi toán học
const questions = [
    [1, "2 + 3 = ?", "4", "5", "6", "7", "B", 1],
    [2, "10 - 4 = ?", "5", "6", "7", "8", "B", 1],
    [3, "3 × 4 = ?", "10", "11", "12", "13", "C", 1],
    [4, "15 ÷ 3 = ?", "4", "5", "6", "7", "B", 1],
    [5, "√16 = ?", "3", "4", "5", "6", "B", 1],
    [6, "2³ = ?", "6", "7", "8", "9", "C", 1],
    [7, "5! = ?", "100", "110", "120", "130", "C", 2],
    [8, "sin(30°) = ?", "0.5", "0.6", "0.7", "0.8", "A", 2],
    [9, "log₁₀(100) = ?", "1", "2", "3", "4", "B", 2],
    [10, "∫x dx = ?", "x²", "x²/2", "x²/3", "x²/4", "B", 3]
];

// Tạo nội dung CSV
const headers = ["STT", "Câu hỏi", "Đáp án A", "Đáp án B", "Đáp án C", "Đáp án D", "Đáp án đúng", "Điểm"];
const csvContent = [
    headers.join(","),
    ...questions.map(row => row.map(cell => `"${cell}"`).join(","))
].join("\n");

// Tạo thư mục templates nếu chưa có
const templatesDir = path.join(__dirname, 'frontend', 'public', 'templates');
if (!fs.existsSync(templatesDir)) {
    fs.mkdirSync(templatesDir, { recursive: true });
}

// Ghi file CSV (có thể mở bằng Excel)
const csvPath = path.join(templatesDir, 'quiz-template.csv');
fs.writeFileSync(csvPath, csvContent, 'utf8');

console.log('✅ Đã tạo file Excel template thành công!');
console.log(`📁 Đường dẫn: ${csvPath}`);
console.log(`📊 Số câu hỏi: ${questions.length}`);
console.log('\n📋 Nội dung:');
console.log(csvContent); 