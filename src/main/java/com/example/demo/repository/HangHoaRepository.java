package com.example.demo.repository;

import com.example.demo.entity.HangHoa;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HangHoaRepository extends JpaRepository<HangHoa, Long> {
    
    @Cacheable(value = "hangHoaByTen", unless = "#result == null")
    List<HangHoa> findByTenHangHoaContainingIgnoreCase(String tenHangHoa);
    
    @Cacheable(value = "hangHoaByLoai", unless = "#result == null")
    List<HangHoa> findByLoaiHangHoa(String loaiHangHoa);
    
    @Cacheable(value = "hangHoaByThuongHieu", unless = "#result == null")
    List<HangHoa> findByThuongHieu(String thuongHieu);
    
    @Cacheable(value = "hangHoaByMa", unless = "#result == null")
    HangHoa findByMaHangHoa(String maHangHoa);
    
    @Cacheable(value = "hangHoaBySoSerial", unless = "#result == null")
    Optional<HangHoa> findBySoSerial(String soSerial);
    
    @Cacheable(value = "hangHoaBySoLuongTon", unless = "#result == null")
    List<HangHoa> findBySoLuongTonGreaterThan(int soLuongTon);
    
    // Tìm tất cả hàng hóa sắp xếp theo tên
    @Cacheable(value = "allHangHoaOrdered", unless = "#result == null")
    List<HangHoa> findAllByOrderByTenHangHoaAsc();
    
    // ✅ NEW: Eager loading for all items with supplier
    @Query("SELECT DISTINCT h FROM HangHoa h " +
           "LEFT JOIN FETCH h.nhaPhanPhoi")
    List<HangHoa> findAllWithSupplier();
    
    // ✅ NEW: Find single item with supplier
    @Query("SELECT h FROM HangHoa h " +
           "LEFT JOIN FETCH h.nhaPhanPhoi " +
           "WHERE h.id = :id")
    Optional<HangHoa> findByIdWithSupplier(@Param("id") Long id);
}