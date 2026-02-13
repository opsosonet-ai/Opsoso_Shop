package com.example.demo.repository;

import com.example.demo.entity.PhieuXuat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhieuXuatRepository extends JpaRepository<PhieuXuat, Long> {
    
    @Query("SELECT DISTINCT p FROM PhieuXuat p LEFT JOIN FETCH p.chiTietList WHERE p.maPhieuXuat = :maPhieuXuat")
    PhieuXuat findByMaPhieuXuat(@Param("maPhieuXuat") String maPhieuXuat);
    
    List<PhieuXuat> findByTrangThai(String trangThai);
    List<PhieuXuat> findByNguoiXuat(String nguoiXuat);
}
