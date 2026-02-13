package com.example.demo.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ChiTietPhieuXuatTest {
    
    @Test
    public void testChiTietPhieuXuatCreation() {
        ChiTietPhieuXuat ct = new ChiTietPhieuXuat();
        ct.setSoLuong(5);
        ct.setDonGia(java.math.BigDecimal.valueOf(100000));
        
        assertEquals(5, ct.getSoLuong());
    }
    
    @Test
    public void testChiTietPhieuXuatValidation() {
        ChiTietPhieuXuat ct = new ChiTietPhieuXuat();
        assertNotNull(ct);
    }
    
    @Test
    public void testChiTietPhieuXuatFields() {
        ChiTietPhieuXuat ct = new ChiTietPhieuXuat();
        ct.setSoLuong(10);
        ct.setGhiChu("Test note");
        
        assertEquals(10, ct.getSoLuong());
        assertEquals("Test note", ct.getGhiChu());
    }
}
