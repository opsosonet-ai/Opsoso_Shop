-- Database Migration: Add Metrics Columns to Debt Tables
-- Date: 2025-11-02
-- Purpose: Add payload size, payment count, and average payment metrics for analytics

USE oss;

-- ========== SUPPLIER DEBT METRICS ==========

-- Add payload_size column to supplier_debt
ALTER TABLE supplier_debt ADD COLUMN IF NOT EXISTS payload_size BIGINT DEFAULT 0 COMMENT 'Kích thước dữ liệu thanh toán (Payload Size)';

-- Add payment_count column to supplier_debt
ALTER TABLE supplier_debt ADD COLUMN IF NOT EXISTS payment_count INT DEFAULT 0 COMMENT 'Số lượng thanh toán';

-- Add average_payment column to supplier_debt
ALTER TABLE supplier_debt ADD COLUMN IF NOT EXISTS average_payment DECIMAL(15, 2) DEFAULT 0.00 COMMENT 'Tiền thanh toán trung bình';

-- Create index for metrics columns in supplier_debt
ALTER TABLE supplier_debt ADD INDEX IF NOT EXISTS idx_payment_count (payment_count);
ALTER TABLE supplier_debt ADD INDEX IF NOT EXISTS idx_average_payment (average_payment);

-- ========== CUSTOMER DEBT METRICS ==========

-- Add payload_size column to customer_debt
ALTER TABLE customer_debt ADD COLUMN IF NOT EXISTS payload_size BIGINT DEFAULT 0 COMMENT 'Kích thước dữ liệu thanh toán (Payload Size)';

-- Add payment_count column to customer_debt
ALTER TABLE customer_debt ADD COLUMN IF NOT EXISTS payment_count INT DEFAULT 0 COMMENT 'Số lượng thanh toán';

-- Add average_payment column to customer_debt
ALTER TABLE customer_debt ADD COLUMN IF NOT EXISTS average_payment DECIMAL(15, 2) DEFAULT 0.00 COMMENT 'Tiền thanh toán trung bình';

-- Create index for metrics columns in customer_debt
ALTER TABLE customer_debt ADD INDEX IF NOT EXISTS idx_customer_payment_count (payment_count);
ALTER TABLE customer_debt ADD INDEX IF NOT EXISTS idx_customer_average_payment (average_payment);

-- ========== SUPPLIER DEBT PAYMENT METRICS ==========

-- Add payload_size column to supplier_debt_payment
ALTER TABLE supplier_debt_payment ADD COLUMN IF NOT EXISTS payload_size BIGINT DEFAULT 0 COMMENT 'Kích thước dữ liệu thanh toán (Payload Size)';

-- Add transaction_hash column to supplier_debt_payment
ALTER TABLE supplier_debt_payment ADD COLUMN IF NOT EXISTS transaction_hash VARCHAR(64) COMMENT 'Mã xác thực thanh toán';

-- Create index for transaction_hash in supplier_debt_payment
ALTER TABLE supplier_debt_payment ADD INDEX IF NOT EXISTS idx_transaction_hash (transaction_hash);

-- ========== CUSTOMER DEBT PAYMENT METRICS ==========

-- Add payload_size column to customer_debt_payment
ALTER TABLE customer_debt_payment ADD COLUMN IF NOT EXISTS payload_size BIGINT DEFAULT 0 COMMENT 'Kích thước dữ liệu thanh toán (Payload Size)';

-- Add transaction_hash column to customer_debt_payment
ALTER TABLE customer_debt_payment ADD COLUMN IF NOT EXISTS transaction_hash VARCHAR(64) COMMENT 'Mã xác thực thanh toán';

-- Create index for transaction_hash in customer_debt_payment
ALTER TABLE customer_debt_payment ADD INDEX IF NOT EXISTS idx_customer_transaction_hash (transaction_hash);

-- ========== VERIFICATION ==========

-- Verify columns were added
SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME IN ('supplier_debt', 'customer_debt', 'supplier_debt_payment', 'customer_debt_payment')
AND COLUMN_NAME IN ('payload_size', 'payment_count', 'average_payment', 'transaction_hash')
ORDER BY TABLE_NAME, ORDINAL_POSITION;

-- Show table structure
SHOW CREATE TABLE supplier_debt\G
SHOW CREATE TABLE customer_debt\G
SHOW CREATE TABLE supplier_debt_payment\G
SHOW CREATE TABLE customer_debt_payment\G
