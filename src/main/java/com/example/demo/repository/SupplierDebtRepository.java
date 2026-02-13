package com.example.demo.repository;

import com.example.demo.entity.debt.SupplierDebt;
import com.example.demo.entity.NhaPhanPhoi;
import com.example.demo.entity.enums.DebtStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý Nợ Nhà Phân Phối
 * 
 * Các phương thức chính:
 * - Tìm nợ theo nhà phân phối
 * - Lọc nợ theo trạng thái
 * - Tìm nợ quá hạn
 * - Tính tổng nợ
 * 
 * Sử dụng JPA Repository cho các thao tác CRUD cơ bản
 * Và @Query cho các truy vấn tùy chỉnh
 */
@Repository
public interface SupplierDebtRepository extends JpaRepository<SupplierDebt, Long> {
    
    /**
     * Tìm tất cả nợ của một nhà phân phối
     * 
     * @param nhaPhanPhoi Nhà phân phối
     * @return Danh sách nợ của nhà phân phối này
     */
    List<SupplierDebt> findByNhaPhanPhoi(NhaPhanPhoi nhaPhanPhoi);
    
    /**
     * Tìm tất cả nợ của nhà phân phối theo ID
     * 
     * @param nhaPhanPhoiId ID nhà phân phối
     * @return Danh sách nợ
     */
    @Query("SELECT sd FROM SupplierDebt sd WHERE sd.nhaPhanPhoi.id = :nhaPhanPhoiId")
    List<SupplierDebt> findByNhaPhanPhoiId(@Param("nhaPhanPhoiId") Long nhaPhanPhoiId);
    
    /**
     * Tìm nợ theo trạng thái
     * 
     * @param trangThai Trạng thái công nợ
     * @return Danh sách nợ có trạng thái tương ứng
     */
    List<SupplierDebt> findByTrangThai(DebtStatus trangThai);
    
    /**
     * Tìm nợ theo số phiếu xuất chi
     * 
     * @param soPhieuXuatChi Số phiếu xuất chi
     * @return Optional chứa nợ nếu tồn tại
     */
    Optional<SupplierDebt> findBySoPhieuXuatChi(String soPhieuXuatChi);
    
    /**
     * Tìm tất cả nợ sắp quá hạn
     * 
     * @param date Ngày giới hạn
     * @return Danh sách nợ có hạn thanh toán trước ngày này
     */
    List<SupplierDebt> findByNgayHanChotBefore(LocalDate date);
    
    /**
     * Tìm tất cả nợ chưa thanh toán hết
     * Sử dụng JPQL để tìm nợ có tongTienConNo > 0
     * 
     * @return Danh sách nợ còn dư
     */
    @Query("SELECT sd FROM SupplierDebt sd WHERE sd.tongTienConNo > 0 ORDER BY sd.ngayHanChot ASC")
    List<SupplierDebt> findAllUnpaidDebts();
    
    /**
     * Tìm tất cả nợ chưa thanh toán của nhà phân phối cụ thể
     * 
     * @param nhaPhanPhoiId ID nhà phân phối
     * @return Danh sách nợ còn dư
     */
    @Query("SELECT sd FROM SupplierDebt sd WHERE sd.nhaPhanPhoi.id = :nhaPhanPhoiId AND sd.tongTienConNo > 0 " +
           "ORDER BY sd.ngayHanChot ASC")
    List<SupplierDebt> findUnpaidDebtsBySupplier(@Param("nhaPhanPhoiId") Long nhaPhanPhoiId);
    
    /**
     * Tìm tất cả nợ quá hạn (ngày hôm nay > ngayHanChot)
     * 
     * @return Danh sách nợ quá hạn
     */
    @Query("SELECT sd FROM SupplierDebt sd WHERE sd.ngayHanChot < CURRENT_DATE AND sd.tongTienConNo > 0")
    List<SupplierDebt> findOverdueDebts();
    
    /**
     * Tính tổng tiền nợ còn lại của tất cả nhà phân phối
     * Trả về 0 nếu không có nợ
     * 
     * @return Tổng tiền nợ
     */
    @Query("SELECT COALESCE(SUM(sd.tongTienConNo), 0) FROM SupplierDebt sd")
    BigDecimal calculateTotalRemainingDebt();
    
    /**
     * Tính tổng tiền nợ còn lại của một nhà phân phối
     * 
     * @param nhaPhanPhoiId ID nhà phân phối
     * @return Tổng tiền nợ của nhà phân phối
     */
    @Query("SELECT COALESCE(SUM(sd.tongTienConNo), 0) FROM SupplierDebt sd WHERE sd.nhaPhanPhoi.id = :nhaPhanPhoiId")
    BigDecimal calculateRemainingDebtBySupplier(@Param("nhaPhanPhoiId") Long nhaPhanPhoiId);
    
    /**
     * Tính tổng tiền nợ ban đầu của tất cả nhà phân phối
     * 
     * @return Tổng tiền nợ
     */
    @Query("SELECT COALESCE(SUM(sd.tongTienNo), 0) FROM SupplierDebt sd")
    BigDecimal calculateTotalInitialDebt();
    
    /**
     * Tính tổng tiền đã thanh toán
     * 
     * @return Tổng tiền đã thanh toán
     */
    @Query("SELECT COALESCE(SUM(sd.tongTienDaThanhToan), 0) FROM SupplierDebt sd")
    BigDecimal calculateTotalPaid();
    
    /**
     * Đếm số nợ quá hạn
     * 
     * @return Số lượng nợ quá hạn
     */
    @Query("SELECT COUNT(sd) FROM SupplierDebt sd WHERE sd.ngayHanChot < CURRENT_DATE AND sd.tongTienConNo > 0")
    long countOverdueDebts();
    
    /**
     * Đếm số nợ đang duy trì của nhà phân phối
     * 
     * @param nhaPhanPhoiId ID nhà phân phối
     * @return Số lượng nợ
     */
    @Query("SELECT COUNT(sd) FROM SupplierDebt sd WHERE sd.nhaPhanPhoi.id = :nhaPhanPhoiId AND sd.tongTienConNo > 0")
    long countUnpaidDebtsBySupplier(@Param("nhaPhanPhoiId") Long nhaPhanPhoiId);
    
    /**
     * Tìm nợ với điều kiện phức tạp (theo trạng thái và nhà phân phối)
     * 
     * @param trangThai Trạng thái
     * @param nhaPhanPhoiId ID nhà phân phối
     * @return Danh sách nợ phù hợp
     */
    @Query("SELECT sd FROM SupplierDebt sd WHERE sd.trangThai = :trangThai AND sd.nhaPhanPhoi.id = :nhaPhanPhoiId " +
           "ORDER BY sd.ngayTaoNo DESC")
    List<SupplierDebt> findByStatusAndSupplier(@Param("trangThai") DebtStatus trangThai, 
                                               @Param("nhaPhanPhoiId") Long nhaPhanPhoiId);
    
    /**
     * Tìm tất cả nợ với eager loading của nhà phân phối
     * Giải quyết LazyInitializationException bằng JOIN FETCH
     * 
     * @return Danh sách tất cả nợ với nhà phân phối đã được load
     */
    @Query("SELECT DISTINCT sd FROM SupplierDebt sd LEFT JOIN FETCH sd.nhaPhanPhoi")
    List<SupplierDebt> findAllWithNhaPhanPhoi();
    
    /**
     * Tìm nợ nhà phân phối theo ID với eager loading
     * Giải quyết LazyInitializationException bằng JOIN FETCH
     * 
     * @param debtId ID của nợ nhà phân phối
     * @return Optional chứa nợ nhà phân phối với nhà phân phối và payments đã được load
     */
    @Query("SELECT sd FROM SupplierDebt sd " +
           "LEFT JOIN FETCH sd.nhaPhanPhoi " +
           "LEFT JOIN FETCH sd.payments " +
           "WHERE sd.id = :debtId")
    Optional<SupplierDebt> findByIdWithNhaPhanPhoi(@Param("debtId") Long debtId);
    
    /**
     * Tìm tất cả nợ chưa thu hồi với eager loading của nhà phân phối
     * 
     * @return Danh sách nợ còn dư với nhà phân phối đã được load
     */
    @Query("SELECT DISTINCT sd FROM SupplierDebt sd LEFT JOIN FETCH sd.nhaPhanPhoi " +
           "WHERE sd.tongTienConNo > 0 ORDER BY sd.ngayHanChot ASC")
    List<SupplierDebt> findAllUnpaidDebtsWithNhaPhanPhoi();
}
