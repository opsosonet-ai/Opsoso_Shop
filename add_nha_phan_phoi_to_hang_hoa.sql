-- Add ma_nha_phan_phoi column to hang_hoa table if it doesn't exist
ALTER TABLE hang_hoa 
ADD COLUMN IF NOT EXISTS ma_nha_phan_phoi VARCHAR(50);

-- Drop existing foreign key if it exists
ALTER TABLE hang_hoa DROP FOREIGN KEY IF EXISTS fk_hang_hoa_nha_phan_phoi;

-- Add foreign key constraint
ALTER TABLE hang_hoa 
ADD CONSTRAINT fk_hang_hoa_nha_phan_phoi
FOREIGN KEY (ma_nha_phan_phoi) 
REFERENCES nha_phan_phoi(ma_nha_phan_phoi) 
ON DELETE SET NULL 
ON UPDATE CASCADE;
