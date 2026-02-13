package com.example.demo.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KhachHangTest {
    
    @Test
    public void testKhachHangCreation() {
        KhachHang khachHang = new KhachHang();
        khachHang.setHoTen("Trần Văn B");
        khachHang.setEmail("tvb@example.com");
        khachHang.setSoDienThoai("0987654321");
        khachHang.setDiaChi("123 Đường ABC");
        
        assertEquals("Trần Văn B", khachHang.getHoTen());
        assertEquals("tvb@example.com", khachHang.getEmail());
        assertEquals("0987654321", khachHang.getSoDienThoai());
    }
    
    @Test
    public void testKhachHangEmail() {
        KhachHang khachHang = new KhachHang();
        khachHang.setEmail("customer@company.com");
        
        assertNotNull(khachHang.getEmail());
        assertTrue(khachHang.getEmail().contains("@"));
    }
    
    @Test
    public void testKhachHangValidation() {
        KhachHang khachHang = new KhachHang();
        khachHang.setHoTen("Valid Name");
        khachHang.setDiaChi("Valid Address");
        
        assertNotNull(khachHang.getHoTen());
        assertNotNull(khachHang.getDiaChi());
        assertTrue(khachHang.getHoTen().length() > 0);
    }
}
