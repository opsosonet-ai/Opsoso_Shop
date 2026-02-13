package com.example.demo.repository;

import com.example.demo.entity.debt.CustomerDebtPayment;
import com.example.demo.entity.debt.CustomerDebt;
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
 * Repository quản lý Chi Tiết Thu Hồi Nợ Khách Hàng
 * 
 * Các phương thức chính:
 * - Tìm phiếu thu hồi theo công nợ khách hàng
 * - Lọc phiếu theo phương thức
 * - Tìm phiếu theo ngày
 * - Tính tổng tiền thu hồi
 * 
 * Tương tự SupplierDebtPaymentRepository nhưng cho khách hàng
 */
@Repository
public interface CustomerDebtPaymentRepository extends JpaRepository<CustomerDebtPayment, Long> {
    
    /**
     * Tìm tất cả phiếu thu hồi của một công nợ khách hàng
     * 
     * @param customerDebt Công nợ khách hàng
     * @return Danh sách phiếu thu hồi
     */
    List<CustomerDebtPayment> findByCustomerDebt(CustomerDebt customerDebt);
    
    /**
     * Tìm tất cả phiếu thu hồi của công nợ theo ID
     * 
     * @param debtId ID công nợ
     * @return Danh sách phiếu thu hồi
     */
    @Query("SELECT cdp FROM CustomerDebtPayment cdp WHERE cdp.customerDebt.id = :debtId ORDER BY cdp.ngayThuHoi DESC")
    List<CustomerDebtPayment> findByDebtId(@Param("debtId") Long debtId);
    
    /**
     * Tìm phiếu thu hồi theo số phiếu
     * 
     * @param soPhieuThuHoi Số phiếu thu hồi
     * @return Optional chứa phiếu nếu tồn tại
     */
    Optional<CustomerDebtPayment> findBySoPhieuThuHoi(String soPhieuThuHoi);
    
    /**
     * Tìm phiếu thu hồi theo phương thức
     * 
     * @param phuongThucThuHoi Phương thức thu hồi
     * @return Danh sách phiếu
     */
    List<CustomerDebtPayment> findByPhuongThucThuHoi(PaymentMethod phuongThucThuHoi);
    
    /**
     * Tìm phiếu thu hồi trong khoảng thời gian
     * 
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách phiếu thu hồi trong khoảng thời gian
     */
    @Query("SELECT cdp FROM CustomerDebtPayment cdp WHERE cdp.ngayThuHoi BETWEEN :startDate AND :endDate " +
           "ORDER BY cdp.ngayThuHoi DESC")
    List<CustomerDebtPayment> findCollectionsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);
    
    /**
     * Tìm phiếu thu hồi của công nợ trong khoảng thời gian
     * 
     * @param debtId ID công nợ
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách phiếu
     */
    @Query("SELECT cdp FROM CustomerDebtPayment cdp WHERE cdp.customerDebt.id = :debtId " +
           "AND cdp.ngayThuHoi BETWEEN :startDate AND :endDate ORDER BY cdp.ngayThuHoi DESC")
    List<CustomerDebtPayment> findCollectionsByDebtAndDateRange(@Param("debtId") Long debtId,
                                                                @Param("startDate") LocalDateTime startDate,
                                                                @Param("endDate") LocalDateTime endDate);
    
    /**
     * Tính tổng tiền thu hồi của một công nợ khách hàng
     * 
     * @param debtId ID công nợ
     * @return Tổng tiền thu hồi
     */
    @Query("SELECT COALESCE(SUM(cdp.soTienThuHoi), 0) FROM CustomerDebtPayment cdp WHERE cdp.customerDebt.id = :debtId")
    BigDecimal calculateTotalCollectionByDebt(@Param("debtId") Long debtId);
    
    /**
     * Tính tổng tiền thu hồi theo phương thức
     * 
     * @param phuongThucThuHoi Phương thức thu hồi
     * @return Tổng tiền
     */
    @Query("SELECT COALESCE(SUM(cdp.soTienThuHoi), 0) FROM CustomerDebtPayment cdp " +
           "WHERE cdp.phuongThucThuHoi = :phuongThucThuHoi")
    BigDecimal calculateTotalCollectionByMethod(@Param("phuongThucThuHoi") PaymentMethod phuongThucThuHoi);
    
    /**
     * Tính tổng tiền thu hồi trong khoảng thời gian
     * 
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Tổng tiền thu hồi
     */
    @Query("SELECT COALESCE(SUM(cdp.soTienThuHoi), 0) FROM CustomerDebtPayment cdp " +
           "WHERE cdp.ngayThuHoi BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalCollectionByDateRange(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    /**
     * Đếm số phiếu thu hồi của một công nợ
     * 
     * @param debtId ID công nợ
     * @return Số lượng phiếu
     */
    @Query("SELECT COUNT(cdp) FROM CustomerDebtPayment cdp WHERE cdp.customerDebt.id = :debtId")
    long countCollectionsByDebt(@Param("debtId") Long debtId);
    
    /**
     * Tìm phiếu thu hồi gần nhất của một công nợ
     * 
     * @param debtId ID công nợ
     * @return Optional chứa phiếu thu hồi gần nhất
     */
    @Query("SELECT cdp FROM CustomerDebtPayment cdp WHERE cdp.customerDebt.id = :debtId " +
           "ORDER BY cdp.ngayThuHoi DESC LIMIT 1")
    Optional<CustomerDebtPayment> findLatestCollectionByDebt(@Param("debtId") Long debtId);
    
    /**
     * Tìm phiếu thu hồi sớm nhất của một công nợ
     * 
     * @param debtId ID công nợ
     * @return Optional chứa phiếu thu hồi sớm nhất
     */
    @Query("SELECT cdp FROM CustomerDebtPayment cdp WHERE cdp.customerDebt.id = :debtId " +
           "ORDER BY cdp.ngayThuHoi ASC LIMIT 1")
    Optional<CustomerDebtPayment> findFirstCollectionByDebt(@Param("debtId") Long debtId);
    
    /**
     * Tìm phiếu thu hồi cần xác minh (phương thức chuyển khoản)
     * 
     * @return Danh sách phiếu chuyển khoản
     */
    @Query("SELECT cdp FROM CustomerDebtPayment cdp WHERE cdp.phuongThucThuHoi = com.example.demo.entity.enums.PaymentMethod.CHUYEN_KHOAN " +
           "ORDER BY cdp.ngayThuHoi DESC")
    List<CustomerDebtPayment> findTransfersForVerification();
    
    /**
     * Tìm top N phương thức thu hồi được sử dụng nhiều nhất
     * 
     * @return Danh sách phương thức
     */
    @Query("SELECT cdp.phuongThucThuHoi, COUNT(cdp) as count FROM CustomerDebtPayment cdp " +
           "GROUP BY cdp.phuongThucThuHoi ORDER BY count DESC")
    List<Object[]> findMostUsedPaymentMethods();
    
    /**
     * Tìm tất cả phiếu thu hồi với eager loading của liên kết
     * Sử dụng JOIN FETCH để tránh LazyInitializationException
     */
    @Query("SELECT DISTINCT cdp FROM CustomerDebtPayment cdp " +
           "LEFT JOIN FETCH cdp.customerDebt cd " +
           "LEFT JOIN FETCH cd.khachHang " +
           "ORDER BY cdp.ngayThuHoi DESC")
    List<CustomerDebtPayment> findAllWithEagerLoading();
}
