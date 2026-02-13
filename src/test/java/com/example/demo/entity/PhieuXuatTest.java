package com.example.demo.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

public class PhieuXuatTest {
    
    @Test
    public void testPhieuXuatCreation() {
        PhieuXuat phieuXuat = new PhieuXuat();
        phieuXuat.setMaPhieuXuat("PX20251101001");
        phieuXuat.setNguoiXuat("Nhân viên A");
        phieuXuat.setNgayXuat(LocalDateTime.now());
        phieuXuat.setTongTien(java.math.BigDecimal.valueOf(100000));
        
        assertEquals("PX20251101001", phieuXuat.getMaPhieuXuat());
        assertEquals("Nhân viên A", phieuXuat.getNguoiXuat());
        assertNotNull(phieuXuat.getNgayXuat());
    }
    
    @Test
    public void testPhieuXuatTongTien() {
        PhieuXuat phieuXuat = new PhieuXuat();
        phieuXuat.setTongTien(java.math.BigDecimal.valueOf(500000));
        
        assertTrue(phieuXuat.getTongTien().compareTo(java.math.BigDecimal.ZERO) > 0);
    }
    
    @Test
    public void testPhieuXuatValidation() {
        PhieuXuat phieuXuat = new PhieuXuat();
        phieuXuat.setMaPhieuXuat("PX001");
        phieuXuat.setNguoiXuat("Test");
        
        assertNotNull(phieuXuat.getMaPhieuXuat());
        assertTrue(phieuXuat.getMaPhieuXuat().startsWith("PX"));
    }
}
