#!/bin/bash

# Script để thêm menu Quản Lý Công Nợ vào tất cả navbar

# Các file cần cập nhật
files=(
  "src/main/resources/templates/layout.html"
  "src/main/resources/templates/index.html"
  "src/main/resources/templates/user-form.html"
  "src/main/resources/templates/user-detail.html"
  "src/main/resources/templates/layout/navbar.html"
  "src/main/resources/templates/tra-hang/danh-sach.html"
  "src/main/resources/templates/tra-hang/chi-tiet.html"
  "src/main/resources/templates/tra-hang/them.html"
  "src/main/resources/templates/tra-hang/sua.html"
  "src/main/resources/templates/nha-phan-phoi/form.html"
  "src/main/resources/templates/nha-phan-phoi/index.html"
  "src/main/resources/templates/nhan-vien/form.html"
  "src/main/resources/templates/nhan-vien/index.html"
  "src/main/resources/templates/khach-hang/index.html"
  "src/main/resources/templates/settings/index.html"
  "src/main/resources/templates/phieu-xuat/list.html"
  "src/main/resources/templates/phieu-xuat/form.html"
)

for file in "${files[@]}"; do
  if [ -f "$file" ]; then
    # Kiểm tra xem file đã có menu không
    if ! grep -q "Quản Lý Công Nợ\|cong-no" "$file"; then
      echo "Cập nhật: $file"
      # TODO: Thêm menu tại đây
    else
      echo "Bỏ qua (đã có): $file"
    fi
  fi
done

