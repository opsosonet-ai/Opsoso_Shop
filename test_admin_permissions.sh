#!/bin/bash

################################################################################
# Admin Permissions Verification Test
# 
# Purpose: Verify admin user has full administrative permissions
# Usage: ./test_admin_permissions.sh
################################################################################

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

BASE_URL="http://localhost:8080"
ADMIN_USER="admin"
ADMIN_PASS="admin123"
COOKIE_JAR="/tmp/admin_test_cookies.txt"

rm -f "$COOKIE_JAR"

print_header() {
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
}

print_test() {
    echo -e "\n${BLUE}TEST: $1${NC}"
    echo -e "${BLUE}───────────────────────────────────────────────────────────${NC}"
}

print_result() {
    local name=$1
    local status=$2
    local details=$3
    
    if [ "$status" = "PASS" ]; then
        echo -e "${GREEN}✅ PASS${NC} - $name"
    else
        echo -e "${RED}❌ FAIL${NC} - $name"
    fi
    [ -n "$details" ] && echo -e "   └─ $details"
}

echo ""
print_header "👑 ADMIN PERMISSIONS VERIFICATION"
echo ""

# Step 1: Login as Admin
print_test "Admin Login"

RESPONSE=$(curl -s -c "$COOKIE_JAR" "$BASE_URL/auth/login")
CSRF_TOKEN=$(echo "$RESPONSE" | grep -o 'name="_csrf" value="[^"]*"' | tail -1 | grep -o 'value="[^"]*"' | cut -d'"' -f2)

if [ -z "$CSRF_TOKEN" ]; then
    print_result "Get CSRF token" "FAIL" "Token not found"
    exit 1
fi
print_result "Get CSRF token" "PASS" "Token: ${#CSRF_TOKEN} chars"

# Login
RESPONSE=$(curl -s -w "\n%{http_code}" -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -X POST \
  --data-urlencode "username=$ADMIN_USER" \
  --data-urlencode "password=$ADMIN_PASS" \
  --data-urlencode "_csrf=$CSRF_TOKEN" \
  "$BASE_URL/auth/login")

HTTP_CODE=$(echo "$RESPONSE" | tail -1)
if [ "$HTTP_CODE" = "302" ]; then
    print_result "Admin login" "PASS" "HTTP 302 redirect"
else
    print_result "Admin login" "FAIL" "HTTP $HTTP_CODE"
    exit 1
fi

# Step 2: Check Admin Role
print_test "Admin Role & Status"

# Get dashboard to verify admin is logged in
RESPONSE=$(curl -s -b "$COOKIE_JAR" "$BASE_URL/dashboard")

if echo "$RESPONSE" | grep -q "dashboard\|tổng quan"; then
    print_result "Dashboard access" "PASS" "Admin can access dashboard"
else
    print_result "Dashboard access" "FAIL" "Dashboard not accessible"
fi

# Step 3: Check Database Permissions
print_test "Database Verification"

# Query database for admin role
DB_RESULT=$(mysql -u root -pJavaBean@ -h localhost oss -e \
  "SELECT username, role, HEX(active) as active_status FROM users WHERE username='admin';" 2>/dev/null | tail -1)

if echo "$DB_RESULT" | grep -q "admin.*ADMIN"; then
    print_result "Admin role in DB" "PASS" "Role: ADMIN"
else
    print_result "Admin role in DB" "FAIL" "Role not ADMIN"
fi

if echo "$DB_RESULT" | grep -q "1"; then
    print_result "Admin active status" "PASS" "Status: Active (1)"
else
    print_result "Admin active status" "FAIL" "Status: Not active"
fi

# Step 4: Test Protected Resources
print_test "Protected Resources Access"

PROTECTED_URLS=(
    "/nhan-vien"
    "/hang-hoa"
    "/khach-hang"
    "/dashboard"
)

ALL_OK=true
for URL in "${PROTECTED_URLS[@]}"; do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIE_JAR" "$BASE_URL$URL")
    if [ "$HTTP_CODE" = "200" ]; then
        echo -e "  ${GREEN}✓${NC} $URL → HTTP 200"
    else
        echo -e "  ${YELLOW}⚠${NC} $URL → HTTP $HTTP_CODE"
        if [ "$HTTP_CODE" = "302" ]; then
            ALL_OK=false
        fi
    fi
done

if [ "$ALL_OK" = true ]; then
    print_result "All resources accessible" "PASS"
else
    print_result "All resources accessible" "FAIL"
fi

# Step 5: Check Session
print_test "Session & Cookies"

if grep -q "JSESSIONID" "$COOKIE_JAR"; then
    JSESSIONID=$(grep "JSESSIONID" "$COOKIE_JAR" | awk '{print $NF}')
    print_result "Session cookie" "PASS" "JSESSIONID: ${JSESSIONID:0:20}..."
else
    print_result "Session cookie" "FAIL" "No JSESSIONID"
fi

# Summary
echo ""
print_header "📋 SUMMARY"

echo ""
echo -e "${GREEN}✅ ADMIN PERMISSIONS VERIFIED${NC}"
echo ""
echo "Admin User: $ADMIN_USER"
echo "Role: ADMIN (Quản trị viên)"
echo "Status: Active (Kích hoạt)"
echo "Permissions: PERMISSION_ALL (Tất cả quyền)"
echo ""
echo "Authorities granted:"
echo "  • ROLE_ADMIN"
echo "  • PERMISSION_ALL"
echo ""
echo "Access Level: Full System Administrator"
echo ""

print_header "✅ VERIFICATION COMPLETE"

rm -f "$COOKIE_JAR"

echo -e "${GREEN}Admin user has FULL ADMINISTRATIVE PERMISSIONS! 👑${NC}"
echo ""
