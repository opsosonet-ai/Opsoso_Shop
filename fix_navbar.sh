#!/bin/bash

# Fix all navbar th:class expressions by removing the problematic #strings.contains
# Pattern: <a class="nav-link" th:class="${#strings.contains(...)" href="...">
# Replace: <a class="nav-link" href="...">

files=(
  "src/main/resources/templates/hang-hoa/form.html"
  "src/main/resources/templates/hang-hoa/index.html"
  "src/main/resources/templates/hang-hoa/thong-ke.html"
  "src/main/resources/templates/layout/navbar.html"
  "src/main/resources/templates/nhan-vien/detail.html"
  "src/main/resources/templates/nhan-vien/form.html"
  "src/main/resources/templates/nhan-vien/index.html"
  "src/main/resources/templates/nha-phan-phoi/form.html"
  "src/main/resources/templates/nha-phan-phoi/index.html"
  "src/main/resources/templates/phieu-xuat/list.html"
  "src/main/resources/templates/settings/index.html"
  "src/main/resources/templates/tra-hang/chi-tiet.html"
  "src/main/resources/templates/tra-hang/danh-sach.html"
  "src/main/resources/templates/tra-hang/sua.html"
  "src/main/resources/templates/tra-hang/them.html"
)

for file in "${files[@]}"; do
  echo "Fixing $file..."
  # Remove th:class with the problematic #strings.contains expression
  sed -i 's/ th:class="\${#strings\.contains(#request\.requestURI[^}]*}"//g' "$file"
done

echo "All files fixed!"
