package com.example.demo.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NhanVienTest {
    
    @Test
    public void testNhanVienCreation() {
        NhanVien nhanVien = new NhanVien();
        nhanVien.setHoTen("Nguyễn Văn A");
        nhanVien.setEmail("nva@example.com");
        nhanVien.setSoDienThoai("0123456789");
        nhanVien.setChucVu("Quản lý");
        nhanVien.setPhongBan("IT");
        nhanVien.setLuong(10000000.0);
        
        assertEquals("Nguyễn Văn A", nhanVien.getHoTen());
        assertEquals("nva@example.com", nhanVien.getEmail());
        assertEquals("0123456789", nhanVien.getSoDienThoai());
        assertEquals("Quản lý", nhanVien.getChucVu());
    }
    
    @Test
    public void testNhanVienValidation() {
        NhanVien nhanVien = new NhanVien();
        nhanVien.setHoTen("Test");
        nhanVien.setEmail("test@example.com");
        
        assertNotNull(nhanVien.getHoTen());
        assertTrue(nhanVien.getEmail().contains("@"));
    }
    
    @Test
    public void testNhanVienLuong() {
        NhanVien nhanVien = new NhanVien();
        nhanVien.setLuong(5000000.0);
        
        assertTrue(nhanVien.getLuong() > 0);
        assertEquals(5000000.0, nhanVien.getLuong());
    }
}