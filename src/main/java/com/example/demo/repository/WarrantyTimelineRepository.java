package com.example.demo.repository;

import com.example.demo.entity.WarrantyTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarrantyTimelineRepository extends JpaRepository<WarrantyTimeline, Long> {
    
    /**
     * Tìm tất cả timeline của một bảo hành, với thông tin warranty được eager-load
     */
    @Query("SELECT DISTINCT wt FROM WarrantyTimeline wt " +
           "LEFT JOIN FETCH wt.warranty w " +
           "LEFT JOIN FETCH w.hangHoa " +
           "LEFT JOIN FETCH w.khachHang " +
           "WHERE w.id = :warrantyId " +
           "ORDER BY wt.thoiGianThucHien ASC")
    List<WarrantyTimeline> findByWarrantyIdWithRelations(@Param("warrantyId") Long warrantyId);
    
    /**
     * Tìm timeline theo warrantyId (đơn giản)
     */
    List<WarrantyTimeline> findByWarrantyIdOrderByThoiGianThucHienAsc(Long warrantyId);
    
    /**
     * Tìm timeline theo bước thực hiện
     */
    @Query("SELECT DISTINCT wt FROM WarrantyTimeline wt " +
           "LEFT JOIN FETCH wt.warranty w " +
           "LEFT JOIN FETCH w.hangHoa " +
           "LEFT JOIN FETCH w.khachHang " +
           "WHERE wt.buocThucHien = :buoc " +
           "ORDER BY wt.thoiGianThucHien DESC")
    List<WarrantyTimeline> findByBuocThucHien(@Param("buoc") String buoc);
}
