package com.example.demo.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StoreInfoTest {
    
    @Test
    public void testStoreInfoCreation() {
        StoreInfo storeInfo = new StoreInfo();
        storeInfo.setStoreName("Cửa hàng ABC");
        storeInfo.setAddress("123 Đường Chính");
        storeInfo.setPhone("0123456789");
        storeInfo.setEmail("store@example.com");
        storeInfo.setTaxCode("1234567890");
        
        assertEquals("Cửa hàng ABC", storeInfo.getStoreName());
        assertEquals("123 Đường Chính", storeInfo.getAddress());
        assertEquals("0123456789", storeInfo.getPhone());
    }
    
    @Test
    public void testStoreInfoValidation() {
        StoreInfo storeInfo = new StoreInfo();
        storeInfo.setStoreName("Test Store");
        storeInfo.setTaxCode("123");
        
        assertNotNull(storeInfo.getStoreName());
        assertNotNull(storeInfo.getTaxCode());
        assertTrue(storeInfo.getStoreName().length() > 0);
    }
    
    @Test
    public void testStoreInfoContact() {
        StoreInfo storeInfo = new StoreInfo();
        storeInfo.setPhone("0123456789");
        storeInfo.setEmail("test@store.com");
        
        assertNotNull(storeInfo.getPhone());
        assertTrue(storeInfo.getEmail().contains("@"));
    }
}
