#!/bin/bash

# Initialize Sample Data - Quick Script
# This script initializes sample data for the OSS application

set -e

echo "🔄 OSS Data Initialization Tool"
echo "================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
APP_URL="http://127.0.0.1:8080"
SETTINGS_PAGE="$APP_URL/admin/settings"
API_INIT="$APP_URL/api/settings/initialize-data"
ADMIN_USER="admin"
ADMIN_PASS="admin123"

echo -e "${YELLOW}Prerequisites:${NC}"
echo "1. Application must be running at $APP_URL"
echo "2. You must be logged in as admin"
echo ""

# Check if app is running
echo -e "${YELLOW}Checking application status...${NC}"
if curl -s --connect-timeout 2 "$APP_URL" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Application is running${NC}"
else
    echo -e "${RED}❌ Application is NOT running${NC}"
    echo "Please start the application first:"
    echo "  ./chay.sh"
    exit 1
fi

echo ""
echo "📋 Data Initialization Options:"
echo "================================"
echo ""
echo "1. Initialize via Web UI (Recommended)"
echo "   - Manual, visible progress, easy to monitor"
echo ""
echo "2. Initialize via API (Automated)"
echo "   - Requires authentication"
echo "   - Good for scripting"
echo ""
echo "3. Exit"
echo ""

read -p "Choose option (1-3): " choice

case $choice in
    1)
        echo ""
        echo -e "${YELLOW}Opening Settings page...${NC}"
        echo ""
        echo "Steps:"
        echo "1. The settings page will open in your browser"
        echo "2. Look for the '🔄 Initialize Sample Data' button"
        echo "3. Click it to initialize data"
        echo "4. Wait for the success message"
        echo ""
        echo "Settings URL: $SETTINGS_PAGE"
        echo ""
        
        # Try to open in browser
        if command -v xdg-open &> /dev/null; then
            xdg-open "$SETTINGS_PAGE" 2>/dev/null || true
        elif command -v open &> /dev/null; then
            open "$SETTINGS_PAGE" 2>/dev/null || true
        else
            echo "⚠️ Could not auto-open browser. Please visit:"
            echo "   $SETTINGS_PAGE"
        fi
        
        echo ""
        echo -e "${GREEN}✅ Instructions displayed${NC}"
        echo ""
        ;;
        
    2)
        echo ""
        echo -e "${YELLOW}Initializing via API...${NC}"
        echo ""
        
        # Get auth token (simplified - assumes credentials)
        echo "Note: This requires proper authentication"
        echo "If you get a 401 error, please use Option 1 instead"
        echo ""
        
        echo "Sending initialization request..."
        RESPONSE=$(curl -s -X POST "$API_INIT" \
          -H "Content-Type: application/json" \
          -d '{}' 2>&1 || echo "")
        
        if echo "$RESPONSE" | grep -q "success\|✅\|已"; then
            echo -e "${GREEN}✅ Data initialization successful!${NC}"
            echo ""
            echo "Response:"
            echo "$RESPONSE" | head -20
        else
            echo -e "${YELLOW}⚠️ Response:${NC}"
            echo "$RESPONSE" | head -10
            echo ""
            echo "If you got a 401 error, please use the Web UI (Option 1) instead"
        fi
        
        echo ""
        ;;
        
    3)
        echo "Exiting..."
        exit 0
        ;;
        
    *)
        echo -e "${RED}Invalid option. Please choose 1-3.${NC}"
        exit 1
        ;;
esac

echo ""
echo "📊 Verification:"
echo "==============="
echo ""
echo "After initialization, verify data was created:"
echo ""
echo "1. Check console output for:"
echo "   ✅ Đã tạo... (records created)"
echo "   🎉 Hoàn tất khởi tạo (initialization complete)"
echo ""
echo "2. Visit pages to verify data:"
echo "   - Products: $APP_URL/admin/hang-hoa"
echo "   - Returns: $APP_URL/admin/tra-hang"
echo "   - Customers: $APP_URL/admin/khach-hang"
echo "   - Invoices: $APP_URL/admin/phieu-xuat"
echo "   - Users: $APP_URL/admin/users"
echo ""
echo "3. Database query:"
echo "   SELECT COUNT(*) FROM hang_hoa; (should be 8)"
echo ""
echo -e "${GREEN}✅ Done!${NC}"
echo ""

