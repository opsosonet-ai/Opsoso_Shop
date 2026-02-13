-- Thêm cột hang_hoa_doi_moi_id vào bảng doi_tra_hang_hoa
USE oss;

ALTER TABLE doi_tra_hang_hoa 
ADD COLUMN hang_hoa_doi_moi_id BIGINT,
ADD CONSTRAINT fk_doi_tra_hang_hoa_doi_moi 
    FOREIGN KEY (hang_hoa_doi_moi_id) REFERENCES hang_hoa(id);

-- Kiểm tra cấu trúc bảng
DESCRIBE doi_tra_hang_hoa;