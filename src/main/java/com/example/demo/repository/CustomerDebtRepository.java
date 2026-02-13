package com.example.demo.repository;

import com.example.demo.entity.debt.CustomerDebt;
import com.example.demo.entity.KhachHang;
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
 * Repository quản lý Nợ Khách Hàng
 * 
 * Các phương thức chính:
 * - Tìm nợ theo khách hàng
 * - Lọc nợ theo trạng thái
 * - Tìm nợ quá hạn
 * - Tính tổng nợ (khoản phải thu)
 * 
 * Tương tự SupplierDebtRepository nhưng cho khách hàng
 */
@Repository
public interface CustomerDebtRepository extends JpaRepository<CustomerDebt, Long> {
    
    /**
     * Tìm tất cả nợ của một khách hàng
     * 
     * @param khachHang Khách hàng
     * @return Danh sách nợ của khách hàng
     */
    List<CustomerDebt> findByKhachHang(KhachHang khachHang);
    
    /**
     * Tìm tất cả nợ của khách hàng theo ID
     * 
     * @param khachHangId ID khách hàng
     * @return Danh sách nợ
     */
    @Query("SELECT cd FROM CustomerDebt cd WHERE cd.khachHang.id = :khachHangId")
    List<CustomerDebt> findByKhachHangId(@Param("khachHangId") Long khachHangId);
    
    /**
     * Tìm nợ theo trạng thái
     * 
     * @param trangThai Trạng thái công nợ
     * @return Danh sách nợ có trạng thái tương ứng
     */
    List<CustomerDebt> findByTrangThai(DebtStatus trangThai);
    
    /**
     * Tìm nợ theo số phiếu xuất bán
     * 
     * @param soPhieuXuatBan Số phiếu xuất bán
     * @return Optional chứa nợ nếu tồn tại
     */
    Optional<CustomerDebt> findBySoPhieuXuatBan(String soPhieuXuatBan);
    
    /**
     * Tìm tất cả nợ sắp quá hạn
     * 
     * @param date Ngày giới hạn
     * @return Danh sách nợ có hạn thanh toán trước ngày này
     */
    List<CustomerDebt> findByNgayHanChotBefore(LocalDate date);
    
    /**
     * Tìm tất cả nợ chưa thu hồi hết
     * Sử dụng JPQL để tìm nợ có tongTienConNo > 0
     * 
     * @return Danh sách nợ còn dư
     */
    @Query("SELECT cd FROM CustomerDebt cd WHERE cd.tongTienConNo > 0 ORDER BY cd.ngayHanChot ASC")
    List<CustomerDebt> findAllUnpaidDebts();
    
    /**
     * Tìm tất cả nợ chưa thu hồi của khách hàng cụ thể
     * 
     * @param khachHangId ID khách hàng
     * @return Danh sách nợ còn dư
     */
    @Query("SELECT cd FROM CustomerDebt cd WHERE cd.khachHang.id = :khachHangId AND cd.tongTienConNo > 0 " +
           "ORDER BY cd.ngayHanChot ASC")
    List<CustomerDebt> findUnpaidDebtsByCustomer(@Param("khachHangId") Long khachHangId);
    
    /**
     * Tìm tất cả nợ quá hạn (ngày hôm nay > ngayHanChot)
     * 
     * @return Danh sách nợ quá hạn
     */
    @Query("SELECT cd FROM CustomerDebt cd WHERE cd.ngayHanChot < CURRENT_DATE AND cd.tongTienConNo > 0")
    List<CustomerDebt> findOverdueDebts();
    
    /**
     * Tính tổng tiền nợ còn lại (khoản phải thu) của tất cả khách hàng
     * Trả về 0 nếu không có nợ
     * 
     * @return Tổng tiền nợ
     */
    @Query("SELECT COALESCE(SUM(cd.tongTienConNo), 0) FROM CustomerDebt cd")
    BigDecimal calculateTotalRemainingDebt();
    
    /**
     * Tính tổng tiền nợ còn lại của một khách hàng
     * 
     * @param khachHangId ID khách hàng
     * @return Tổng tiền nợ của khách hàng
     */
    @Query("SELECT COALESCE(SUM(cd.tongTienConNo), 0) FROM CustomerDebt cd WHERE cd.khachHang.id = :khachHangId")
    BigDecimal calculateRemainingDebtByCustomer(@Param("khachHangId") Long khachHangId);
    
    /**
     * Tính tổng tiền nợ ban đầu (khoản phải thu) của tất cả khách hàng
     * 
     * @return Tổng tiền nợ
     */
    @Query("SELECT COALESCE(SUM(cd.tongTienNo), 0) FROM CustomerDebt cd")
    BigDecimal calculateTotalInitialDebt();
    
    /**
     * Tính tổng tiền đã thu hồi
     * 
     * @return Tổng tiền đã thu hồi
     */
    @Query("SELECT COALESCE(SUM(cd.tongTienDaThanhToan), 0) FROM CustomerDebt cd")
    BigDecimal calculateTotalCollected();
    
    /**
     * Đếm số nợ quá hạn
     * 
     * @return Số lượng nợ quá hạn
     */
    @Query("SELECT COUNT(cd) FROM CustomerDebt cd WHERE cd.ngayHanChot < CURRENT_DATE AND cd.tongTienConNo > 0")
    long countOverdueDebts();
    
    /**
     * Đếm số nợ đang duy trì của khách hàng
     * 
     * @param khachHangId ID khách hàng
     * @return Số lượng nợ
     */
    @Query("SELECT COUNT(cd) FROM CustomerDebt cd WHERE cd.khachHang.id = :khachHangId AND cd.tongTienConNo > 0")
    long countUnpaidDebtsByCustomer(@Param("khachHangId") Long khachHangId);
    
    /**
     * Tìm nợ với điều kiện phức tạp (theo trạng thái và khách hàng)
     * 
     * @param trangThai Trạng thái
     * @param khachHangId ID khách hàng
     * @return Danh sách nợ phù hợp
     */
    @Query("SELECT cd FROM CustomerDebt cd WHERE cd.trangThai = :trangThai AND cd.khachHang.id = :khachHangId " +
           "ORDER BY cd.ngayTaoNo DESC")
    List<CustomerDebt> findByStatusAndCustomer(@Param("trangThai") DebtStatus trangThai,
                                               @Param("khachHangId") Long khachHangId);
    
    /**
     * Tìm top N khách hàng nợ nhiều tiền nhất
     * 
     * @param limit Số lượng khách hàng
     * @return Danh sách nợ của khách hàng nợ nhiều nhất
     */
    @Query("SELECT cd FROM CustomerDebt cd WHERE cd.tongTienConNo > 0 " +
           "ORDER BY cd.tongTienConNo DESC LIMIT :limit")
    List<CustomerDebt> findTopDebtorCustomers(@Param("limit") int limit);
    
    /**
     * Tìm tất cả nợ với eager loading của khách hàng
     * Giải quyết LazyInitializationException bằng JOIN FETCH
     * 
     * @return Danh sách tất cả nợ với khách hàng đã được load
     */
    @Query("SELECT DISTINCT cd FROM CustomerDebt cd LEFT JOIN FETCH cd.khachHang")
    List<CustomerDebt> findAllWithKhachHang();
    
    /**
     * Tìm nợ khách hàng theo ID với eager loading
     * Giải quyết LazyInitializationException bằng JOIN FETCH
     * 
     * @param debtId ID của nợ khách hàng
     * @return Optional chứa nợ khách hàng với khách hàng và payments đã được load
     */
    @Query("SELECT cd FROM CustomerDebt cd " +
           "LEFT JOIN FETCH cd.khachHang " +
           "LEFT JOIN FETCH cd.payments " +
           "WHERE cd.id = :debtId")
    Optional<CustomerDebt> findByIdWithKhachHang(@Param("debtId") Long debtId);
    
    /**
     * Tìm tất cả nợ chưa thu hồi với eager loading của khách hàng
     * 
     * @return Danh sách nợ còn dư với khách hàng đã được load
     */
    @Query("SELECT DISTINCT cd FROM CustomerDebt cd LEFT JOIN FETCH cd.khachHang " +
           "WHERE cd.tongTienConNo > 0 ORDER BY cd.ngayHanChot ASC")
    List<CustomerDebt> findAllUnpaidDebtsWithKhachHang();
}
