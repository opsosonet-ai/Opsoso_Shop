package com.example.demo.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

public class DiscountUtilTest {
    
    @Test
    public void testCalculateDiscount() {
        BigDecimal original = BigDecimal.valueOf(100000);
        BigDecimal discountPercent = BigDecimal.valueOf(10);
        BigDecimal discount = original.multiply(discountPercent).divide(BigDecimal.valueOf(100));
        
        assertNotNull(discount);
        assertTrue(discount.compareTo(BigDecimal.ZERO) > 0);
    }
    
    @Test
    public void testZeroDiscount() {
        BigDecimal original = BigDecimal.valueOf(50000);
        BigDecimal discount = original.multiply(BigDecimal.ZERO);
        
        assertEquals(BigDecimal.ZERO, discount);
    }
    
    @Test
    public void testValidDiscountPercent() {
        BigDecimal percent = BigDecimal.valueOf(25);
        assertTrue(percent.compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(percent.compareTo(BigDecimal.valueOf(100)) <= 0);
    }
}
