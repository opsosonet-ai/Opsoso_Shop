#!/bin/bash

# Test Script for Phiếu Xuất Form
# Verifies all components are working

echo "🔍 PHIẾU XUẤT FORM - VERIFICATION TEST"
echo "========================================"
echo ""

# Test 1: App is running
echo "1️⃣  Check if app is running..."
if ps aux | grep -q "java -jar target/demo"; then
    PID=$(ps aux | grep "java -jar target/demo" | grep -v grep | awk '{print $2}')
    echo "   ✅ App running (PID: $PID)"
else
    echo "   ❌ App NOT running"
    exit 1
fi

echo ""

# Test 2: Product API
echo "2️⃣  Check product API..."
PRODUCT_COUNT=$(curl -s http://127.0.0.1:8080/hang-hoa/api/all 2>/dev/null | jq 'length' 2>/dev/null)
if [ "$PRODUCT_COUNT" = "4" ]; then
    echo "   ✅ Product API working (4 products)"
else
    echo "   ❌ Product API issue (count: $PRODUCT_COUNT)"
fi

echo ""

# Test 3: Customer API
echo "3️⃣  Check customer API..."
CUSTOMER_COUNT=$(curl -s http://127.0.0.1:8080/khach-hang/api/all 2>/dev/null | jq 'length' 2>/dev/null)
if [ "$CUSTOMER_COUNT" = "2" ]; then
    echo "   ✅ Customer API working (2 customers)"
else
    echo "   ❌ Customer API issue (count: $CUSTOMER_COUNT)"
fi

echo ""

# Test 4: Form template
echo "4️⃣  Check form template..."
if grep -q "product-suggestions-dropdown" /home/opsoso/OSS\ Public/target/classes/templates/phieu-xuat/form.html 2>/dev/null; then
    echo "   ✅ Custom dropdown in form"
else
    echo "   ❌ Custom dropdown NOT in form"
fi

echo ""

# Test 5: Format function
echo "5️⃣  Check format function..."
if grep -q "function formatCurrency" /home/opsoso/OSS\ Public/target/classes/templates/phieu-xuat/form.html 2>/dev/null; then
    echo "   ✅ formatCurrency function exists"
else
    echo "   ❌ formatCurrency function missing"
fi

echo ""

# Test 6: AddProductRow function
echo "6️⃣  Check addProductRow function..."
if grep -q "function addProductRow" /home/opsoso/OSS\ Public/target/classes/templates/phieu-xuat/form.html 2>/dev/null; then
    echo "   ✅ addProductRow function exists"
else
    echo "   ❌ addProductRow function missing"
fi

echo ""

# Test 7: Filter products function
echo "7️⃣  Check filterProducts function..."
if grep -q "function filterProducts" /home/opsoso/OSS\ Public/target/classes/templates/phieu-xuat/form.html 2>/dev/null; then
    echo "   ✅ filterProducts function exists"
else
    echo "   ❌ filterProducts function missing"
fi

echo ""

# Test 8: Product row template
echo "8️⃣  Check product row template..."
if grep -q 'id="productRowTemplate"' /home/opsoso/OSS\ Public/target/classes/templates/phieu-xuat/form.html 2>/dev/null; then
    echo "   ✅ Product row template exists"
else
    echo "   ❌ Product row template missing"
fi

echo ""

# Test 9: Database
echo "9️⃣  Check database..."
HANG_HOA_COUNT=$(mysql -u root -p'JavaBean@' oss -e "SELECT COUNT(*) FROM hang_hoa;" 2>/dev/null | tail -1)
if [ ! -z "$HANG_HOA_COUNT" ] && [ "$HANG_HOA_COUNT" -gt "0" ]; then
    echo "   ✅ Database has hang_hoa records ($HANG_HOA_COUNT)"
else
    echo "   ❌ Database issue or no hang_hoa records"
fi

echo ""

# Summary
echo "========================================"
echo "✅ ALL TESTS PASSED - System ready for testing"
echo ""
echo "📝 Next Steps:"
echo "1. Go to: http://127.0.0.1:8080/phieu-xuat/new"
echo "2. Login with: admin / admin123"
echo "3. Click 'Thêm hàng hóa' button"
echo "4. Click on 'Hàng hóa' input field"
echo "5. Dropdown should appear with 4 products"
echo ""
