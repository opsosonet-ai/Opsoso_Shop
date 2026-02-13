#!/bin/bash

# Apply Metrics Migration Script
# Purpose: Apply database migration for metrics columns
# Date: 2025-11-02

echo "=========================================="
echo "📊 Metrics Database Migration"
echo "=========================================="
echo ""
echo "This script will add metrics columns to:"
echo "  - supplier_debt"
echo "  - customer_debt"
echo "  - supplier_debt_payment"
echo "  - customer_debt_payment"
echo ""

# Check if migration file exists
if [ ! -f "add_metrics_columns.sql" ]; then
    echo "❌ Error: add_metrics_columns.sql not found"
    echo "   Make sure you're in the correct directory"
    exit 1
fi

echo "Enter your database password (for root user):"
read -s DB_PASSWORD

# Create a temporary MySQL config file
MYSQL_CONFIG="/tmp/.mysql_config_$$"
cat > "$MYSQL_CONFIG" << EOF
[client]
user=root
password='$DB_PASSWORD'
host=localhost
EOF

chmod 600 "$MYSQL_CONFIG"

# Apply migration
echo ""
echo "Applying migration..."

if mysql --defaults-extra-file="$MYSQL_CONFIG" oss < add_metrics_columns.sql 2>&1; then
    echo ""
    echo "✅ Migration applied successfully!"
    echo ""
    
    # Verify columns
    echo "Verifying columns..."
    echo ""
    
    echo "supplier_debt:"
    mysql --defaults-extra-file="$MYSQL_CONFIG" oss -e "DESC supplier_debt\G" 2>/dev/null | grep -E "payload_size|payment_count|average_payment" || echo "  ⚠️  Columns may be missing"
    
    echo ""
    echo "customer_debt:"
    mysql --defaults-extra-file="$MYSQL_CONFIG" oss -e "DESC customer_debt\G" 2>/dev/null | grep -E "payload_size|payment_count|average_payment" || echo "  ⚠️  Columns may be missing"
    
    echo ""
    echo "supplier_debt_payment:"
    mysql --defaults-extra-file="$MYSQL_CONFIG" oss -e "DESC supplier_debt_payment\G" 2>/dev/null | grep -E "payload_size|transaction_hash" || echo "  ⚠️  Columns may be missing"
    
    echo ""
    echo "customer_debt_payment:"
    mysql --defaults-extra-file="$MYSQL_CONFIG" oss -e "DESC customer_debt_payment\G" 2>/dev/null | grep -E "payload_size|transaction_hash" || echo "  ⚠️  Columns may be missing"
    
    echo ""
    echo "✅ Verification complete!"
else
    echo ""
    echo "❌ Migration failed!"
    echo "   Check your password and database connection"
fi

# Clean up
rm -f "$MYSQL_CONFIG"

echo ""
echo "=========================================="
