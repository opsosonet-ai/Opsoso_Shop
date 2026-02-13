package com.example.demo.repository;

import com.example.demo.entity.DoiTraHangHoa;
import com.example.demo.entity.HangHoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoiTraHangHoaRepository extends JpaRepository<DoiTraHangHoa, Long> {
    
    // Tìm theo mã đổi trả
    Optional<DoiTraHangHoa> findByMaDoiTra(String maDoiTra);
    
    // Tìm theo hàng hóa
    List<DoiTraHangHoa> findByHangHoaOrderByNgayDoiTraDesc(HangHoa hangHoa);
    
    // Tìm theo trạng thái
    List<DoiTraHangHoa> findByTrangThaiOrderByNgayDoiTraDesc(DoiTraHangHoa.TrangThaiDoiTra trangThai);
    
    // Tìm theo loại đổi trả
    List<DoiTraHangHoa> findByLoaiDoiTraOrderByNgayDoiTraDesc(DoiTraHangHoa.LoaiDoiTra loaiDoiTra);
    
    // Tìm theo khách hàng
    List<DoiTraHangHoa> findByTenKhachHangContainingIgnoreCaseOrderByNgayDoiTraDesc(String tenKhachHang);
    
    // Tìm theo số điện thoại
    List<DoiTraHangHoa> findBySoDienThoaiOrderByNgayDoiTraDesc(String soDienThoai);
    
    // Tìm tất cả sắp xếp theo ngày đổi trả mới nhất
    List<DoiTraHangHoa> findAllByOrderByNgayDoiTraDesc();
    
    // Tìm theo khoảng thời gian
    @Query("SELECT d FROM DoiTraHangHoa d WHERE d.ngayDoiTra BETWEEN :startDate AND :endDate ORDER BY d.ngayDoiTra DESC")
    List<DoiTraHangHoa> findByNgayDoiTraBetween(@Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);
    
    // Đếm số lượng theo trạng thái
    long countByTrangThai(DoiTraHangHoa.TrangThaiDoiTra trangThai);
    
    // Đếm số lượng theo loại đổi trả
    long countByLoaiDoiTra(DoiTraHangHoa.LoaiDoiTra loaiDoiTra);
    
    // Tìm mã đổi trả lớn nhất để tạo mã mới
    @Query("SELECT d.maDoiTra FROM DoiTraHangHoa d ORDER BY d.maDoiTra DESC LIMIT 1")
    Optional<String> findLastMaDoiTra();
    
    // Tìm kiếm tổng hợp
    @Query("SELECT d FROM DoiTraHangHoa d WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "d.maDoiTra LIKE %:keyword% OR " +
           "d.tenKhachHang LIKE %:keyword% OR " +
           "d.soDienThoai LIKE %:keyword% OR " +
           "d.hangHoa.tenHangHoa LIKE %:keyword%) " +
           "ORDER BY d.ngayDoiTra DESC")
    List<DoiTraHangHoa> findByKeyword(@Param("keyword") String keyword);
}