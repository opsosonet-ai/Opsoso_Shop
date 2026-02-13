#!/bin/bash

# Database Backup Script for OSS Project
# Usage: ./backup_database.sh [backup_directory]

# Configuration
DB_USER="root"
DB_PASS="JavaBean@"
DB_NAME="oss"
BACKUP_DIR="${1:-/home/opsoso}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/oss_backup_$TIMESTAMP.sql"

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

echo "🗄️  Bắt đầu backup cơ sở dữ liệu OSS..."
echo "📍 Database: $DB_NAME"
echo "📁 Thư mục backup: $BACKUP_DIR"
echo "⏰ Thời gian: $(date)"
echo ""

# Perform database backup
mysqldump -u "$DB_USER" -p"$DB_PASS" \
    --databases "$DB_NAME" \
    --single-transaction \
    --routines \
    --triggers \
    --add-drop-database \
    --create-options \
    --disable-keys \
    --extended-insert \
    > "$BACKUP_FILE"

# Check if backup was successful
if [ $? -eq 0 ]; then
    echo "✅ Backup thành công!"
    echo "📄 File: $BACKUP_FILE"
    
    # Compress the backup file
    echo "🗜️  Nén file backup..."
    gzip "$BACKUP_FILE"
    
    if [ $? -eq 0 ]; then
        COMPRESSED_FILE="$BACKUP_FILE.gz"
        ORIGINAL_SIZE=$(du -h "$BACKUP_FILE" 2>/dev/null | cut -f1 || echo "N/A")
        COMPRESSED_SIZE=$(du -h "$COMPRESSED_FILE" | cut -f1)
        
        echo "✅ Nén thành công!"
        echo "📦 File nén: $COMPRESSED_FILE"
        echo "📊 Kích thước nén: $COMPRESSED_SIZE"
    else
        echo "⚠️  Lỗi khi nén file backup"
    fi
    
    # Clean up old backups (keep only last 7 backups)
    echo ""
    echo "🧹 Dọn dẹp backup cũ (giữ lại 7 backup gần nhất)..."
    find "$BACKUP_DIR" -name "oss_backup_*.sql.gz" -type f -mtime +7 -delete
    
    echo ""
    echo "📋 Danh sách backup hiện có:"
    ls -lh "$BACKUP_DIR"/oss_backup_*.sql.gz 2>/dev/null || echo "Không có backup nào"
    
else
    echo "❌ Lỗi khi backup cơ sở dữ liệu!"
    exit 1
fi

echo ""
echo "🎉 Hoàn thành backup cơ sở dữ liệu OSS!"
