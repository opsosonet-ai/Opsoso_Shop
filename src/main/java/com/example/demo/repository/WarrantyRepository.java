package com.example.demo.repository;

import com.example.demo.entity.Warranty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WarrantyRepository extends JpaRepository<Warranty, Long> {
    
    // Tìm bảo hành theo khách hàng (với JOIN FETCH)
    @Query("SELECT DISTINCT w FROM Warranty w " +
           "LEFT JOIN FETCH w.hangHoa " +
           "LEFT JOIN FETCH w.khachHang " +
           "WHERE w.khachHang.id = :khachHangId")
    List<Warranty> findByKhachHangId(@Param("khachHangId") Long khachHangId);
    
    // Tìm bảo hành theo hàng hóa (với JOIN FETCH)
    @Query("SELECT DISTINCT w FROM Warranty w " +
           "LEFT JOIN FETCH w.hangHoa " +
           "LEFT JOIN FETCH w.khachHang " +
           "WHERE w.hangHoa.id = :hangHoaId")
    List<Warranty> findByHangHoaId(@Param("hangHoaId") Long hangHoaId);
    
    // Tìm bảo hành theo trạng thái (với JOIN FETCH)
    @Query("SELECT DISTINCT w FROM Warranty w " +
           "LEFT JOIN FETCH w.hangHoa " +
           "LEFT JOIN FETCH w.khachHang " +
           "WHERE w.trangThai = :trangThai")
    List<Warranty> findByTrangThai(@Param("trangThai") String trangThai);
    
    // Tìm bảo hành còn hiệu lực (với JOIN FETCH)
    @Query("SELECT DISTINCT w FROM Warranty w " +
           "LEFT JOIN FETCH w.hangHoa " +
           "LEFT JOIN FETCH w.khachHang " +
           "WHERE w.ngayHetHanBaoHanh >= :date " +
           "ORDER BY w.ngayHetHanBaoHanh ASC")
    List<Warranty> findByNgayHetHanBaoHanhGreaterThanEqual(@Param("date") LocalDate date);
    
    // Tìm bảo hành hết hạn (với JOIN FETCH)
    @Query("SELECT DISTINCT w FROM Warranty w " +
           "LEFT JOIN FETCH w.hangHoa " +
           "LEFT JOIN FETCH w.khachHang " +
           "WHERE w.ngayHetHanBaoHanh < :date " +
           "ORDER BY w.ngayHetHanBaoHanh DESC")
    List<Warranty> findByNgayHetHanBaoHanhLessThan(@Param("date") LocalDate date);
    
    // Tìm bảo hành trong khoảng thời gian (với JOIN FETCH)
    @Query("SELECT DISTINCT w FROM Warranty w " +
           "LEFT JOIN FETCH w.hangHoa " +
           "LEFT JOIN FETCH w.khachHang " +
           "WHERE w.ngayHetHanBaoHanh BETWEEN :startDate AND :endDate " +
           "ORDER BY w.ngayHetHanBaoHanh ASC")
    List<Warranty> findByNgayHetHanBaoHanhBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Tìm bảo hành theo chi tiết phiếu xuất (với JOIN FETCH)
    @Query("SELECT w FROM Warranty w " +
           "LEFT JOIN FETCH w.hangHoa " +
           "LEFT JOIN FETCH w.khachHang " +
           "WHERE w.chiTietPhieuXuatId = :chiTietPhieuXuatId")
    Warranty findByChiTietPhieuXuatId(@Param("chiTietPhieuXuatId") Long chiTietPhieuXuatId);
    
    // Lấy toàn bộ bảo hành (với JOIN FETCH) - sử dụng cho danh sách
    @Query("SELECT DISTINCT w FROM Warranty w " +
           "LEFT JOIN FETCH w.hangHoa " +
           "LEFT JOIN FETCH w.khachHang " +
           "ORDER BY w.id DESC")
    @org.springframework.lang.NonNull
    List<Warranty> findAll();
    
    // Lấy chi tiết bảo hành theo ID (với JOIN FETCH) - sử dụng cho detail page
    @Query("SELECT w FROM Warranty w " +
           "LEFT JOIN FETCH w.hangHoa " +
           "LEFT JOIN FETCH w.khachHang " +
           "LEFT JOIN FETCH w.timelines " +
           "WHERE w.id = :id")
    java.util.Optional<Warranty> findByIdWithRelations(@org.springframework.data.repository.query.Param("id") Long id);
}
