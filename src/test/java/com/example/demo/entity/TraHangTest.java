package com.example.demo.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

public class TraHangTest {
    
    @Test
    public void testTraHangCreation() {
        TraHang th = new TraHang();
        th.setMaTraHang("TH001");
        th.setLyDo("Damaged product");
        th.setSoLuong(5);
        
        assertEquals("TH001", th.getMaTraHang());
        assertEquals("Damaged product", th.getLyDo());
    }
    
    @Test
    public void testTraHangValidation() {
        TraHang th = new TraHang();
        assertNotNull(th);
    }
    
    @Test
    public void testTraHangFields() {
        TraHang th = new TraHang();
        th.setMaTraHang("TH002");
        th.setSoLuong(3);
        th.setTrangThai(TraHang.TrangThaiTraHang.CHO_DUYET);
        th.setDonGia(BigDecimal.valueOf(50000));
        
        assertEquals("TH002", th.getMaTraHang());
        assertEquals(3, th.getSoLuong());
        assertEquals(TraHang.TrangThaiTraHang.CHO_DUYET, th.getTrangThai());
    }
}
