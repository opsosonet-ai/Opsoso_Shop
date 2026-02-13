package com.example.demo.util;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class CurrencyFormatterTest {
    
    @Test
    public void testFormatCurrency() {
        String result = NumberFormatUtil.formatVietnameseCurrency(1000000);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
    
    @Test
    public void testFormatCurrencyZero() {
        String result = NumberFormatUtil.formatVietnameseCurrency(0);
        assertNotNull(result);
    }
    
    @Test
    public void testFormatCurrencyWithUnit() {
        String result = NumberFormatUtil.formatVietnameseCurrencyWithUnit(BigDecimal.valueOf(1000000));
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
