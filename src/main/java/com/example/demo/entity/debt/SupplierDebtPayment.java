package com.example.demo.entity.debt;

import com.example.demo.entity.enums.PaymentMethod;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity quản lý Chi Tiết Thanh Toán Nợ Nhà Phân Phối
 * 
 * Lưu trữ lịch sử từng lần thanh toán công nợ nhà phân phối
 * Mỗi lần thanh toán được ghi lại một bản ghi
 * 
 * Các trường chính:
 * - soPhieuThanhToan: Số phiếu thanh toán duy nhất
 * - supplierDebt: Tham chiếu đến công nợ được thanh toán
 * - soTienThanhToan: Số tiền thanh toán
 * - phuongThucThanhToan: Phương thức (tiền mặt, chuyển khoản, chi tiêu)
 * - soBangKe: Số bảng kê (nếu thanh toán bằng chuyển khoản)
 * 
 * Mối Quan Hệ:
 * - ManyToOne với SupplierDebt
 * - Khi xóa SupplierDebt thì xóa tất cả SupplierDebtPayment
 */
@Entity
@Table(name = "supplier_debt_payment", indexes = {
    @Index(name = "idx_supplier_debt_payment", columnList = "supplier_debt_id"),
    @Index(name = "idx_payment_date", columnList = "ngay_thanh_toan")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDebtPayment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Công nợ mà phiếu thanh toán này liên quan đến
     * ManyToOne: Một công nợ có thể có nhiều phiếu thanh toán
     * FetchType.LAZY: Tải lazy để tối ưu hiệu năng
     * ON DELETE CASCADE: Khi xóa công nợ, xóa luôn phiếu thanh toán
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_debt_id", nullable = false)
    private SupplierDebt supplierDebt;
    
    /**
     * Số phiếu thanh toán
     * Unique: Mỗi phiếu có số duy nhất
     * Format: PTT + UUID (ví dụ: PTT12345678)
     */
    @Column(unique = true, nullable = false, length = 50)
    private String soPhieuThanhToan;
    
    /**
     * Ngày tháng thanh toán
     */
    @Column(nullable = false)
    private LocalDateTime ngayThanhToan;
    
    /**
     * Số tiền thanh toán
     * Precision: 15 chữ số, 2 chữ số thập phân
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal soTienThanhToan;
    
    /**
     * Phương thức thanh toán
     * - TIEN_MAT: Tiền mặt
     * - CHUYEN_KHOAN: Chuyển khoản
     * - CHI_TIEU: Chi tiêu
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PaymentMethod phuongThucThanhToan;
    
    /**
     * Số bảng kê (dùng khi thanh toán bằng chuyển khoản)
     * Tham khảo thêm: Số hóa đơn từ ngân hàng
     */
    @Column(length = 100)
    private String soBangKe;
    
    /**
     * Ghi chú bổ sung
     * Ví dụ: Người thanh toán, người nhận, điều khoản đặc biệt
     */
    @Column(columnDefinition = "TEXT")
    private String ghiChu;
    
    /**
     * Người ghi nhận thanh toán
     * Tên người dùng hoặc nhân viên đã ghi nhận giao dịch này
     */
    @Column(length = 255)
    private String nguoiGhiNhan;
    
    /**
     * Ngày tạo bản ghi
     * Tự động gán khi insert, không thể cập nhật
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Ngày cập nhật bản ghi
     * Tự động cập nhật khi update
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Kích thước dữ liệu thanh toán (Payload Size)
     * Dùng cho phân tích và tối ưu hóa dữ liệu
     */
    @Column(name = "payload_size")
    private Long payloadSize = 0L;
    
    /**
     * Mã xác thực thanh toán
     * Dùng để theo dõi và xác minh giao dịch
     */
    @Column(name = "transaction_hash", length = 64)
    private String transactionHash;
    
    // ========== HELPER METHODS ==========
    
    /**
     * Kiểm tra xem phương thức thanh toán có cần xác minh không
     * @return true nếu là chuyển khoản (cần xác minh qua ngân hàng)
     */
    public boolean requiresVerification() {
        return PaymentMethod.CHUYEN_KHOAN.equals(this.phuongThucThanhToan);
    }
    
    /**
     * Lấy tiêu đề của phương thức thanh toán
     * @return Tiêu đề tiếng Việt
     */
    public String getPaymentMethodLabel() {
        if (this.phuongThucThanhToan == null) {
            return "Không xác định";
        }
        return this.phuongThucThanhToan.getLabel();
    }
    
    /**
     * Lấy icon của phương thức thanh toán (dùng cho UI)
     * @return Icon Bootstrap class
     */
    public String getPaymentMethodIcon() {
        if (this.phuongThucThanhToan == null) {
            return "bi-question-circle";
        }
        return this.phuongThucThanhToan.getIcon();
    }
    
    /**
     * Kiểm tra xem phiếu thanh toán này có hợp lệ hay không
     * @return true nếu hợp lệ
     */
    public boolean isValid() {
        return this.soTienThanhToan != null && 
               this.soTienThanhToan.compareTo(BigDecimal.ZERO) > 0 &&
               this.ngayThanhToan != null;
    }
}
