-- ============================================
-- Phase 3: Database Performance Indexes (FIXED)
-- Database: OSS
-- Date: 01/11/2025
-- Expected Impact: 10-20x faster queries
-- ============================================

-- ============================================
-- STEP 1: Single Column Indexes (Search & Filter)
-- ============================================

-- Product Name Search
CREATE INDEX IF NOT EXISTS idx_hang_hoa_ten ON hang_hoa(ten_hang_hoa);

-- Product Code Lookup
CREATE INDEX IF NOT EXISTS idx_hang_hoa_ma ON hang_hoa(ma_hang_hoa);

-- Return Status Filter
CREATE INDEX IF NOT EXISTS idx_tra_hang_trang_thai ON tra_hang(trang_thai);

-- Return Code Lookup
CREATE INDEX IF NOT EXISTS idx_tra_hang_ma ON tra_hang(ma_tra_hang);

-- User Authentication (Critical for every login!)
CREATE INDEX IF NOT EXISTS idx_user_username ON users(username);

-- User Active Status Filter
CREATE INDEX IF NOT EXISTS idx_user_active ON users(active);

-- Invoice Date Range Query
CREATE INDEX IF NOT EXISTS idx_phieu_xuat_ngay ON phieu_xuat(ngay_xuat);

-- ============================================
-- STEP 2: Foreign Key Indexes (Join Optimization)
-- ============================================

-- Product-Supplier joins
CREATE INDEX IF NOT EXISTS idx_hang_hoa_supplier ON hang_hoa(ma_nha_phan_phoi);

-- Invoice-Customer joins
CREATE INDEX IF NOT EXISTS idx_phieu_xuat_khach ON phieu_xuat(khach_hang_id);

-- ============================================
-- STEP 3: Composite Indexes (Multi-column Queries)
-- ============================================

-- Return: Status + Date (Common WHERE clause)
CREATE INDEX IF NOT EXISTS idx_tra_hang_status_date ON tra_hang(trang_thai, ngay_tra_hang);

-- Product: Supplier + Active (Filter products by supplier and status)
CREATE INDEX IF NOT EXISTS idx_hang_hoa_supplier_active ON hang_hoa(ma_nha_phan_phoi, ten_hang_hoa);

-- Invoice: Date + Active (Common queries)
CREATE INDEX IF NOT EXISTS idx_phieu_xuat_date_active ON phieu_xuat(ngay_xuat, khach_hang_id);

-- ============================================
-- VERIFICATION QUERIES (Run after creating indexes)
-- ============================================

-- Count total indexes created
SELECT COUNT(*) as total_indexes FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = 'oss' AND INDEX_NAME NOT IN ('PRIMARY');

-- View all indexes by table
SELECT TABLE_NAME, INDEX_NAME, COLUMN_NAME 
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = 'oss' AND TABLE_NAME IN ('hang_hoa', 'tra_hang', 'users', 'phieu_xuat')
ORDER BY TABLE_NAME, INDEX_NAME;

-- Check specific table indexes
SELECT TABLE_NAME, INDEX_NAME, COLUMN_NAME, SEQ_IN_INDEX
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'oss' AND TABLE_NAME = 'hang_hoa'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;
