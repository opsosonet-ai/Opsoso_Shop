package com.example.demo.repository;

import com.example.demo.entity.debt.SupplierDebtPayment;
import com.example.demo.entity.debt.SupplierDebt;
import com.example.demo.entity.enums.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý Chi Tiết Thanh Toán Nợ Nhà Phân Phối
 * 
 * Các phương thức chính:
 * - Tìm phiếu thanh toán theo công nợ
 * - Lọc phiếu theo phương thức thanh toán
 * - Tìm phiếu theo ngày
 * - Tính tổng tiền thanh toán
 * 
 * Sử dụng JPA Repository cho CRUD cơ bản
 * Và @Query cho truy vấn tùy chỉnh
 */
@Repository
public interface SupplierDebtPaymentRepository extends JpaRepository<SupplierDebtPayment, Long> {
    
    /**
     * Tìm tất cả phiếu thanh toán của một công nợ
     * 
     * @param supplierDebt Công nợ
     * @return Danh sách phiếu thanh toán
     */
    List<SupplierDebtPayment> findBySupplierDebt(SupplierDebt supplierDebt);
    
    /**
     * Tìm tất cả phiếu thanh toán của công nợ theo ID
     * 
     * @param debtId ID công nợ
     * @return Danh sách phiếu thanh toán
     */
    @Query("SELECT sdp FROM SupplierDebtPayment sdp WHERE sdp.supplierDebt.id = :debtId ORDER BY sdp.ngayThanhToan DESC")
    List<SupplierDebtPayment> findByDebtId(@Param("debtId") Long debtId);
    
    /**
     * Tìm phiếu thanh toán theo số phiếu
     * 
     * @param soPhieuThanhToan Số phiếu thanh toán
     * @return Optional chứa phiếu nếu tồn tại
     */
    Optional<SupplierDebtPayment> findBySoPhieuThanhToan(String soPhieuThanhToan);
    
    /**
     * Tìm phiếu thanh toán theo phương thức
     * 
     * @param phuongThucThanhToan Phương thức thanh toán
     * @return Danh sách phiếu
     */
    List<SupplierDebtPayment> findByPhuongThucThanhToan(PaymentMethod phuongThucThanhToan);
    
    /**
     * Tìm phiếu thanh toán trong khoảng thời gian
     * 
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách phiếu thanh toán trong khoảng thời gian
     */
    @Query("SELECT sdp FROM SupplierDebtPayment sdp WHERE sdp.ngayThanhToan BETWEEN :startDate AND :endDate " +
           "ORDER BY sdp.ngayThanhToan DESC")
    List<SupplierDebtPayment> findPaymentsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Tìm phiếu thanh toán của công nợ trong khoảng thời gian
     * 
     * @param debtId ID công nợ
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách phiếu
     */
    @Query("SELECT sdp FROM SupplierDebtPayment sdp WHERE sdp.supplierDebt.id = :debtId " +
           "AND sdp.ngayThanhToan BETWEEN :startDate AND :endDate ORDER BY sdp.ngayThanhToan DESC")
    List<SupplierDebtPayment> findPaymentsByDebtAndDateRange(@Param("debtId") Long debtId,
                                                             @Param("startDate") LocalDateTime startDate,
                                                             @Param("endDate") LocalDateTime endDate);
    
    /**
     * Tính tổng tiền thanh toán của một công nợ
     * 
     * @param debtId ID công nợ
     * @return Tổng tiền thanh toán
     */
    @Query("SELECT COALESCE(SUM(sdp.soTienThanhToan), 0) FROM SupplierDebtPayment sdp WHERE sdp.supplierDebt.id = :debtId")
    BigDecimal calculateTotalPaymentByDebt(@Param("debtId") Long debtId);
    
    /**
     * Tính tổng tiền thanh toán theo phương thức
     * 
     * @param phuongThucThanhToan Phương thức thanh toán
     * @return Tổng tiền
     */
    @Query("SELECT COALESCE(SUM(sdp.soTienThanhToan), 0) FROM SupplierDebtPayment sdp " +
           "WHERE sdp.phuongThucThanhToan = :phuongThucThanhToan")
    BigDecimal calculateTotalPaymentByMethod(@Param("phuongThucThanhToan") PaymentMethod phuongThucThanhToan);
    
    /**
     * Tính tổng tiền thanh toán trong khoảng thời gian
     * 
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Tổng tiền thanh toán
     */
    @Query("SELECT COALESCE(SUM(sdp.soTienThanhToan), 0) FROM SupplierDebtPayment sdp " +
           "WHERE sdp.ngayThanhToan BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalPaymentByDateRange(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
    
    /**
     * Đếm số phiếu thanh toán của một công nợ
     * 
     * @param debtId ID công nợ
     * @return Số lượng phiếu
     */
    @Query("SELECT COUNT(sdp) FROM SupplierDebtPayment sdp WHERE sdp.supplierDebt.id = :debtId")
    long countPaymentsByDebt(@Param("debtId") Long debtId);
    
    /**
     * Tìm phiếu thanh toán gần nhất của một công nợ
     * 
     * @param debtId ID công nợ
     * @return Optional chứa phiếu thanh toán gần nhất
     */
    @Query("SELECT sdp FROM SupplierDebtPayment sdp WHERE sdp.supplierDebt.id = :debtId " +
           "ORDER BY sdp.ngayThanhToan DESC LIMIT 1")
    Optional<SupplierDebtPayment> findLatestPaymentByDebt(@Param("debtId") Long debtId);
    
    /**
     * Tìm phiếu thanh toán sớm nhất của một công nợ
     * 
     * @param debtId ID công nợ
     * @return Optional chứa phiếu thanh toán sớm nhất
     */
    @Query("SELECT sdp FROM SupplierDebtPayment sdp WHERE sdp.supplierDebt.id = :debtId " +
           "ORDER BY sdp.ngayThanhToan ASC LIMIT 1")
    Optional<SupplierDebtPayment> findFirstPaymentByDebt(@Param("debtId") Long debtId);
    
    /**
     * Tìm phiếu thanh toán cần xác minh (phương thức chuyển khoản)
     * 
     * @return Danh sách phiếu chuyển khoản
     */
    @Query("SELECT sdp FROM SupplierDebtPayment sdp WHERE sdp.phuongThucThanhToan = com.example.demo.entity.enums.PaymentMethod.CHUYEN_KHOAN " +
           "AND (sdp.soBangKe IS NULL OR sdp.soBangKe = '') ORDER BY sdp.ngayThanhToan DESC")
    List<SupplierDebtPayment> findUnverifiedTransfers();
    
    /**
     * Tìm tất cả phiếu thanh toán với eager loading của liên kết
     * Sử dụng JOIN FETCH để tránh LazyInitializationException
     */
    @Query("SELECT DISTINCT sdp FROM SupplierDebtPayment sdp " +
           "LEFT JOIN FETCH sdp.supplierDebt sd " +
           "LEFT JOIN FETCH sd.nhaPhanPhoi " +
           "ORDER BY sdp.ngayThanhToan DESC")
    List<SupplierDebtPayment> findAllWithEagerLoading();
}
