-- ============================================================================
-- Migration Script: Add Missing Columns to phieu_xuat Table
-- Description: Adds loai_xuat and ngay_han_thanh_toan columns for BAN_NO feature
-- Date: 2024
-- ============================================================================

USE oss;

-- Check if columns exist, if not add them
ALTER TABLE phieu_xuat
ADD COLUMN IF NOT EXISTS loai_xuat VARCHAR(50) NOT NULL DEFAULT 'TIEN_MAT' AFTER trang_thai,
ADD COLUMN IF NOT EXISTS ngay_han_thanh_toan DATETIME NULL AFTER loai_xuat;

-- Verify the columns were added
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'phieu_xuat' 
AND COLUMN_NAME IN ('loai_xuat', 'ngay_han_thanh_toan')
ORDER BY ORDINAL_POSITION;

-- Show full table structure
DESCRIBE phieu_xuat;
