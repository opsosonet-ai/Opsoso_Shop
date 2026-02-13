-- Add nguoi_ghi_nhan field to supplier_debt_payment table
ALTER TABLE supplier_debt_payment ADD COLUMN nguoi_ghi_nhan VARCHAR(255) NULL AFTER ghi_chu;

-- Add nguoi_ghi_nhan field to customer_debt_payment table
ALTER TABLE customer_debt_payment ADD COLUMN nguoi_ghi_nhan VARCHAR(255) NULL AFTER ghi_chu;

-- Verify columns were added
SHOW COLUMNS FROM supplier_debt_payment WHERE Field = 'nguoi_ghi_nhan';
SHOW COLUMNS FROM customer_debt_payment WHERE Field = 'nguoi_ghi_nhan';
