package com.example.demo.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberFormatUtilTest {
    
    @Test
    void testFormatVietnameseCurrency() {
        // Test các trường hợp khác nhau
        assertEquals("1.000", NumberFormatUtil.formatVietnameseCurrency(1000));
        assertEquals("10.000", NumberFormatUtil.formatVietnameseCurrency(10000));
        assertEquals("100.000", NumberFormatUtil.formatVietnameseCurrency(100000));
        assertEquals("1.000.000", NumberFormatUtil.formatVietnameseCurrency(1000000));
        assertEquals("10.000.000", NumberFormatUtil.formatVietnameseCurrency(10000000));
        assertEquals("123.456.789", NumberFormatUtil.formatVietnameseCurrency(123456789));
        assertEquals("0", NumberFormatUtil.formatVietnameseCurrency(0));
        assertEquals("0", NumberFormatUtil.formatVietnameseCurrency(null));
    }
    
    @Test
    void testFormatVietnameseCurrencyWithUnit() {
        assertEquals("1.000.000 VNĐ", NumberFormatUtil.formatVietnameseCurrencyWithUnit(1000000));
        assertEquals("500.000 VNĐ", NumberFormatUtil.formatVietnameseCurrencyWithUnit(500000));
        assertEquals("0 VNĐ", NumberFormatUtil.formatVietnameseCurrencyWithUnit(null));
    }
}