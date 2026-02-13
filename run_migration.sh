#!/bin/bash

# Script: run_migration.sh
# Mục Đích: Chạy migration SQL để tạo bảng công nợ
# Ngày: November 1, 2025

set -e

echo "=========================================="
echo "🚀 Chạy Migration Công Nợ (Debt Management)"
echo "=========================================="
echo ""

# Kiểm tra xem file migration có tồn tại không
if [ ! -f "V1__Create_Debt_Tables.sql" ]; then
    echo "❌ Lỗi: File V1__Create_Debt_Tables.sql không tìm thấy!"
    echo "   Vui lòng chạy script này từ thư mục chứa file migration."
    exit 1
fi

# Lấy thông tin database từ application.properties
echo "📝 Đang đọc cấu hình database..."

# Thử lấy từ application.properties
if [ -f "src/main/resources/application.properties" ]; then
    DB_URL=$(grep "spring.datasource.url" src/main/resources/application.properties | cut -d '=' -f 2 | sed 's/^[ \t]*//;s/[ \t]*$//')
    DB_USER=$(grep "spring.datasource.username" src/main/resources/application.properties | cut -d '=' -f 2 | sed 's/^[ \t]*//;s/[ \t]*$//')
    DB_PASS=$(grep "spring.datasource.password" src/main/resources/application.properties | cut -d '=' -f 2 | sed 's/^[ \t]*//;s/[ \t]*$//')
    
    # Xử lý thông tin database từ URL
    # Format: jdbc:mysql://localhost:3306/oss_public
    DB_NAME=$(echo $DB_URL | sed 's|.*//[^/]*/||')
    DB_HOST=$(echo $DB_URL | sed 's|.*//||' | sed 's|:.*||')
    
    echo "✅ Database: $DB_NAME"
    echo "✅ Host: $DB_HOST"
    echo "✅ User: $DB_USER"
else
    echo "⚠️  Không tìm thấy application.properties"
    echo "   Sử dụng giá trị mặc định:"
    DB_HOST="localhost"
    DB_USER="root"
    DB_PASS="password"
    DB_NAME="oss_public"
fi

echo ""
echo "⏳ Đang chạy migration..."
echo ""

# Chạy SQL migration
mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < V1__Create_Debt_Tables.sql

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "✅ Migration hoàn thành thành công!"
    echo "=========================================="
    echo ""
    echo "📊 Bảng được tạo:"
    echo "   1. supplier_debt (Nợ nhà phân phối)"
    echo "   2. supplier_debt_payment (Chi tiết thanh toán NCC)"
    echo "   3. customer_debt (Nợ khách hàng)"
    echo "   4. customer_debt_payment (Chi tiết thu hồi KH)"
    echo ""
    echo "🎯 Bước tiếp theo:"
    echo "   1. Restart ứng dụng Spring Boot"
    echo "   2. Truy cập http://127.0.0.1:8080"
    echo "   3. Kiểm tra bảng trong database"
    echo ""
else
    echo ""
    echo "❌ Migration thất bại!"
    echo "   Vui lòng kiểm tra lỗi trên"
    exit 1
fi
