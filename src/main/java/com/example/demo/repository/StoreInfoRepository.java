package com.example.demo.repository;

import com.example.demo.entity.StoreInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreInfoRepository extends JpaRepository<StoreInfo, Long> {
    
    /**
     * Lấy thông tin cửa hàng đầu tiên (chỉ có một record duy nhất)
     */
    Optional<StoreInfo> findFirstBy();
}