-- Fix foreign key constraint on chi_tiet_phieu_xuat.hang_hoa_id
-- Allow products to be deleted while preserving invoice history (set to NULL)

-- First, drop the old constraint
ALTER TABLE chi_tiet_phieu_xuat DROP FOREIGN KEY FK1a3gk6ui6wgch6y0shtxq135i;

-- Recreate the constraint with ON DELETE SET NULL
ALTER TABLE chi_tiet_phieu_xuat 
ADD CONSTRAINT FK1a3gk6ui6wgch6y0shtxq135i 
FOREIGN KEY (hang_hoa_id) REFERENCES hang_hoa (id) ON DELETE SET NULL;

-- Verify the constraint was updated
SHOW CREATE TABLE chi_tiet_phieu_xuat\G
