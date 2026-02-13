package com.example.demo.repository;

import com.example.demo.entity.KhachHang;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    
    @Cacheable(value = "khachHangByHoTen", unless = "#result == null")
    List<KhachHang> findByHoTenContainingIgnoreCase(String hoTen);
    
    @Cacheable(value = "khachHangByLoai", unless = "#result == null")
    List<KhachHang> findByLoaiKhachHang(String loaiKhachHang);
    
    @Cacheable(value = "khachHangByEmail", unless = "#result == null")
    KhachHang findByEmail(String email);
    
    // Tìm kiếm khách hàng theo tên hoặc số điện thoại
    @Cacheable(value = "timKiemKhachHang", unless = "#result == null")
    @Query("SELECT k FROM KhachHang k WHERE " +
           "k.hoTen LIKE %:hoTen% OR k.soDienThoai LIKE %:soDienThoai%")
    List<KhachHang> timKiemKhachHang(@Param("hoTen") String hoTen, 
                                    @Param("soDienThoai") String soDienThoai);
}