#!/bin/bash

# Quick Verification After Migration
# Confirm all metrics columns are in place

echo "✅ METRICS MIGRATION - VERIFICATION REPORT"
echo "==========================================="
echo ""
echo "Database: oss"
echo "Date: $(date)"
echo ""

# Create temporary file for queries
TEMP_FILE="/tmp/metrics_check_$$.sql"
cat > "$TEMP_FILE" << 'EOF'
-- Check metrics columns
SELECT 
  'supplier_debt' as table_name,
  COUNT(*) as metrics_columns
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA='oss'
AND TABLE_NAME='supplier_debt'
AND COLUMN_NAME IN ('payload_size', 'payment_count', 'average_payment')
UNION ALL
SELECT 
  'customer_debt',
  COUNT(*)
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA='oss'
AND TABLE_NAME='customer_debt'
AND COLUMN_NAME IN ('payload_size', 'payment_count', 'average_payment')
UNION ALL
SELECT 
  'supplier_debt_payment',
  COUNT(*)
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA='oss'
AND TABLE_NAME='supplier_debt_payment'
AND COLUMN_NAME IN ('payload_size', 'transaction_hash')
UNION ALL
SELECT 
  'customer_debt_payment',
  COUNT(*)
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA='oss'
AND TABLE_NAME='customer_debt_payment'
AND COLUMN_NAME IN ('payload_size', 'transaction_hash');

-- Check indexes
SHOW INDEX FROM supplier_debt WHERE Column_name IN ('payment_count', 'average_payment');
SHOW INDEX FROM customer_debt WHERE Column_name IN ('payment_count', 'average_payment');
SHOW INDEX FROM supplier_debt_payment WHERE Column_name = 'transaction_hash';
SHOW INDEX FROM customer_debt_payment WHERE Column_name = 'transaction_hash';

-- Check data
SELECT 
  'supplier_debt' as table_name,
  COUNT(*) as record_count
FROM supplier_debt
UNION ALL
SELECT 'customer_debt', COUNT(*) FROM customer_debt
UNION ALL
SELECT 'supplier_debt_payment', COUNT(*) FROM supplier_debt_payment
UNION ALL
SELECT 'customer_debt_payment', COUNT(*) FROM customer_debt_payment;
EOF

echo "Running verification queries..."
echo ""

# Execute with password
mysql -u root -p oss < "$TEMP_FILE" 2>&1 | head -80

rm -f "$TEMP_FILE"

echo ""
echo "==========================================="
echo "✅ Verification complete!"
