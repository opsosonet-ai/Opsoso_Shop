-- Create warranty_timeline table
CREATE TABLE IF NOT EXISTS warranty_timeline (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warranty_id BIGINT NOT NULL,
    buoc_thuc_hien VARCHAR(255) NOT NULL COMMENT 'Nhận thiết bị, Gửi nhà phân phối/hãng, Lấy về, Trả khách hàng',
    thoi_gian_thuc_hien DATETIME NOT NULL,
    ghi_chu LONGTEXT,
    nguoi_thuc_hien VARCHAR(100),
    ngay_tao DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_warranty_timeline_warranty FOREIGN KEY (warranty_id) REFERENCES warranty(id) ON DELETE CASCADE,
    INDEX idx_warranty_id (warranty_id),
    INDEX idx_buoc_thuc_hien (buoc_thuc_hien),
    INDEX idx_thoi_gian_thuc_hien (thoi_gian_thuc_hien)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tracking quy trình thời gian bảo hành';
