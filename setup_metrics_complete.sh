#!/bin/bash

# Complete Database Setup Script
# Handles all scenarios for metrics migration

set -e

echo "╔════════════════════════════════════════════════════════╗"
echo "║        Database Metrics Setup - Complete               ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check MariaDB status
echo "Step 1: Checking MariaDB status..."
if systemctl is-active --quiet mariadb; then
    echo -e "${GREEN}✅ MariaDB is running${NC}"
else
    echo -e "${YELLOW}⚠️  MariaDB is not running. Attempting to start...${NC}"
    if sudo systemctl start mariadb 2>/dev/null; then
        echo -e "${GREEN}✅ MariaDB started${NC}"
        sleep 2
    else
        echo -e "${RED}❌ Failed to start MariaDB${NC}"
        echo "Try: sudo systemctl start mariadb"
        exit 1
    fi
fi

echo ""
echo "Step 2: Testing database connection..."

# Try different connection methods
MYSQL_CMD=""

if mysql -u root -e "SELECT 1;" &>/dev/null 2>&1; then
    MYSQL_CMD="mysql -u root"
    echo -e "${GREEN}✅ Connected without password${NC}"
elif mysql -u root -p -e "SELECT 1;" &>/dev/null 2>&1; then
    MYSQL_CMD="mysql -u root -p"
    echo -e "${GREEN}✅ Connected with password${NC}"
else
    echo -e "${RED}❌ Cannot connect to database${NC}"
    echo ""
    echo "Try these commands:"
    echo "  mysql -u root -p -e 'SELECT 1;'"
    echo "  sudo mysql -u root -e 'SELECT 1;'"
    exit 1
fi

echo ""
echo "Step 3: Checking metrics columns..."

# Query to check columns
if [ "$MYSQL_CMD" = "mysql -u root" ]; then
    SUPPLIER_COUNT=$($MYSQL_CMD -e "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='oss' AND TABLE_NAME='supplier_debt' AND COLUMN_NAME IN ('payload_size','payment_count','average_payment');" 2>/dev/null | tail -1 || echo "0")
else
    SUPPLIER_COUNT=$($MYSQL_CMD -e "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='oss' AND TABLE_NAME='supplier_debt' AND COLUMN_NAME IN ('payload_size','payment_count','average_payment');" 2>/dev/null | tail -1 || echo "0")
fi

echo "supplier_debt metrics columns: $SUPPLIER_COUNT/3"

if [ "$SUPPLIER_COUNT" -lt 3 ]; then
    echo ""
    echo -e "${YELLOW}⚠️  Metrics columns are missing!${NC}"
    echo ""
    echo "Step 4: Applying migration..."
    
    if [ "$MYSQL_CMD" = "mysql -u root" ]; then
        if mysql -u root oss < add_metrics_columns.sql 2>&1; then
            echo -e "${GREEN}✅ Migration applied successfully!${NC}"
        else
            echo -e "${RED}❌ Migration failed${NC}"
            exit 1
        fi
    else
        echo "Please enter your database password:"
        if mysql -u root -p oss < add_metrics_columns.sql 2>&1; then
            echo -e "${GREEN}✅ Migration applied successfully!${NC}"
        else
            echo -e "${RED}❌ Migration failed${NC}"
            exit 1
        fi
    fi
else
    echo -e "${GREEN}✅ All metrics columns present!${NC}"
fi

echo ""
echo "Step 5: Final verification..."
echo ""

if [ "$MYSQL_CMD" = "mysql -u root" ]; then
    mysql -u root oss << 'EOF'
SELECT 
  TABLE_NAME,
  GROUP_CONCAT(COLUMN_NAME) as metrics_columns
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA='oss'
AND TABLE_NAME IN ('supplier_debt', 'customer_debt', 'supplier_debt_payment', 'customer_debt_payment')
AND COLUMN_NAME IN ('payload_size', 'payment_count', 'average_payment', 'transaction_hash')
GROUP BY TABLE_NAME
ORDER BY TABLE_NAME;
EOF
else
    echo "Run with password to see verification details"
fi

echo ""
echo "╔════════════════════════════════════════════════════════╗"
echo "║                  Setup Complete! ✅                     ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""
echo "Next steps:"
echo "  1. Review: cat METRICS_QUICK_REFERENCE.md"
echo "  2. Test: ./verify_metrics.sh"
echo "  3. Run: curl http://localhost:8080/auth/login"
