-- Migration: V1__Create_Debt_Tables.sql
-- Ngày: November 1, 2025
-- Mục Đích: Tạo các bảng quản lý công nợ (Debt Management)
-- Trạng Thái: Chạy đầu tiên sau khi update code

-- ========== TABLE: supplier_debt ==========
-- Bảng lưu trữ Nợ Nhà Phân Phối (mua hàng trước trả sau)

CREATE TABLE IF NOT EXISTS supplier_debt (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID duy nhất',
    ma_nha_phan_phoi VARCHAR(50) NOT NULL COMMENT 'Mã nhà phân phối nợ tiền',
    so_phieu_xuat_chi VARCHAR(50) NOT NULL UNIQUE COMMENT 'Số phiếu xuất chi (từ hóa đơn)',
    ngay_tao_no DATETIME NOT NULL COMMENT 'Ngày tạo công nợ',
    tong_tien_no DECIMAL(15, 2) NOT NULL COMMENT 'Tổng tiền nợ ban đầu',
    tong_tien_da_thanh_toan DECIMAL(15, 2) DEFAULT 0 COMMENT 'Tiền đã thanh toán',
    tong_tien_con_no DECIMAL(15, 2) NOT NULL COMMENT 'Tiền còn nợ (tính tự động)',
    trang_thai VARCHAR(30) NOT NULL DEFAULT 'DANG_NO' COMMENT 'Trạng thái: DANG_NO, THANH_TOAN_TUAN_TUAN, DA_THANH_TOAN_HET, QUA_HAN',
    ngay_han_chot DATE COMMENT 'Ngày hạn chót thanh toán',
    ghi_chu TEXT COMMENT 'Ghi chú bổ sung',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo bản ghi',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Ngày cập nhật bản ghi',
    
    -- Foreign Key
    FOREIGN KEY (ma_nha_phan_phoi) REFERENCES nha_phan_phoi(ma_nha_phan_phoi) ON DELETE RESTRICT,
    
    -- Index để tối ưu hiệu năng
    INDEX idx_ma_nha_phan_phoi (ma_nha_phan_phoi),
    INDEX idx_trang_thai (trang_thai),
    INDEX idx_ngay_han_chot (ngay_han_chot),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci 
COMMENT='Bảng quản lý Nợ Nhà Phân Phối';

-- ========== TABLE: supplier_debt_payment ==========
-- Bảng lưu trữ Chi Tiết Thanh Toán Nợ Nhà Phân Phối
-- Mỗi lần thanh toán được ghi lại một bản ghi

CREATE TABLE IF NOT EXISTS supplier_debt_payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID duy nhất',
    supplier_debt_id BIGINT NOT NULL COMMENT 'Công nợ tương ứng',
    so_phieu_thanh_toan VARCHAR(50) NOT NULL UNIQUE COMMENT 'Số phiếu thanh toán duy nhất',
    ngay_thanh_toan DATETIME NOT NULL COMMENT 'Ngày thanh toán',
    so_tien_thanh_toan DECIMAL(15, 2) NOT NULL COMMENT 'Số tiền thanh toán',
    phuong_thuc_thanh_toan VARCHAR(30) COMMENT 'Phương thức: TIEN_MAT, CHUYEN_KHOAN, CHI_TIEU',
    so_bang_ke VARCHAR(100) COMMENT 'Số bảng kê (dùng khi chuyển khoản)',
    ghi_chu TEXT COMMENT 'Ghi chú bổ sung',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo bản ghi',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Ngày cập nhật bản ghi',
    
    -- Foreign Key
    FOREIGN KEY (supplier_debt_id) REFERENCES supplier_debt(id) ON DELETE CASCADE,
    
    -- Index
    INDEX idx_supplier_debt (supplier_debt_id),
    INDEX idx_ngay_thanh_toan (ngay_thanh_toan),
    INDEX idx_phuong_thuc (phuong_thuc_thanh_toan)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci 
COMMENT='Bảng quản lý Chi Tiết Thanh Toán Nợ Nhà Phân Phối';

-- ========== TABLE: customer_debt ==========
-- Bảng lưu trữ Nợ Khách Hàng (bán hàng trước thu sau)

CREATE TABLE IF NOT EXISTS customer_debt (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID duy nhất',
    khach_hang_id BIGINT NOT NULL COMMENT 'Khách hàng nợ tiền',
    so_phieu_xuat_ban VARCHAR(50) NOT NULL UNIQUE COMMENT 'Số phiếu xuất bán (từ hóa đơn)',
    ngay_tao_no DATETIME NOT NULL COMMENT 'Ngày tạo công nợ',
    tong_tien_no DECIMAL(15, 2) NOT NULL COMMENT 'Tổng tiền nợ ban đầu (khoản phải thu)',
    tong_tien_da_thanh_toan DECIMAL(15, 2) DEFAULT 0 COMMENT 'Tiền đã thu hồi',
    tong_tien_con_no DECIMAL(15, 2) NOT NULL COMMENT 'Tiền còn nợ (tính tự động)',
    trang_thai VARCHAR(30) NOT NULL DEFAULT 'DANG_NO' COMMENT 'Trạng thái: DANG_NO, THANH_TOAN_TUAN_TUAN, DA_THANH_TOAN_HET, QUA_HAN',
    ngay_han_chot DATE COMMENT 'Ngày hạn chót thanh toán',
    ghi_chu TEXT COMMENT 'Ghi chú bổ sung',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo bản ghi',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Ngày cập nhật bản ghi',
    
    -- Foreign Key
    FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id) ON DELETE RESTRICT,
    
    -- Index
    INDEX idx_khach_hang (khach_hang_id),
    INDEX idx_trang_thai (trang_thai),
    INDEX idx_ngay_han_chot (ngay_han_chot),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci 
COMMENT='Bảng quản lý Nợ Khách Hàng';

-- ========== TABLE: customer_debt_payment ==========
-- Bảng lưu trữ Chi Tiết Thu Hồi Nợ Khách Hàng
-- Mỗi lần thu hồi được ghi lại một bản ghi

CREATE TABLE IF NOT EXISTS customer_debt_payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID duy nhất',
    customer_debt_id BIGINT NOT NULL COMMENT 'Công nợ tương ứng',
    so_phieu_thu_hoi VARCHAR(50) NOT NULL UNIQUE COMMENT 'Số phiếu thu hồi duy nhất',
    ngay_thu_hoi DATETIME NOT NULL COMMENT 'Ngày thu hồi tiền',
    so_tien_thu_hoi DECIMAL(15, 2) NOT NULL COMMENT 'Số tiền thu hồi',
    phuong_thuc_thu_hoi VARCHAR(30) COMMENT 'Phương thức: TIEN_MAT, CHUYEN_KHOAN, CHI_TIEU',
    ghi_chu TEXT COMMENT 'Ghi chú bổ sung',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo bản ghi',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Ngày cập nhật bản ghi',
    
    -- Foreign Key
    FOREIGN KEY (customer_debt_id) REFERENCES customer_debt(id) ON DELETE CASCADE,
    
    -- Index
    INDEX idx_customer_debt (customer_debt_id),
    INDEX idx_ngay_thu_hoi (ngay_thu_hoi),
    INDEX idx_phuong_thuc (phuong_thuc_thu_hoi)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci 
COMMENT='Bảng quản lý Chi Tiết Thu Hồi Nợ Khách Hàng';

-- ========== VERIFY TABLES CREATED ==========
-- Kiểm tra các bảng đã được tạo thành công

SELECT 'Bảng supplier_debt' as `Tên Bảng`, COUNT(*) as `Số Dòng`
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'supplier_debt'
UNION ALL
SELECT 'Bảng supplier_debt_payment', COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'supplier_debt_payment'
UNION ALL
SELECT 'Bảng customer_debt', COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'customer_debt'
UNION ALL
SELECT 'Bảng customer_debt_payment', COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'customer_debt_payment';

-- ========== END MIGRATION ==========
-- Tất cả bảng đã được tạo thành công!
