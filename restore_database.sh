#!/bin/bash

# Database Restore Script for OSS Project
# Usage: ./restore_database.sh <backup_file.sql.gz>

# Configuration
DB_USER="root"
DB_PASS="Vph38302"
DB_NAME="oss"

# Check if backup file is provided
if [ $# -eq 0 ]; then
    echo "❌ Lỗi: Vui lòng cung cấp file backup để restore"
    echo "📖 Cách sử dụng: $0 <backup_file.sql.gz>"
    echo ""
    echo "📋 Danh sách backup có sẵn:"
    ls -lh /home/opsoso/OSS/backups/oss_backup_*.sql.gz 2>/dev/null || echo "Không có backup nào"
    exit 1
fi

BACKUP_FILE="$1"

# Check if backup file exists
if [ ! -f "$BACKUP_FILE" ]; then
    echo "❌ Lỗi: File backup không tồn tại: $BACKUP_FILE"
    exit 1
fi

echo "🔄 Bắt đầu khôi phục cơ sở dữ liệu OSS..."
echo "📍 Database: $DB_NAME"
echo "📄 File backup: $BACKUP_FILE"
echo "⏰ Thời gian: $(date)"
echo ""

# Warning confirmation
read -p "⚠️  CẢNH BÁO: Thao tác này sẽ XÓA toàn bộ dữ liệu hiện tại! Bạn có chắc chắn? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Hủy bỏ khôi phục database"
    exit 1
fi

# Create backup of current database before restore
echo "📋 Tạo backup hiện tại trước khi khôi phục..."
CURRENT_BACKUP="/tmp/oss_before_restore_$(date +%Y%m%d_%H%M%S).sql.gz"
mysqldump -u "$DB_USER" -p"$DB_PASS" --databases "$DB_NAME" --single-transaction | gzip > "$CURRENT_BACKUP"
echo "✅ Backup hiện tại đã lưu tại: $CURRENT_BACKUP"
echo ""

# Decompress if needed and restore
if [[ "$BACKUP_FILE" == *.gz ]]; then
    echo "🗜️  Giải nén file backup..."
    zcat "$BACKUP_FILE" | mysql -u "$DB_USER" -p"$DB_PASS"
else
    echo "📥 Khôi phục từ file SQL..."
    mysql -u "$DB_USER" -p"$DB_PASS" < "$BACKUP_FILE"
fi

# Check if restore was successful
if [ $? -eq 0 ]; then
    echo "✅ Khôi phục cơ sở dữ liệu thành công!"
    echo "🔄 Vui lòng khởi động lại ứng dụng để áp dụng thay đổi"
    echo ""
    echo "📊 Thống kê sau khôi phục:"
    mysql -u "$DB_USER" -p"$DB_PASS" -e "USE $DB_NAME; 
        SELECT 'Users' as Table_Name, COUNT(*) as Records FROM users
        UNION ALL
        SELECT 'Hang Hoa', COUNT(*) FROM hang_hoa  
        UNION ALL
        SELECT 'Phieu Xuat', COUNT(*) FROM phieu_xuat
        UNION ALL
        SELECT 'Khach Hang', COUNT(*) FROM khach_hang;"
else
    echo "❌ Lỗi khi khôi phục cơ sở dữ liệu!"
    echo "🔄 Khôi phục lại từ backup hiện tại..."
    zcat "$CURRENT_BACKUP" | mysql -u "$DB_USER" -p"$DB_PASS"
    exit 1
fi

echo ""
echo "🎉 Hoàn thành khôi phục cơ sở dữ liệu OSS!"