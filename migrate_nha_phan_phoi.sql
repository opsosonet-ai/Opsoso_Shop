-- Backup old data
CREATE TABLE nha_phan_phoi_backup AS SELECT * FROM nha_phan_phoi;

-- Drop foreign key constraints if any
-- ALTER TABLE phieu_xuat DROP FOREIGN KEY fk_phieu_xuat_nha_phan_phoi;

-- Drop old table
DROP TABLE IF EXISTS nha_phan_phoi;

-- Create new table with maNhaPhanPhoi as primary key
CREATE TABLE nha_phan_phoi (
    ma_nha_phan_phoi VARCHAR(50) NOT NULL PRIMARY KEY,
    ten_nha_phan_phoi VARCHAR(255) NOT NULL,
    id_cu BIGINT,
    so_dien_thoai VARCHAR(255),
    email VARCHAR(255),
    dia_chi VARCHAR(255),
    nguoi_lien_he VARCHAR(255),
    ma_so_thue VARCHAR(255),
    trang_thai VARCHAR(255),
    ghi_chu VARCHAR(255),
    linh_vuc VARCHAR(255)
);

-- Migrate data from backup to new table
INSERT INTO nha_phan_phoi (ma_nha_phan_phoi, ten_nha_phan_phoi, id_cu, so_dien_thoai, email, dia_chi, nguoi_lien_he, ma_so_thue, trang_thai, ghi_chu, linh_vuc)
SELECT ma_nha_phan_phoi, ten_nha_phan_phoi, id, so_dien_thoai, email, dia_chi, nguoi_lien_he, ma_so_thue, trang_thai, ghi_chu, linh_vuc
FROM nha_phan_phoi_backup
WHERE ma_nha_phan_phoi IS NOT NULL AND ma_nha_phan_phoi != '';

-- Verify migration
SELECT COUNT(*) as total_records FROM nha_phan_phoi;
SELECT * FROM nha_phan_phoi ORDER BY ma_nha_phan_phoi;
