package com.example.demo.entity.enums;

/**
 * Enum quản lý trạng thái công nợ
 * - DANG_NO: Đang nợ
 * - THANH_TOAN_TUAN_TUAN: Thanh toán từng phần
 * - DA_THANH_TOAN_HET: Đã thanh toán hết
 * - QUA_HAN: Quá hạn thanh toán
 */
public enum DebtStatus {
    DANG_NO("Đang nợ", "warning"),
    THANH_TOAN_TUAN_TUAN("Thanh toán từng tuần", "info"),
    DA_THANH_TOAN_HET("Đã thanh toán hết", "success"),
    QUA_HAN("Quá hạn", "danger");
    
    private final String label;
    private final String badgeClass;
    
    DebtStatus(String label, String badgeClass) {
        this.label = label;
        this.badgeClass = badgeClass;
    }
    
    /**
     * Lấy tên tiếng Việt của trạng thái
     * @return Tên trạng thái
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Lấy CSS class để hiển thị badge
     * @return CSS class (warning, info, success, danger)
     */
    public String getBadgeClass() {
        return badgeClass;
    }
    
    /**
     * Kiểm tra xem công nợ có quá hạn hay không
     * @return true nếu quá hạn
     */
    public boolean isOverdue() {
        return this == QUA_HAN;
    }
    
    /**
     * Kiểm tra xem công nợ còn dư hay không
     * @return true nếu còn dư
     */
    public boolean hasRemainingDebt() {
        return this != DA_THANH_TOAN_HET;
    }
}
