package com.example.demo.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

public class DoiTraTest {
    
    @Test
    public void testDoiTraHangHoaCreation() {
        DoiTraHangHoa dt = new DoiTraHangHoa();
        dt.setMaDoiTra("DT001");
        dt.setSoLuong(2);
        dt.setDonGia(BigDecimal.valueOf(75000));
        
        assertEquals("DT001", dt.getMaDoiTra());
        assertEquals(2, dt.getSoLuong());
    }
    
    @Test
    public void testDoiTraHangHoaValidation() {
        DoiTraHangHoa dt = new DoiTraHangHoa();
        assertNotNull(dt);
    }
    
    @Test
    public void testDoiTraHangHoaFields() {
        DoiTraHangHoa dt = new DoiTraHangHoa();
        dt.setMaDoiTra("DT002");
        dt.setTenKhachHang("Nguyen Van A");
        dt.setSoDienThoai("0123456789");
        dt.setLyDo("Wrong size");
        
        assertEquals("DT002", dt.getMaDoiTra());
        assertEquals("Nguyen Van A", dt.getTenKhachHang());
        assertEquals("0123456789", dt.getSoDienThoai());
    }
}
