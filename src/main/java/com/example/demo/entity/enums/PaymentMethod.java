package com.example.demo.entity.enums;

/**
 * Enum quản lý phương thức thanh toán
 * - TIEN_MAT: Tiền mặt
 * - CHUYEN_KHOAN: Chuyển khoản
 * - CHI_TIEU: Chi tiêu
 */
public enum PaymentMethod {
    TIEN_MAT("Tiền mặt", "cash"),
    CHUYEN_KHOAN("Chuyển khoản", "transfer"),
    CHI_TIEU("Chi tiêu", "check");
    
    private final String label;
    private final String code;
    
    PaymentMethod(String label, String code) {
        this.label = label;
        this.code = code;
    }
    
    /**
     * Lấy tên tiếng Việt của phương thức thanh toán
     * @return Tên phương thức
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Lấy mã phương thức (dùng cho API)
     * @return Mã phương thức
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Lấy icon cho phương thức thanh toán
     * @return Icon Bootstrap
     */
    public String getIcon() {
        switch (this) {
            case TIEN_MAT:
                return "bi-cash-coin";
            case CHUYEN_KHOAN:
                return "bi-bank";
            case CHI_TIEU:
                return "bi-receipt";
            default:
                return "bi-question-circle";
        }
    }
    
    /**
     * Kiểm tra xem phương thức thanh toán có cần xác minh không
     * @return true nếu cần xác minh
     */
    public boolean requiresVerification() {
        return this == CHUYEN_KHOAN; // Chuyển khoản cần xác minh
    }
}
