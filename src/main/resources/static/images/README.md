# Thư mục Images

Thư mục này chứa các hình ảnh tĩnh cho ứng dụng.

## Cấu trúc thư mục:

- `logo/` - Chứa các file logo của công ty
  - Định dạng được hỗ trợ: `.png`, `.jpg`, `.jpeg`, `.svg`
  - Kích thước khuyến nghị: 
    - Logo header: 200x100px
    - Logo print: 150x75px

## Cách sử dụng trong template:

### Thymeleaf Template:
```html
<!-- Logo trong header -->
<img th:src="@{/images/logo/company-logo.png}" alt="Company Logo" width="200">

<!-- Logo trong phiếu in -->
<img th:src="@{/images/logo/print-logo.png}" alt="Print Logo" width="150">
```

### Truy cập URL trực tiếp:
- `http://localhost:8080/images/logo/company-logo.png`

## Lưu ý:
- File sẽ được serve tự động bởi Spring Boot static resource handler
- Đường dẫn bắt đầu từ `/images/` trong URL
- Nên tối ưu kích thước file để tải nhanh hơn