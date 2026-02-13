-- Migration script: Đổi từ "Đổi trả" thành "Trả hàng"
-- Chạy script này để cập nhật database schema

-- 1. Tạo bảng mới tra_hang
CREATE TABLE IF NOT EXISTS tra_hang (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ma_tra_hang VARCHAR(50) NOT NULL UNIQUE,
    hang_hoa_id BIGINT NOT NULL,
    so_luong INT NOT NULL,
    don_gia DECIMAL(15,2) NOT NULL,
    thanh_tien DECIMAL(15,2) NOT NULL,
    ten_khach_hang VARCHAR(100),
    so_dien_thoai VARCHAR(20),
    ly_do VARCHAR(500),
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'CHO_DUYET',
    nguoi_xu_ly VARCHAR(100),
    ngay_tra_hang DATETIME,
    ngay_xu_ly DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT FK_tra_hang_hang_hoa FOREIGN KEY (hang_hoa_id) REFERENCES hang_hoa(id),
    INDEX idx_tra_hang_ma (ma_tra_hang),
    INDEX idx_tra_hang_khach_hang (ten_khach_hang),
    INDEX idx_tra_hang_sdt (so_dien_thoai),
    INDEX idx_tra_hang_trang_thai (trang_thai),
    INDEX idx_tra_hang_ngay (ngay_tra_hang)
);

-- 2. Migration dữ liệu từ bảng cũ (nếu tồn tại và có dữ liệu TRA_HANG)
INSERT INTO tra_hang (
    ma_tra_hang, hang_hoa_id, so_luong, don_gia, thanh_tien,
    ten_khach_hang, so_dien_thoai, ly_do, trang_thai, nguoi_xu_ly,
    ngay_tra_hang, ngay_xu_ly, created_at, updated_at
)
SELECT 
    CONCAT('TH', LPAD(ROW_NUMBER() OVER (ORDER BY id), 3, '0')) as ma_tra_hang,
    hang_hoa_id,
    so_luong,
    don_gia,
    thanh_tien,
    ten_khach_hang,
    so_dien_thoai,
    ly_do,
    trang_thai,
    nguoi_xu_ly,
    ngay_doi_tra as ngay_tra_hang,
    ngay_xu_ly,
    created_at,
    updated_at
FROM doi_tra_hang_hoa 
WHERE loai_doi_tra = 'TRA_HANG'
AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'doi_tra_hang_hoa');

-- 3. Xóa bảng cũ (sau khi đã backup dữ liệu quan trọng)
-- CẢNH BÁO: Chỉ chạy lệnh này sau khi đã chắc chắn migration thành công
-- DROP TABLE IF EXISTS doi_tra_hang_hoa;

-- 4. Cập nhật AUTO_INCREMENT cho ma_tra_hang
SET @max_id = (SELECT COALESCE(MAX(CAST(SUBSTRING(ma_tra_hang, 3) AS UNSIGNED)), 0) FROM tra_hang WHERE ma_tra_hang LIKE 'TH%');
SET @sql = CONCAT('ALTER TABLE tra_hang AUTO_INCREMENT = ', @max_id + 1);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;