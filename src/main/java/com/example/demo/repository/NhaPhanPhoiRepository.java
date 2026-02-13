package com.example.demo.repository;

import com.example.demo.entity.NhaPhanPhoi;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NhaPhanPhoiRepository extends JpaRepository<NhaPhanPhoi, String> {
    
    @Cacheable(value = "nhaPhanPhoiByTen", unless = "#result == null")
    List<NhaPhanPhoi> findByTenNhaPhanPhoiContainingIgnoreCase(String tenNhaPhanPhoi);
    
    @Cacheable(value = "nhaPhanPhoiByTrangThai", unless = "#result == null")
    List<NhaPhanPhoi> findByTrangThai(String trangThai);
    
    @Cacheable(value = "nhaPhanPhoiByLinhVuc", unless = "#result == null")
    List<NhaPhanPhoi> findByLinhVuc(String linhVuc);
}