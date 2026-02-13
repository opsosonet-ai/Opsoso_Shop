#!/bin/bash

# Quick Database Check Script
# Kiểm tra kết nối database mà không cần password tương tác

DB_HOST="localhost"
DB_USER="root"
DB_NAME="oss"

echo "🔍 Checking Database Connection..."
echo ""

# Method 1: Try without password
if mysql -u "$DB_USER" -e "SELECT 1;" &>/dev/null 2>&1; then
    echo "✅ Connected via: mysql -u $DB_USER"
    
    # Check metrics columns
    echo ""
    echo "Checking metrics columns:"
    echo ""
    
    # Check supplier_debt
    SUPPLIER_COLS=$(mysql -u "$DB_USER" "$DB_NAME" -e "SELECT COUNT(*) as count FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='supplier_debt' AND COLUMN_NAME IN ('payload_size','payment_count','average_payment');" -sN 2>/dev/null)
    echo "supplier_debt: $SUPPLIER_COLS/3 metrics columns"
    
    # Check customer_debt
    CUSTOMER_COLS=$(mysql -u "$DB_USER" "$DB_NAME" -e "SELECT COUNT(*) as count FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='customer_debt' AND COLUMN_NAME IN ('payload_size','payment_count','average_payment');" -sN 2>/dev/null)
    echo "customer_debt: $CUSTOMER_COLS/3 metrics columns"
    
    # Check payment tables
    SUPP_PAYMENT=$(mysql -u "$DB_USER" "$DB_NAME" -e "SELECT COUNT(*) as count FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='supplier_debt_payment' AND COLUMN_NAME IN ('payload_size','transaction_hash');" -sN 2>/dev/null)
    echo "supplier_debt_payment: $SUPP_PAYMENT/2 columns"
    
    CUST_PAYMENT=$(mysql -u "$DB_USER" "$DB_NAME" -e "SELECT COUNT(*) as count FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='customer_debt_payment' AND COLUMN_NAME IN ('payload_size','transaction_hash');" -sN 2>/dev/null)
    echo "customer_debt_payment: $CUST_PAYMENT/2 columns"
    
    echo ""
    
    # If columns are missing, suggest migration
    if [ "$SUPPLIER_COLS" -lt 3 ] || [ "$CUSTOMER_COLS" -lt 3 ]; then
        echo "⚠️  Metrics columns not found!"
        echo ""
        echo "To apply migration, run:"
        echo "  mysql -u $DB_USER $DB_NAME < add_metrics_columns.sql"
    else
        echo "✅ All metrics columns present!"
    fi
    
elif mysql -h "$DB_HOST" -u "$DB_USER" "$DB_NAME" -e "SELECT 1;" &>/dev/null 2>&1; then
    echo "✅ Connected via: mysql -h $DB_HOST -u $DB_USER"
    echo "(Same as above connection)"
else
    echo "❌ Cannot connect to database"
    echo ""
    echo "Possible solutions:"
    echo "  1. Start MariaDB: systemctl start mariadb"
    echo "  2. Check credentials"
    echo "  3. Try: mysql -u root -p"
fi

echo ""
