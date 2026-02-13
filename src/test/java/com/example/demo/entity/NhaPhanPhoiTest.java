package com.example.demo.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NhaPhanPhoiTest {
    
    @Test
    public void testNhaPhanPhoiCreation() {
        NhaPhanPhoi npp = new NhaPhanPhoi();
        npp.setTenNhaPhanPhoi("Distributor 1");
        npp.setDiaChi("123 Street");
        npp.setSoDienThoai("0123456789");
        
        assertEquals("Distributor 1", npp.getTenNhaPhanPhoi());
    }
    
    @Test
    public void testNhaPhanPhoiValidation() {
        NhaPhanPhoi npp = new NhaPhanPhoi();
        assertNotNull(npp);
    }
    
    @Test
    public void testNhaPhanPhoiContact() {
        NhaPhanPhoi npp = new NhaPhanPhoi();
        npp.setSoDienThoai("0987654321");
        npp.setEmail("contact@distributor.com");
        
        assertEquals("0987654321", npp.getSoDienThoai());
        assertEquals("contact@distributor.com", npp.getEmail());
    }
}
