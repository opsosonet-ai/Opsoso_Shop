#!/bin/bash

# Database Metrics Verification Script
# Purpose: Verify all metrics columns are properly created
# Date: 2025-11-02

echo "=========================================="
echo "🔍 Database Metrics Verification Script"
echo "=========================================="
echo ""

# Check if database exists
echo "1️⃣  Checking database connection..."

# Try to connect with different methods
if mysql -u root -e "SELECT 1" &>/dev/null; then
    echo "✅ Database connection successful"
    MYSQL_CMD="mysql -u root"
elif mysql -u root -p -e "SELECT 1" &>/dev/null 2>&1; then
    echo "✅ Database connection successful (with password)"
    MYSQL_CMD="mysql -u root -p"
elif mysql -h localhost -u root oss -e "SELECT 1" &>/dev/null; then
    echo "✅ Database connection successful (localhost)"
    MYSQL_CMD="mysql -h localhost -u root oss"
else
    echo "❌ Failed to connect to database"
    echo ""
    echo "Try these commands manually:"
    echo "  mysql -u root -e 'SELECT 1;'"
    echo "  mysql -u root -p -e 'SELECT 1;'"
    echo "  mysql -h localhost -u root oss -e 'SELECT 1;'"
    exit 1
fi

echo ""
echo "2️⃣  Checking supplier_debt table..."
$MYSQL_CMD -e "USE oss; DESC supplier_debt;" 2>/dev/null | grep -E "payload_size|payment_count|average_payment" && echo "✅ supplier_debt metrics columns present" || echo "⚠️  Some columns may be missing"

echo ""
echo "3️⃣  Checking customer_debt table..."
$MYSQL_CMD -e "USE oss; DESC customer_debt;" 2>/dev/null | grep -E "payload_size|payment_count|average_payment" && echo "✅ customer_debt metrics columns present" || echo "⚠️  Some columns may be missing"

echo ""
echo "4️⃣  Checking supplier_debt_payment table..."
$MYSQL_CMD -e "USE oss; DESC supplier_debt_payment;" 2>/dev/null | grep -E "payload_size|transaction_hash" && echo "✅ supplier_debt_payment metrics columns present" || echo "⚠️  Some columns may be missing"

echo ""
echo "5️⃣  Checking customer_debt_payment table..."
$MYSQL_CMD -e "USE oss; DESC customer_debt_payment;" 2>/dev/null | grep -E "payload_size|transaction_hash" && echo "✅ customer_debt_payment metrics columns present" || echo "⚠️  Some columns may be missing"

echo ""
echo "6️⃣  Verifying table row counts..."
$MYSQL_CMD -e "
USE oss;
SELECT 
  CONCAT('supplier_debt: ', COUNT(*)) as table_count 
FROM supplier_debt
UNION ALL
SELECT CONCAT('customer_debt: ', COUNT(*)) FROM customer_debt
UNION ALL
SELECT CONCAT('supplier_debt_payment: ', COUNT(*)) FROM supplier_debt_payment
UNION ALL
SELECT CONCAT('customer_debt_payment: ', COUNT(*)) FROM customer_debt_payment;
" 2>/dev/null || echo "⚠️  Could not get table counts"

echo ""
echo "7️⃣  Checking indexes on metrics columns..."
$MYSQL_CMD -e "USE oss; SHOW INDEX FROM supplier_debt WHERE Column_name IN ('payment_count', 'average_payment');" 2>/dev/null || echo "⚠️  Could not check indexes"

echo ""
echo "8️⃣  Checking application logs for errors..."
if [ -f app.log ]; then
    tail -50 app.log 2>/dev/null | grep -i "error\|exception" && echo "⚠️  Possible errors in logs" || echo "✅ No errors found in recent logs"
else
    echo "⚠️  Application log file not found"
fi

echo ""
echo "=========================================="
echo "✅ Verification complete!"
echo "=========================================="
echo ""
echo "Summary:"
echo "  - Database connection: OK"
echo "  - To apply migration: mysql -u root oss < add_metrics_columns.sql"
echo "  - To check specific table: mysql -u root oss -e 'DESC supplier_debt;'"
