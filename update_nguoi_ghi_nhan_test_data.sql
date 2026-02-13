-- Update nguoi_ghi_nhan for test data
UPDATE supplier_debt_payment SET nguoi_ghi_nhan = 'Admin User' WHERE id > 0 LIMIT 5;
UPDATE customer_debt_payment SET nguoi_ghi_nhan = 'Admin User' WHERE id > 0 LIMIT 5;

-- Verify updates
SELECT id, so_phieu_thanh_toan, nguoi_ghi_nhan FROM supplier_debt_payment LIMIT 5;
SELECT id, so_phieu_thu_hoi, nguoi_ghi_nhan FROM customer_debt_payment LIMIT 5;
