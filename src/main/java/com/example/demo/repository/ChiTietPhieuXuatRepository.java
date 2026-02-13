package com.example.demo.repository;

import com.example.demo.entity.ChiTietPhieuXuat;
import com.example.demo.entity.HangHoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChiTietPhieuXuatRepository extends JpaRepository<ChiTietPhieuXuat, Long> {
    
    // Lấy tất cả chi tiết phiếu xuất với thông tin đầy đủ
    @Query("SELECT ct FROM ChiTietPhieuXuat ct " +
           "JOIN FETCH ct.hangHoa " +
           "JOIN FETCH ct.phieuXuat px " +
           "LEFT JOIN FETCH px.khachHang " +
           "ORDER BY px.ngayXuat DESC")
    List<ChiTietPhieuXuat> findAllWithDetails();
    
    // Lấy tất cả phiếu xuất với thông tin khách hàng (sử dụng DTO để tránh LazyInitializationException)
    @Query("SELECT NEW com.example.demo.dto.PhieuXuatDTO(" +
           "px.id, " +
           "px.maPhieuXuat, " +
           "COALESCE(kh.hoTen, 'Khách lẻ'), " +
           "COALESCE(kh.soDienThoai, ''), " +
           "COALESCE(kh.diaChi, ''), " +
           "px.ngayXuat) " +
           "FROM PhieuXuat px " +
           "LEFT JOIN px.khachHang kh " +
           "WHERE px.trangThai = 'Đã xuất' " +
           "ORDER BY px.ngayXuat DESC")
    List<com.example.demo.dto.PhieuXuatDTO> findAllPhieuXuatWithDetails();
    
    // Lấy chi tiết các hàng hóa trong phiếu xuất cụ thể
    @Query("SELECT ct FROM ChiTietPhieuXuat ct " +
           "JOIN FETCH ct.hangHoa " +
           "WHERE ct.phieuXuat.id = :phieuXuatId " +
           "ORDER BY ct.hangHoa.tenHangHoa ASC")
    List<ChiTietPhieuXuat> findByPhieuXuatId(@Param("phieuXuatId") Long phieuXuatId);
    
    // Lấy danh sách hàng hóa đã bán trong khoảng thời gian (cho đổi trả)
    @Query("SELECT DISTINCT ct.hangHoa FROM ChiTietPhieuXuat ct " +
           "JOIN ct.phieuXuat px " +
           "WHERE px.ngayXuat >= :fromDate " +
           "AND px.trangThai = 'Đã xuất' " +
           "ORDER BY ct.hangHoa.tenHangHoa ASC")
    List<HangHoa> findSoldProductsInPeriod(@Param("fromDate") LocalDateTime fromDate);
    
    // Lấy chi tiết sản phẩm đã bán trong khoảng thời gian với thông tin chi tiết
    @Query("SELECT ct FROM ChiTietPhieuXuat ct " +
           "JOIN FETCH ct.hangHoa " +
           "JOIN FETCH ct.phieuXuat px " +
           "LEFT JOIN FETCH px.khachHang " +
           "WHERE px.ngayXuat >= :fromDate " +
           "AND px.trangThai = 'Đã xuất' " +
           "ORDER BY px.ngayXuat DESC")
    List<ChiTietPhieuXuat> findSoldProductDetailsInPeriod(@Param("fromDate") LocalDateTime fromDate);
    
    // Tìm chi tiết phiếu xuất theo hàng hóa
    @Query("SELECT ct FROM ChiTietPhieuXuat ct " +
           "JOIN FETCH ct.phieuXuat px " +
           "LEFT JOIN FETCH px.khachHang " +
           "WHERE ct.hangHoa.id = :hangHoaId " +
           "AND px.trangThai = 'Đã xuất' " +
           "ORDER BY px.ngayXuat DESC")
    List<ChiTietPhieuXuat> findByHangHoaId(@Param("hangHoaId") Long hangHoaId);
    
    // Tìm khách hàng đã mua hàng hóa cụ thể
    @Query("SELECT DISTINCT kh.id, kh.hoTen, kh.soDienThoai, kh.diaChi, " +
           "px.maPhieuXuat, px.ngayXuat, ct.soLuong, ct.donGia " +
           "FROM ChiTietPhieuXuat ct " +
           "JOIN ct.phieuXuat px " +
           "LEFT JOIN px.khachHang kh " +
           "WHERE ct.hangHoa.id = :hangHoaId " +
           "AND px.trangThai = 'Đã xuất' " +
           "ORDER BY px.ngayXuat DESC")
    List<Object[]> findKhachHangDaMuaHangHoa(@Param("hangHoaId") Long hangHoaId);
    
    // Lấy danh sách hàng hóa đã bán cho khách hàng cụ thể
    @Query("SELECT DISTINCT ct FROM ChiTietPhieuXuat ct " +
           "JOIN FETCH ct.hangHoa " +
           "JOIN FETCH ct.phieuXuat px " +
           "WHERE px.khachHang.id = :khachHangId " +
           "AND px.trangThai = 'Đã xuất' " +
           "ORDER BY px.ngayXuat DESC")
    List<ChiTietPhieuXuat> findByKhachHangId(@Param("khachHangId") Long khachHangId);
    
    // Lấy danh sách hàng hóa đã bán cho khách hàng theo ID (chỉ lấy HangHoa)
    @Query("SELECT DISTINCT ct.hangHoa FROM ChiTietPhieuXuat ct " +
           "JOIN ct.phieuXuat px " +
           "WHERE px.khachHang.id = :khachHangId " +
           "AND px.trangThai = 'Đã xuất' " +
           "ORDER BY ct.hangHoa.tenHangHoa ASC")
    List<HangHoa> findHangHoaBySoldToKhachHang(@Param("khachHangId") Long khachHangId);
    
    // Lấy danh sách hàng hóa + ngày bán (ngayXuat) cho khách hàng
    // Trả về Object[] với [id, tenHangHoa, ngayBan (LocalDate), soLuongTon]
    @Query("SELECT NEW com.example.demo.dto.HangHoaBanDTO(" +
           "ct.hangHoa.id, " +
           "ct.hangHoa.tenHangHoa, " +
           "CAST(px.ngayXuat AS java.time.LocalDate), " +
           "ct.hangHoa.soLuongTon) " +
           "FROM ChiTietPhieuXuat ct " +
           "JOIN ct.phieuXuat px " +
           "JOIN ct.hangHoa hh " +
           "WHERE px.khachHang.id = :khachHangId " +
           "AND px.trangThai = 'Đã xuất' " +
           "ORDER BY px.ngayXuat DESC")
    List<com.example.demo.dto.HangHoaBanDTO> findHangHoaBanWithNgayBan(@Param("khachHangId") Long khachHangId);
}
