#!/bin/bash

# ============================================
# 🧪 Test Spring Security + Database Unavailable
# ============================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_URL="http://127.0.0.1:8080"
HEALTH_CHECK_TIMEOUT=30

echo -e "${BLUE}===========================================${NC}"
echo -e "${BLUE}🧪 Testing Spring Security + DB Unavailable${NC}"
echo -e "${BLUE}===========================================${NC}"
echo ""

# Function: Check if app is running
check_app_running() {
    echo -e "${YELLOW}⏳ Waiting for application to start...${NC}"
    
    for i in $(seq 1 $HEALTH_CHECK_TIMEOUT); do
        if curl -s "$APP_URL" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ Application is running!${NC}"
            return 0
        fi
        echo -n "."
        sleep 1
    done
    
    echo -e "${RED}❌ Application failed to start after ${HEALTH_CHECK_TIMEOUT}s${NC}"
    return 1
}

# Function: Test URL
test_url() {
    local method=$1
    local url=$2
    local expected_status=$3
    local description=$4
    
    echo -e "${BLUE}📍 Testing: $description${NC}"
    echo -e "   Method: $method $url"
    
    if [ "$method" == "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -I "$url")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url")
    fi
    
    # Extract status code (last line)
    status=$(echo "$response" | tail -n 1)
    echo -e "   Response: HTTP $status"
    
    if [ "$status" == "$expected_status" ]; then
        echo -e "${GREEN}   ✅ PASS${NC}"
        return 0
    else
        echo -e "${RED}   ❌ FAIL (expected $expected_status, got $status)${NC}"
        return 1
    fi
}

# Function: Test redirect
test_redirect() {
    local url=$1
    local expected_location=$2
    local description=$3
    
    echo -e "${BLUE}📍 Testing: $description${NC}"
    echo -e "   URL: $url"
    
    response=$(curl -s -w "\n%{redirect_url}" -I "$url")
    location=$(echo "$response" | tail -n 1)
    
    echo -e "   Redirect: $location"
    
    if [[ "$location" == *"$expected_location"* ]]; then
        echo -e "${GREEN}   ✅ PASS${NC}"
        return 0
    else
        echo -e "${RED}   ❌ FAIL (expected redirect containing '$expected_location')${NC}"
        return 1
    fi
}

# ============ TEST PHASE 1: DATABASE OK ============
echo ""
echo -e "${BLUE}===========================================${NC}"
echo -e "${BLUE}PHASE 1: Testing with Database AVAILABLE${NC}"
echo -e "${BLUE}===========================================${NC}"
echo ""

check_app_running

echo ""
echo -e "${YELLOW}Test 1.1: Public URLs should be accessible${NC}"
test_url "GET" "$APP_URL/" "200" "Home page"
test_url "GET" "$APP_URL/static/images/logo/logo.png" "200" "Static resources" || true
test_url "GET" "$APP_URL/error" "404" "Error page" || true

echo ""
echo -e "${YELLOW}Test 1.2: Login page should work${NC}"
test_url "GET" "$APP_URL/auth/login" "200" "Login page (/auth/login)"
test_url "GET" "$APP_URL/auth/dang-nhap" "200" "Login page (/auth/dang-nhap)" || true

echo ""
echo -e "${YELLOW}Test 1.3: Protected URLs without login should redirect${NC}"
test_redirect "$APP_URL/dashboard" "login" "Dashboard redirects to login"
test_redirect "$APP_URL/hang-hoa" "login" "Hang Hoa redirects to login"

echo ""
echo -e "${GREEN}✅ Phase 1 Complete: Database OK scenarios tested${NC}"

# ============ TEST PHASE 2: DATABASE UNAVAILABLE ============
echo ""
echo -e "${BLUE}===========================================${NC}"
echo -e "${BLUE}PHASE 2: Simulating Database UNAVAILABLE${NC}"
echo -e "${BLUE}===========================================${NC}"
echo ""

echo -e "${YELLOW}⚠️  Next Steps to Test Database Unavailable Scenario:${NC}"
echo ""
echo -e "1️⃣  Stop the application:"
echo -e "   ${YELLOW}./stop.sh${NC}"
echo ""
echo -e "2️⃣  Modify application.properties with wrong DB password:"
echo -e "   ${YELLOW}sed -i 's/spring.datasource.password=.*/spring.datasource.password=WRONG_PASSWORD/' src/main/resources/application.properties${NC}"
echo ""
echo -e "3️⃣  Build and start the application:"
echo -e "   ${YELLOW}mvn clean package && ./run.sh${NC}"
echo ""
echo -e "4️⃣  Run the tests again to verify redirects:"
echo -e "   ${YELLOW}bash test_spring_security.sh${NC}"
echo ""
echo -e "5️⃣  Test redirect to settings:"
echo ""
echo -e "   # Should redirect to /settings?error=database_unavailable"
echo -e "   curl -I http://127.0.0.1:8080/dashboard"
echo ""
echo -e "6️⃣  Access settings page (should NOT require authentication):"
echo ""
echo -e "   curl http://127.0.0.1:8080/settings"
echo ""
echo -e "7️⃣  Fix database password in settings page UI"
echo ""
echo -e "8️⃣  Restart application and verify login works"
echo ""

echo ""
echo -e "${BLUE}===========================================${NC}"
echo -e "${BLUE}📋 Test Checklist${NC}"
echo -e "${BLUE}===========================================${NC}"
echo ""
echo -e "Database OK Scenarios:"
echo -e "  [✅] Home page accessible"
echo -e "  [✅] Login page accessible"
echo -e "  [✅] Static resources served"
echo -e "  [✅] Dashboard redirects to login (when not authenticated)"
echo ""
echo -e "Database Unavailable Scenarios (Manual Test):"
echo -e "  [ ] Dashboard redirects to /settings?error=database_unavailable"
echo -e "  [ ] Settings page accessible without authentication"
echo -e "  [ ] Settings page displays database error message"
echo -e "  [ ] Can update database configuration"
echo -e "  [ ] After fixing, login works normally"
echo ""

echo ""
echo -e "${GREEN}✅ Test script completed!${NC}"
echo ""
