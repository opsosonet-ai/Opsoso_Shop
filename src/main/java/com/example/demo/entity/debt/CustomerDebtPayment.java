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
 * Entity quản lý Chi Tiết Thu Hồi Nợ Khách Hàng
 * 
 * Lưu trữ lịch sử từng lần thu hồi tiền từ khách hàng
 * Mỗi lần thu hồi được ghi lại một bản ghi
 * 
 * Các trường chính:
 * - soPhieuThuHoi: Số phiếu thu hồi duy nhất
 * - customerDebt: Tham chiếu đến công nợ được thu hồi
 * - soTienThuHoi: Số tiền thu hồi
 * - phuongThucThuHoi: Phương thức (tiền mặt, chuyển khoản, chi tiêu)
 * 
 * Mối Quan Hệ:
 * - ManyToOne với CustomerDebt
 * - Khi xóa CustomerDebt thì xóa tất cả CustomerDebtPayment
 * 
 * Tương tự như SupplierDebtPayment nhưng dùng cho khách hàng
 */
@Entity
@Table(name = "customer_debt_payment", indexes = {
    @Index(name = "idx_customer_debt_payment", columnList = "customer_debt_id"),
    @Index(name = "idx_collection_date", columnList = "ngay_thu_hoi")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDebtPayment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Công nợ khách hàng mà phiếu thu hồi này liên quan đến
     * ManyToOne: Một công nợ có thể có nhiều phiếu thu hồi
     * FetchType.LAZY: Tải lazy để tối ưu hiệu năng
     * ON DELETE CASCADE: Khi xóa công nợ, xóa luôn phiếu thu hồi
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_debt_id", nullable = false)
    private CustomerDebt customerDebt;
    
    /**
     * Số phiếu thu hồi
     * Unique: Mỗi phiếu có số duy nhất
     * Format: PTH + UUID (ví dụ: PTH87654321)
     */
    @Column(unique = true, nullable = false, length = 50)
    private String soPhieuThuHoi;
    
    /**
     * Ngày thu hồi tiền
     */
    @Column(nullable = false)
    private LocalDateTime ngayThuHoi;
    
    /**
     * Số tiền thu hồi được
     * Precision: 15 chữ số, 2 chữ số thập phân
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal soTienThuHoi;
    
    /**
     * Phương thức thu hồi
     * - TIEN_MAT: Tiền mặt
     * - CHUYEN_KHOAN: Chuyển khoản
     * - CHI_TIEU: Chi tiêu
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PaymentMethod phuongThucThuHoi;
    
    /**
     * Ghi chú bổ sung
     * Ví dụ: Người nộp tiền, người thu tiền, phương thức thanh toán thực tế
     */
    @Column(columnDefinition = "TEXT")
    private String ghiChu;
    
    /**
     * Người ghi nhận thu hồi
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
     * Kiểm tra xem phương thức thu hồi có cần xác minh không
     * @return true nếu là chuyển khoản (cần xác minh qua ngân hàng)
     */
    public boolean requiresVerification() {
        return PaymentMethod.CHUYEN_KHOAN.equals(this.phuongThucThuHoi);
    }
    
    /**
     * Lấy tiêu đề của phương thức thu hồi
     * @return Tiêu đề tiếng Việt
     */
    public String getPaymentMethodLabel() {
        if (this.phuongThucThuHoi == null) {
            return "Không xác định";
        }
        return this.phuongThucThuHoi.getLabel();
    }
    
    /**
     * Lấy icon của phương thức thu hồi (dùng cho UI)
     * @return Icon Bootstrap class
     */
    public String getPaymentMethodIcon() {
        if (this.phuongThucThuHoi == null) {
            return "bi-question-circle";
        }
        return this.phuongThucThuHoi.getIcon();
    }
    
    /**
     * Kiểm tra xem phiếu thu hồi này có hợp lệ hay không
     * @return true nếu hợp lệ
     */
    public boolean isValid() {
        return this.soTienThuHoi != null && 
               this.soTienThuHoi.compareTo(BigDecimal.ZERO) > 0 &&
               this.ngayThuHoi != null;
    }
    
    /**
     * Lấy mô tả ngắn gọn của phiếu thu hồi
     * @return Chuỗi mô tả (ví dụ: "Tiền mặt - 1,000,000 VND - 2025-11-01")
     */
    public String getDisplayText() {
        return "%s - %s VND - %s".formatted(
                getPaymentMethodLabel(),
                "%,.0f".formatted(soTienThuHoi),
                ngayThuHoi.toLocalDate());
    }
}
