package com.example.demo.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class HangHoaTest {
    
    @Test
    public void testHangHoaCreation() {
        HangHoa hangHoa = new HangHoa();
        hangHoa.setTenHangHoa("Laptop");
        hangHoa.setDonViTinh("Cái");
        hangHoa.setSoLuongTon(10);
        hangHoa.setGiaBan(BigDecimal.valueOf(15000000));
        
        assertEquals("Laptop", hangHoa.getTenHangHoa());
        assertEquals("Cái", hangHoa.getDonViTinh());
        assertEquals(10, hangHoa.getSoLuongTon());
        assertEquals(BigDecimal.valueOf(15000000), hangHoa.getGiaBan());
    }
    
    @Test
    public void testHangHoaEquality() {
        HangHoa hh1 = new HangHoa();
        hh1.setId(1L);
        hh1.setTenHangHoa("Product1");
        
        HangHoa hh2 = new HangHoa();
        hh2.setId(1L);
        hh2.setTenHangHoa("Product1");
        
        assertEquals(hh1.getId(), hh2.getId());
    }
    
    @Test
    public void testHangHoaValidation() {
        HangHoa hangHoa = new HangHoa();
        hangHoa.setTenHangHoa("Test");
        hangHoa.setSoLuongTon(5);
        
        assertNotNull(hangHoa.getTenHangHoa());
        assertTrue(hangHoa.getSoLuongTon() > 0);
    }
}
