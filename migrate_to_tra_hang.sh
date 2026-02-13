#!/bin/bash

# =====================================
# 🔄 Migration Script: Đổi trả → Trả hàng
# =====================================

echo "🔄 Migration: Đổi từ 'Đổi trả' thành 'Trả hàng'"
echo "============================================="

# Database configuration
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="oss"
DB_USER="root"
DB_PASS="newpassword123"

# Backup configuration
BACKUP_DIR="backups"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/pre_migration_backup_${TIMESTAMP}.sql"

# Create backup directory
mkdir -p "$BACKUP_DIR"

echo "📋 Kiểm tra trước migration..."

# 1. Kiểm tra kết nối database
echo "🔗 Kiểm tra kết nối database..."
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "SELECT 1;" "$DB_NAME" >/dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "❌ Không thể kết nối database!"
    echo "   Kiểm tra lại thông tin kết nối:"
    echo "   - Host: $DB_HOST"
    echo "   - Port: $DB_PORT"  
    echo "   - Database: $DB_NAME"
    echo "   - User: $DB_USER"
    exit 1
fi
echo "✅ Kết nối database thành công"

# 2. Kiểm tra bảng tồn tại
echo "📊 Kiểm tra schema hiện tại..."
TABLE_EXISTS=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -se "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB_NAME' AND table_name='doi_tra_hang_hoa';" 2>/dev/null)

if [ "$TABLE_EXISTS" = "1" ]; then
    echo "📋 Tìm thấy bảng doi_tra_hang_hoa"
    
    # Đếm số bản ghi
    TOTAL_RECORDS=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -se "SELECT COUNT(*) FROM doi_tra_hang_hoa;" "$DB_NAME" 2>/dev/null)
    TRA_HANG_RECORDS=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -se "SELECT COUNT(*) FROM doi_tra_hang_hoa WHERE loai_doi_tra = 'TRA_HANG';" "$DB_NAME" 2>/dev/null)
    DOI_HANG_RECORDS=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -se "SELECT COUNT(*) FROM doi_tra_hang_hoa WHERE loai_doi_tra = 'DOI_HANG';" "$DB_NAME" 2>/dev/null)
    
    echo "   📊 Tổng số bản ghi: $TOTAL_RECORDS"
    echo "   📦 Bản ghi TRA_HANG: $TRA_HANG_RECORDS"
    echo "   🔄 Bản ghi DOI_HANG: $DOI_HANG_RECORDS (sẽ bị bỏ qua)"
else
    echo "ℹ️  Không tìm thấy bảng doi_tra_hang_hoa (có thể đây là lần đầu chạy)"
    TOTAL_RECORDS=0
    TRA_HANG_RECORDS=0
fi

# 3. Backup dữ liệu
if [ "$TOTAL_RECORDS" -gt 0 ]; then
    echo "💾 Tạo backup dữ liệu..."
    echo "   📁 File backup: $BACKUP_FILE"
    
    mysqldump -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
        --single-transaction \
        --routines \
        --triggers \
        --add-drop-table \
        "$DB_NAME" > "$BACKUP_FILE"
    
    if [ $? -eq 0 ]; then
        BACKUP_SIZE=$(ls -lh "$BACKUP_FILE" | awk '{print $5}')
        echo "   ✅ Backup thành công (${BACKUP_SIZE})"
    else
        echo "   ❌ Backup thất bại!"
        exit 1
    fi
else
    echo "ℹ️  Bỏ qua backup (không có dữ liệu)"
fi

# 4. Hỏi xác nhận
echo ""
echo "⚠️  CẢNH BÁO: Migration sẽ thực hiện các thay đổi sau:"
echo "   • Tạo bảng mới: tra_hang"
echo "   • Migration $TRA_HANG_RECORDS bản ghi TRA_HANG"
echo "   • Bỏ qua $DOI_HANG_RECORDS bản ghi DOI_HANG"
echo "   • Giữ nguyên bảng cũ (không xóa tự động)"
echo ""

read -p "❓ Bạn có muốn tiếp tục migration? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Migration đã bị hủy bởi người dùng"
    exit 1
fi

# 5. Thực hiện migration
echo "🚀 Bắt đầu migration..."

mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < migrate_doi_tra_to_tra_hang.sql

if [ $? -eq 0 ]; then
    echo "✅ Migration thành công!"
    
    # Kiểm tra kết quả
    NEW_RECORDS=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -se "SELECT COUNT(*) FROM tra_hang;" "$DB_NAME" 2>/dev/null)
    echo "📊 Kết quả migration:"
    echo "   📦 Bản ghi đã migrate: $NEW_RECORDS"
    
    if [ "$NEW_RECORDS" = "$TRA_HANG_RECORDS" ]; then
        echo "   ✅ Migration hoàn hảo!"
    else
        echo "   ⚠️  Số lượng không khớp, kiểm tra lại!"
    fi
    
else
    echo "❌ Migration thất bại!"
    if [ -f "$BACKUP_FILE" ]; then
        echo "💡 Bạn có thể restore từ backup: $BACKUP_FILE"
        echo "   mysql -u$DB_USER -p$DB_PASS $DB_NAME < $BACKUP_FILE"
    fi
    exit 1
fi

# 6. Hướng dẫn cleanup
echo ""
echo "🧹 Cleanup (tùy chọn):"
echo "   Sau khi kiểm tra ứng dụng hoạt động tốt, bạn có thể:"
echo "   1. Xóa bảng cũ: DROP TABLE doi_tra_hang_hoa;"
echo "   2. Xóa backup cũ nếu không cần: rm $BACKUP_FILE"
echo ""
echo "🎉 Migration hoàn tất!"
echo "   📍 URL mới: http://localhost:8080/tra-hang"
echo "   🔄 Restart ứng dụng để áp dụng thay đổi"