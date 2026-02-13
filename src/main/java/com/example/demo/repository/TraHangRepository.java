package com.example.demo.repository;

import com.example.demo.entity.TraHang;
import com.example.demo.entity.TraHang.TrangThaiTraHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TraHangRepository extends JpaRepository<TraHang, Long> {
    
    // Tìm theo mã trả hàng
    Optional<TraHang> findByMaTraHang(String maTraHang);
    
    // Tìm theo trạng thái
    List<TraHang> findByTrangThai(TrangThaiTraHang trangThai);
    Page<TraHang> findByTrangThai(TrangThaiTraHang trangThai, Pageable pageable);
    
    // Tìm theo khách hàng
    List<TraHang> findByTenKhachHangContainingIgnoreCase(String tenKhachHang);
    List<TraHang> findBySoDienThoaiContaining(String soDienThoai);
    
    // Tìm theo ngày
    @Query("SELECT t FROM TraHang t WHERE t.ngayTraHang BETWEEN :tuNgay AND :denNgay")
    List<TraHang> findByNgayTraHangBetween(@Param("tuNgay") LocalDateTime tuNgay, 
                                          @Param("denNgay") LocalDateTime denNgay);
    
    // Tìm theo hàng hóa
    @Query("SELECT t FROM TraHang t WHERE t.hangHoa.tenHangHoa LIKE %:tenHangHoa%")
    List<TraHang> findByHangHoaTenContaining(@Param("tenHangHoa") String tenHangHoa);
    
    // Đếm theo trạng thái
    long countByTrangThai(TrangThaiTraHang trangThai);
    
    // Tìm kiếm tổng hợp
    @Query("SELECT t FROM TraHang t WHERE " +
           "(:maTraHang IS NULL OR t.maTraHang LIKE %:maTraHang%) AND " +
           "(:tenKhachHang IS NULL OR t.tenKhachHang LIKE %:tenKhachHang%) AND " +
           "(:soDienThoai IS NULL OR t.soDienThoai LIKE %:soDienThoai%) AND " +
           "(:trangThai IS NULL OR t.trangThai = :trangThai)")
    Page<TraHang> timKiemTraHang(@Param("maTraHang") String maTraHang,
                                @Param("tenKhachHang") String tenKhachHang,
                                @Param("soDienThoai") String soDienThoai,
                                @Param("trangThai") TrangThaiTraHang trangThai,
                                Pageable pageable);
    
    // Tạo mã trả hàng tự động
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(t.maTraHang, 3) AS int)), 0) FROM TraHang t WHERE t.maTraHang LIKE 'TH%'")
    Integer findMaxMaTraHangNumber();
    
    // Find the last mã trả hàng to extract sequence number
    @Query("SELECT t.maTraHang FROM TraHang t ORDER BY t.createdAt DESC LIMIT 1")
    String findLastMaTraHang();
    
    // Find all with eager loading of hangHoa to avoid LazyInitializationException
    @Query("SELECT DISTINCT t FROM TraHang t LEFT JOIN FETCH t.hangHoa")
    List<TraHang> findAllWithEagerHangHoa();
    
    // Find one with eager loading of hangHoa to avoid LazyInitializationException
    @Query("SELECT t FROM TraHang t LEFT JOIN FETCH t.hangHoa WHERE t.id = :id")
    Optional<TraHang> findByIdWithEagerHangHoa(@Param("id") Long id);
    
    // ✅ NEW: Find by status with eager loading and pagination support
    @Query("SELECT t FROM TraHang t LEFT JOIN FETCH t.hangHoa " +
           "WHERE t.trangThai = :status ORDER BY t.ngayTraHang DESC")
    List<TraHang> findByStatusWithEagerHangHoa(@Param("status") TrangThaiTraHang status);
}