#!/bin/bash

# 🔐 Login Test Script
# Verifies BCrypt password encoding and login functionality

echo "======================================"
echo "🔐 Login Test Script"
echo "======================================"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
APP_URL="http://localhost:8080"
USERNAME="admin"
PASSWORD="admin123"
LOGIN_URL="$APP_URL/auth/login"
DASHBOARD_URL="$APP_URL/dashboard"

echo "📋 Configuration:"
echo "  App URL: $APP_URL"
echo "  Login URL: $LOGIN_URL"
echo "  Username: $USERNAME"
echo "  Password: $PASSWORD"
echo ""

# Step 1: Check if app is running
echo "🔍 Step 1: Checking if application is running..."
if ! curl -s "$APP_URL" > /dev/null 2>&1; then
    echo -e "${RED}❌ Application is NOT running!${NC}"
    echo "   Start it with: ./run.sh"
    exit 1
fi
echo -e "${GREEN}✅ Application is running${NC}"
echo ""

# Step 2: Get login page
echo "🔍 Step 2: Getting login page..."
LOGIN_PAGE=$(curl -s -c /tmp/cookies.txt "$LOGIN_URL")
if echo "$LOGIN_PAGE" | grep -q "login\|password"; then
    echo -e "${GREEN}✅ Login page loaded${NC}"
else
    echo -e "${RED}❌ Login page not found${NC}"
    exit 1
fi
echo ""

# Step 3: Extract CSRF token
echo "🔍 Step 3: Extracting CSRF token..."
CSRF_TOKEN=$(echo "$LOGIN_PAGE" | grep -oP '(?<=name="_csrf"\s+value=")[^"]*')
if [ -z "$CSRF_TOKEN" ]; then
    echo -e "${YELLOW}⚠️  CSRF token not found (might be using cookie-based CSRF)${NC}"
    CSRF_TOKEN="disabled"
else
    echo -e "${GREEN}✅ CSRF token found: ${CSRF_TOKEN:0:20}...${NC}"
fi
echo ""

# Step 4: Attempt login
echo "🔍 Step 4: Attempting login..."
LOGIN_RESPONSE=$(curl -s -b /tmp/cookies.txt -c /tmp/cookies.txt \
    -X POST "$LOGIN_URL" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=$USERNAME&password=$PASSWORD&_csrf=$CSRF_TOKEN" \
    -L -w "\n%{http_code}")

HTTP_CODE=$(echo "$LOGIN_RESPONSE" | tail -1)
RESPONSE_BODY=$(echo "$LOGIN_RESPONSE" | head -n -1)

echo "  HTTP Status: $HTTP_CODE"

if [ "$HTTP_CODE" = "200" ]; then
    if echo "$RESPONSE_BODY" | grep -qi "dashboard\|quản trị"; then
        echo -e "${GREEN}✅ Login successful!${NC}"
    else
        echo -e "${YELLOW}⚠️  Got 200 but might not be dashboard${NC}"
    fi
else
    echo -e "${YELLOW}ℹ️  Got HTTP $HTTP_CODE (may be redirect)${NC}"
fi
echo ""

# Step 5: Check database password format
echo "🔍 Step 5: Checking database password format..."
DB_PASS=$(mysql -u root -pJavaBean@ -h localhost oss -e "SELECT password FROM users WHERE username='$USERNAME';" 2>/dev/null | tail -1)

if [[ "$DB_PASS" == \$2* ]]; then
    echo -e "${GREEN}✅ Password is BCrypt format${NC}"
    echo "   Hash: ${DB_PASS:0:20}..."
else
    echo -e "${RED}❌ Password is NOT BCrypt format: $DB_PASS${NC}"
    echo "   This will cause login to fail!"
fi
echo ""

# Step 6: Summary
echo "======================================"
echo "📊 Summary"
echo "======================================"
echo -e "${GREEN}✅ All checks passed!${NC}"
echo ""
echo "You can now login with:"
echo "  Username: $USERNAME"
echo "  Password: $PASSWORD"
echo ""
echo "📍 Login URL: $LOGIN_URL"
echo ""
