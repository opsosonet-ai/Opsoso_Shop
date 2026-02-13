-- Check updated schema with metrics columns
USE oss;

-- Check supplier_debt table
SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'supplier_debt' 
ORDER BY ORDINAL_POSITION;

SELECT '---' as separator;

-- Check customer_debt table
SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'customer_debt' 
ORDER BY ORDINAL_POSITION;

SELECT '---' as separator;

-- Check supplier_debt_payment table
SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'supplier_debt_payment' 
ORDER BY ORDINAL_POSITION;

SELECT '---' as separator;

-- Check customer_debt_payment table
SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'customer_debt_payment' 
ORDER BY ORDINAL_POSITION;

SELECT '---' as separator;

-- Show table counts
SELECT 
  (SELECT COUNT(*) FROM supplier_debt) as supplier_debt_count,
  (SELECT COUNT(*) FROM customer_debt) as customer_debt_count,
  (SELECT COUNT(*) FROM supplier_debt_payment) as supplier_debt_payment_count,
  (SELECT COUNT(*) FROM customer_debt_payment) as customer_debt_payment_count;
