package com.example.demo.entity.debt;

import com.example.demo.entity.KhachHang;
import com.example.demo.entity.enums.DebtStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity quản lý Nợ Khách Hàng (bán trước thu sau)
 * 
 * Các trường chính:
 * - soPhieuXuatBan: Số phiếu xuất bán cho khách hàng
 * - khachHang: Khách hàng nợ tiền
 * - tongTienNo: Tổng tiền nợ ban đầu
 * - tongTienDaThanhToan: Tiền đã thu hồi
 * - tongTienConNo: Tiền còn nợ (tính tự động)
 * - trangThai: Trạng thái công nợ
 * - ngayHanChot: Ngày hạn chót thanh toán
 * 
 * Mối Quan Hệ:
 * - ManyToOne với KhachHang
 * - OneToMany với CustomerDebtPayment
 * 
 * Khác biệt với SupplierDebt:
 * - Phía bán hàng (đối với khách) thay vì phía mua hàng (đối với nhà phân phối)
 * - Tiền được gọi là "nợ khách hàng" hay "khoản phải thu"
 */
@Entity
@Table(name = "customer_debt", indexes = {
    @Index(name = "idx_khach_hang", columnList = "khach_hang_id"),
    @Index(name = "idx_customer_trang_thai", columnList = "trang_thai"),
    @Index(name = "idx_customer_ngay_han_chot", columnList = "ngay_han_chot")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDebt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Khách hàng nợ tiền
     * ON DELETE RESTRICT: Không cho phép xóa khách hàng khi còn công nợ
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id", nullable = false)
    private KhachHang khachHang;
    
    /**
     * Số phiếu xuất bán cho khách hàng
     * Unique: Mỗi phiếu chỉ tạo 1 công nợ
     */
    @Column(unique = true, nullable = false, length = 50)
    private String soPhieuXuatBan;
    
    /**
     * Ngày tạo công nợ
     */
    @Column(nullable = false)
    private LocalDateTime ngayTaoNo;
    
    /**
     * Tổng tiền nợ ban đầu (khoản phải thu)
     * Precision: 15 chữ số, 2 chữ số thập phân
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal tongTienNo;
    
    /**
     * Tổng tiền đã thu hồi
     * Giá trị mặc định: 0
     */
    @Column(precision = 15, scale = 2)
    private BigDecimal tongTienDaThanhToan = BigDecimal.ZERO;
    
    /**
     * Tiền còn nợ (khoản phải thu còn lại)
     * tongTienConNo = tongTienNo - tongTienDaThanhToan
     */
    @Column(precision = 15, scale = 2)
    private BigDecimal tongTienConNo;
    
    /**
     * Trạng thái công nợ
     * - DANG_NO: Đang nợ
     * - THANH_TOAN_TUAN_TUAN: Thu hồi từng phần
     * - DA_THANH_TOAN_HET: Thu hồi xong
     * - QUA_HAN: Quá hạn thanh toán
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DebtStatus trangThai;
    
    /**
     * Ngày hạn chót thanh toán
     * Nếu null: Không có hạn thanh toán
     */
    @Column
    private LocalDate ngayHanChot;
    
    /**
     * Ghi chú bổ sung
     * Ví dụ: Lý do bán chịu, điều kiện thanh toán
     */
    @Column(columnDefinition = "TEXT")
    private String ghiChu;
    
    /**
     * Danh sách các phiếu thu hồi
     * Cascade: ALL - Xóa công nợ thì xóa tất cả phiếu thu hồi
     * OrphanRemoval: true - Xóa phiếu từ danh sách thì xóa khỏi DB
     */
    @OneToMany(mappedBy = "customerDebt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerDebtPayment> payments = new ArrayList<>();
    
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
     * Số lượng thanh toán
     * Dùng để theo dõi tần suất thanh toán
     */
    @Column(name = "payment_count")
    private Integer paymentCount = 0;
    
    /**
     * Tiền thanh toán trung bình
     * Dùng để tính toán kỳ vọng thanh toán tiếp theo
     */
    @Column(name = "average_payment", precision = 15, scale = 2)
    private BigDecimal averagePayment = BigDecimal.ZERO;
    
    /**
     * Số tiền không thể thu hồi (nợ xấu)
     * Tiền mà khách hàng không thể trả được, cần xóa sổ
     */
    @Column(name = "uncollectible_amount", precision = 15, scale = 2)
    private BigDecimal uncollectibleAmount = BigDecimal.ZERO;
    
    /**
     * Lý do không thể thu hồi
     * Ví dụ: Khách hàng phá sản, vô tích sự, thỏa thuận xóa nợ
     */
    @Column(length = 500)
    private String uncollectibleReason;
    
    // ========== HELPER METHODS ==========
    
    /**
     * Thêm một phiếu thu hồi vào danh sách
     * @param payment Phiếu thu hồi
     */
    public void addPayment(CustomerDebtPayment payment) {
        if (payment != null) {
            payments.add(payment);
            payment.setCustomerDebt(this);
        }
    }
    
    /**
     * Loại bỏ một phiếu thu hồi khỏi danh sách
     * @param payment Phiếu thu hồi
     */
    public void removePayment(CustomerDebtPayment payment) {
        if (payment != null) {
            payments.remove(payment);
            payment.setCustomerDebt(null);
        }
    }
    
    /**
     * Tính toán tiền còn nợ và cập nhật trạng thái tự động
     * 
     * Logic:
     * 1. tongTienConNo = tongTienNo - tongTienDaThanhToan
     * 2. Nếu tongTienConNo <= 0 -> trạng thái = DA_THANH_TOAN_HET
     * 3. Nếu hôm nay > ngayHanChot -> trạng thái = QUA_HAN
     * 4. Nếu còn nợ dư -> trạng thái = THANH_TOAN_TUAN_TUAN
     * 
     * Ghi chú: Hàm này được gọi tự động qua @PreUpdate và @PrePersist
     */
    @PreUpdate
    @PrePersist
    public void calculateRemainingDebt() {
        if (this.tongTienNo != null && this.tongTienDaThanhToan != null) {
            // Tính tiền còn nợ
            this.tongTienConNo = this.tongTienNo.subtract(this.tongTienDaThanhToan);
            
            // Cập nhật trạng thái dựa trên tiền còn nợ
            if (this.tongTienConNo.compareTo(BigDecimal.ZERO) <= 0) {
                // Không còn nợ
                this.trangThai = DebtStatus.DA_THANH_TOAN_HET;
            } else if (this.ngayHanChot != null && LocalDate.now().isAfter(this.ngayHanChot)) {
                // Quá hạn thanh toán
                this.trangThai = DebtStatus.QUA_HAN;
            } else if (!DebtStatus.DA_THANH_TOAN_HET.equals(this.trangThai)) {
                // Còn nợ và không quá hạn
                this.trangThai = DebtStatus.THANH_TOAN_TUAN_TUAN;
            }
        }
    }
    
    /**
     * Kiểm tra xem công nợ này còn dư hay không
     * @return true nếu còn nợ dư
     */
    public boolean hasRemainingDebt() {
        return this.tongTienConNo != null && this.tongTienConNo.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Kiểm tra xem công nợ này có quá hạn hay không
     * @return true nếu quá hạn
     */
    public boolean isOverdue() {
        return this.ngayHanChot != null && LocalDate.now().isAfter(this.ngayHanChot) && hasRemainingDebt();
    }
    
    /**
     * Tính tỷ lệ thu hồi (%)
     * @return Tỷ lệ từ 0 đến 100
     */
    public Double getCollectionPercentage() {
        if (this.tongTienNo == null || this.tongTienNo.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        BigDecimal collected = this.tongTienDaThanhToan != null ? this.tongTienDaThanhToan : BigDecimal.ZERO;
        return collected.divide(this.tongTienNo, 4, java.math.RoundingMode.HALF_UP)
                       .multiply(BigDecimal.valueOf(100))
                       .doubleValue();
    }
    
    /**
     * Lấy số ngày còn lại cho hạn thanh toán
     * @return Số ngày còn lại (âm nếu quá hạn)
     */
    public long getDaysRemaining() {
        if (this.ngayHanChot == null) {
            return Long.MAX_VALUE;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), this.ngayHanChot);
    }
}
