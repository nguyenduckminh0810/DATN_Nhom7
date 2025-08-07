# 🚀 HƯỚNG DẪN SETUP NOTIFICATION SYSTEM

## 📋 CÁC BƯỚC THỰC HIỆN

### Bước 1: Kiểm tra cấu trúc database hiện tại
```sql
-- Chạy file: check_notification_table.sql
-- Mục đích: Xem bảng notifications có tồn tại không và cấu trúc hiện tại
```

### Bước 2: Tạo bảng notifications (nếu chưa có)
```sql
-- Chạy file: create_notification_table.sql
-- Mục đích: Tạo bảng notifications với đầy đủ các field cần thiết
```

### Bước 3: Cập nhật bảng notifications (nếu đã có)
```sql
-- Chạy file: update_notification_table_safe.sql
-- Mục đích: Thêm các field mới vào bảng notifications hiện có
```

## 🔧 CÁCH CHẠY SCRIPT TRONG SQL SERVER MANAGEMENT STUDIO

### 1. Mở SQL Server Management Studio (SSMS)

### 2. Kết nối đến database
- Server name: `localhost` hoặc `your-server-name`
- Authentication: `SQL Server Authentication` hoặc `Windows Authentication`
- Database: `QuizApp`

### 3. Chạy từng script theo thứ tự:

#### Script 1: Kiểm tra cấu trúc
```sql
-- Mở file check_notification_table.sql
-- Nhấn F5 hoặc Execute để chạy
-- Xem kết quả trong tab Results
```

#### Script 2: Tạo bảng (nếu cần)
```sql
-- Mở file create_notification_table.sql
-- Nhấn F5 hoặc Execute để chạy
-- Xem thông báo trong tab Messages
```

#### Script 3: Cập nhật bảng (nếu cần)
```sql
-- Mở file update_notification_table_safe.sql
-- Nhấn F5 hoặc Execute để chạy
-- Xem thông báo trong tab Messages
```

## ⚠️ LƯU Ý QUAN TRỌNG

### 1. Backup database trước khi chạy
```sql
-- Backup database
BACKUP DATABASE QuizApp TO DISK = 'C:\backup\QuizApp_backup.bak'
```

### 2. Kiểm tra quyền truy cập
- Đảm bảo user có quyền `ALTER TABLE`, `CREATE INDEX`
- Nếu không có quyền, liên hệ DBA

### 3. Xử lý lỗi thường gặp

#### Lỗi 1: "Bảng notifications không tồn tại"
**Giải pháp:** Chạy script `create_notification_table.sql`

#### Lỗi 2: "Column đã tồn tại"
**Giải pháp:** Script an toàn sẽ bỏ qua, không ảnh hưởng

#### Lỗi 3: "Foreign key constraint"
**Giải pháp:** Kiểm tra bảng users có tồn tại không

#### Lỗi 4: "Permission denied"
**Giải pháp:** Liên hệ admin để cấp quyền

## 📊 KIỂM TRA KẾT QUẢ

### 1. Kiểm tra cấu trúc bảng
```sql
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'notifications'
ORDER BY ORDINAL_POSITION;
```

### 2. Kiểm tra dữ liệu
```sql
SELECT TOP 10 * FROM notifications ORDER BY created_at DESC;
```

### 3. Kiểm tra index
```sql
SELECT 
    i.name as IndexName,
    i.type_desc as IndexType
FROM sys.indexes i
WHERE i.object_id = OBJECT_ID('notifications');
```

## 🎯 KẾT QUẢ MONG ĐỢI

Sau khi chạy thành công, bảng `notifications` sẽ có cấu trúc:

| Column | Type | Nullable | Default |
|--------|------|----------|---------|
| id | BIGINT | NO | IDENTITY(1,1) |
| user_id | BIGINT | NO | - |
| content | NVARCHAR(MAX) | NO | - |
| notification_type | NVARCHAR(50) | NO | 'SYSTEM_ACTIVITY' |
| title | NVARCHAR(255) | YES | - |
| priority | NVARCHAR(20) | NO | 'NORMAL' |
| related_entity_id | BIGINT | YES | - |
| related_entity_type | NVARCHAR(50) | YES | - |
| action_url | NVARCHAR(255) | YES | - |
| is_read | BIT | NO | 0 |
| created_at | DATETIME2 | NO | GETDATE() |
| updated_at | DATETIME2 | YES | - |

## 🔄 TIẾP THEO

Sau khi setup database thành công:

1. **Backend:** Kiểm tra các file Java đã được tạo
2. **Frontend:** Cài đặt dependencies và kiểm tra WebSocket
3. **Test:** Chạy ứng dụng và test notification

## 📞 HỖ TRỢ

Nếu gặp lỗi, hãy cung cấp:
- Thông báo lỗi cụ thể
- Screenshot kết quả
- Phiên bản SQL Server
- Quyền của user database 