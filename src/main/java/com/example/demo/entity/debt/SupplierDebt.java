package com.example.demo.entity.debt;

import com.example.demo.entity.NhaPhanPhoi;
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
 * Entity quản lý Nợ Nhà Phân Phối (mua trước trả sau)
 * 
 * Các trường chính:
 * - soPhieuXuatChi: Số phiếu xuất chi từ nhà phân phối
 * - nhaPhanPhoi: Nhà phân phối nợ tiền
 * - tongTienNo: Tổng tiền nợ ban đầu
 * - tongTienDaThanhToan: Tiền đã thanh toán
 * - tongTienConNo: Tiền còn nợ (tính tự động)
 * - trangThai: Trạng thái công nợ
 * - ngayHanChot: Ngày hạn chót thanh toán
 * 
 * Mối Quan Hệ:
 * - ManyToOne với NhaPhanPhoi
 * - OneToMany với SupplierDebtPayment
 */
@Entity
@Table(name = "supplier_debt", indexes = {
    @Index(name = "idx_ma_nha_phan_phoi", columnList = "ma_nha_phan_phoi"),
    @Index(name = "idx_trang_thai", columnList = "trang_thai"),
    @Index(name = "idx_ngay_han_chot", columnList = "ngay_han_chot")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDebt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Nhà phân phối nợ tiền
     * ON DELETE RESTRICT: Không cho phép xóa nhà phân phối khi còn công nợ
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nha_phan_phoi", nullable = false)
    private NhaPhanPhoi nhaPhanPhoi;
    
    /**
     * Số phiếu xuất chi từ nhà phân phối
     * Unique: Mỗi phiếu chỉ tạo 1 công nợ
     */
    @Column(unique = true, nullable = false, length = 50)
    private String soPhieuXuatChi;
    
    /**
     * Ngày tạo công nợ
     */
    @Column(nullable = false)
    private LocalDateTime ngayTaoNo;
    
    /**
     * Tổng tiền nợ ban đầu
     * Precision: 15 chữ số, 2 chữ số thập phân (9,999,999,999,999.99)
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal tongTienNo;
    
    /**
     * Tổng tiền đã thanh toán
     * Giá trị mặc định: 0
     */
    @Column(precision = 15, scale = 2)
    private BigDecimal tongTienDaThanhToan = BigDecimal.ZERO;
    
    /**
     * Tiền còn nợ (được tính tự động)
     * tongTienConNo = tongTienNo - tongTienDaThanhToan
     */
    @Column(precision = 15, scale = 2)
    private BigDecimal tongTienConNo;
    
    /**
     * Trạng thái công nợ
     * - DANG_NO: Đang nợ
     * - THANH_TOAN_TUAN_TUAN: Thanh toán từng phần
     * - DA_THANH_TOAN_HET: Thanh toán xong
     * - QUA_HAN: Quá hạn
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DebtStatus trangThai;
    
    /**
     * Ngày hạn chót thanh toán
     */
    @Column
    private LocalDate ngayHanChot;
    
    /**
     * Ghi chú bổ sung
     */
    @Column(columnDefinition = "TEXT")
    private String ghiChu;
    
    /**
     * Danh sách các phiếu thanh toán
     * Cascade: ALL - Xóa công nợ thì xóa tất cả phiếu thanh toán
     * OrphanRemoval: true - Xóa phiếu từ danh sách thì xóa khỏi DB
     */
    @OneToMany(mappedBy = "supplierDebt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplierDebtPayment> payments = new ArrayList<>();
    
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
    
    // ========== HELPER METHODS ==========
    
    /**
     * Thêm một phiếu thanh toán vào danh sách
     * @param payment Phiếu thanh toán
     */
    public void addPayment(SupplierDebtPayment payment) {
        if (payment != null) {
            payments.add(payment);
            payment.setSupplierDebt(this);
        }
    }
    
    /**
     * Loại bỏ một phiếu thanh toán khỏi danh sách
     * @param payment Phiếu thanh toán
     */
    public void removePayment(SupplierDebtPayment payment) {
        if (payment != null) {
            payments.remove(payment);
            payment.setSupplierDebt(null);
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
     * Tính tỷ lệ thanh toán (%)
     * @return Tỷ lệ từ 0 đến 100
     */
    public Double getPaymentPercentage() {
        if (this.tongTienNo == null || this.tongTienNo.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        BigDecimal paid = this.tongTienDaThanhToan != null ? this.tongTienDaThanhToan : BigDecimal.ZERO;
        return paid.divide(this.tongTienNo, 4, java.math.RoundingMode.HALF_UP)
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
