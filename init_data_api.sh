#!/bin/bash

# Direct API Data Initialization
# This script uses the API endpoint to initialize data
# No browser needed, pure command-line approach

set -e

echo "🚀 OSS Direct API Data Initialization"
echo "======================================"
echo ""

# Configuration
APP_URL="http://127.0.0.1:8080"
API_ENDPOINT="$APP_URL/api/settings/initialize-data"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Check if app is running
echo -e "${BLUE}Step 1: Checking application status...${NC}"
if ! curl -s --connect-timeout 2 "$APP_URL" > /dev/null 2>&1; then
    echo -e "${RED}❌ Application is NOT running at $APP_URL${NC}"
    echo ""
    echo "Please start the application first:"
    echo "  ./chay.sh"
    exit 1
fi
echo -e "${GREEN}✅ Application is running${NC}"

echo ""
echo -e "${BLUE}Step 2: Initializing sample data via API...${NC}"
echo "URL: $API_ENDPOINT"
echo ""

# Make the API request
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_ENDPOINT" \
  -H "Content-Type: application/json" \
  -d '{}')

# Extract HTTP status code
HTTP_STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

echo "HTTP Status: $HTTP_STATUS"
echo ""

# Check response
if [ "$HTTP_STATUS" = "200" ]; then
    echo -e "${GREEN}✅ SUCCESS! Data initialization completed!${NC}"
    echo ""
    echo "Response:"
    echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
    
elif [ "$HTTP_STATUS" = "401" ] || [ "$HTTP_STATUS" = "403" ]; then
    echo -e "${YELLOW}⚠️  Authentication required${NC}"
    echo ""
    echo "The API endpoint requires authentication."
    echo "Please use the Web UI instead:"
    echo ""
    echo "1. Go to: $APP_URL/admin/settings"
    echo "2. Login with: admin / admin123"
    echo "3. Click: '🔄 Initialize Sample Data' button"
    echo ""
    exit 1
    
elif [ "$HTTP_STATUS" = "400" ]; then
    echo -e "${YELLOW}⚠️  Bad Request${NC}"
    echo ""
    echo "Response:"
    echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
    echo ""
    echo "Possible reasons:"
    echo "- Data already exists"
    echo "- Database connection issue"
    echo "- Invalid request format"
    exit 1
    
else
    echo -e "${RED}❌ Unexpected response (HTTP $HTTP_STATUS)${NC}"
    echo ""
    echo "Response:"
    echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
    exit 1
fi

echo ""
echo -e "${BLUE}Step 3: Verification${NC}"
echo ""

# Wait a moment for data to be written
sleep 2

# Check database
echo "Checking database..."
CHECK=$(mysql -u root -pJavaBean@ -e "SELECT COUNT(*) as count FROM oss.hang_hoa;" 2>/dev/null | tail -1)
PRODUCT_COUNT=$CHECK

echo -e "  Products: ${GREEN}$PRODUCT_COUNT records${NC}"

if [ "$PRODUCT_COUNT" -gt 0 ]; then
    echo ""
    echo -e "${GREEN}✅ Data verified in database!${NC}"
    echo ""
    echo "Sample data created:"
    echo "  - Users: 4 accounts (root, admin, manager, user)"
    echo "  - Products: 8 items"
    echo "  - Customers: 3 records"
    echo "  - Employees: 3 staff members"
    echo "  - Suppliers: 2 vendors"
    echo "  - Invoices: 2 documents"
    echo "  - Returns: 3 records"
    echo "  - Exchanges: 2 records"
    echo ""
else
    echo -e "${YELLOW}⚠️  Could not verify data (MySQL check failed)${NC}"
    echo ""
    echo "Data may still have been initialized. Please verify in application:"
    echo "  $APP_URL/admin/hang-hoa"
fi

echo ""
echo -e "${BLUE}Step 4: Access Application${NC}"
echo ""
echo "You can now:"
echo "  1. View Products: $APP_URL/admin/hang-hoa"
echo "  2. View Returns: $APP_URL/admin/tra-hang"
echo "  3. View Customers: $APP_URL/admin/khach-hang"
echo "  4. View Invoices: $APP_URL/admin/phieu-xuat"
echo "  5. View Users: $APP_URL/admin/users"
echo ""

echo -e "${GREEN}✅ Data initialization complete!${NC}"
echo ""

