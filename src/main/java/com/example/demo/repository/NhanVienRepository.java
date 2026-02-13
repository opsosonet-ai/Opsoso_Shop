package com.example.demo.repository;

import com.example.demo.entity.NhanVien;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {
    
    @Cacheable(value = "nhanVienByHoTen", unless = "#result == null")
    List<NhanVien> findByHoTenContainingIgnoreCase(String hoTen);
    
    @Cacheable(value = "nhanVienByPhongBan", unless = "#result == null")
    List<NhanVien> findByPhongBan(String phongBan);
    
    @Cacheable(value = "nhanVienByChucVu", unless = "#result == null")
    List<NhanVien> findByChucVu(String chucVu);
}