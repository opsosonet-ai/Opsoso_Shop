package com.example.demo.repository;

import com.example.demo.entity.WarrantyClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim, Long> {
    
    // Tìm yêu cầu bảo hành theo warranty (với JOIN FETCH)
    @Query("SELECT DISTINCT wc FROM WarrantyClaim wc " +
           "LEFT JOIN FETCH wc.warranty w " +
           "LEFT JOIN FETCH w.hangHoa " +
           "LEFT JOIN FETCH w.khachHang " +
           "LEFT JOIN FETCH wc.nhanVienXuLy " +
           "WHERE w.id = :warrantyId")
    List<WarrantyClaim> findByWarrantyId(@Param("warrantyId") Long warrantyId);
    
    // Tìm yêu cầu bảo hành theo trạng thái (với JOIN FETCH)
    @Query("SELECT DISTINCT wc FROM WarrantyClaim wc " +
           "LEFT JOIN FETCH wc.warranty w " +
           "LEFT JOIN FETCH w.hangHoa " +
           "LEFT JOIN FETCH w.khachHang " +
           "LEFT JOIN FETCH wc.nhanVienXuLy " +
           "WHERE wc.trangThai = :trangThai")
    List<WarrantyClaim> findByTrangThai(@Param("trangThai") String trangThai);
    
    // Tìm yêu cầu theo nhân viên xử lý (với JOIN FETCH)
    @Query("SELECT DISTINCT wc FROM WarrantyClaim wc " +
           "LEFT JOIN FETCH wc.warranty w " +
           "LEFT JOIN FETCH w.hangHoa " +
           "LEFT JOIN FETCH w.khachHang " +
           "LEFT JOIN FETCH wc.nhanVienXuLy " +
           "WHERE wc.nhanVienXuLy.id = :nhanVienXuLyId")
    List<WarrantyClaim> findByNhanVienXuLyId(@Param("nhanVienXuLyId") Long nhanVienXuLyId);
    
    // Lấy chi tiết yêu cầu bảo hành theo ID (với JOIN FETCH)
    @Query("SELECT wc FROM WarrantyClaim wc " +
           "LEFT JOIN FETCH wc.warranty w " +
           "LEFT JOIN FETCH w.hangHoa " +
           "LEFT JOIN FETCH w.khachHang " +
           "LEFT JOIN FETCH wc.nhanVienXuLy " +
           "WHERE wc.id = :id")
    java.util.Optional<WarrantyClaim> findByIdWithRelations(@org.springframework.data.repository.query.Param("id") Long id);
}
