package com.example.demo.config;

import com.example.demo.util.NumberFormatUtil;
import org.springframework.stereotype.Component;

@Component("numberFormatter")
public class NumberFormatterComponent {
    
    public String formatCurrency(Number amount) {
        return NumberFormatUtil.formatVietnameseCurrency(amount);
    }
    
    public String formatCurrencyWithUnit(Number amount) {
        return NumberFormatUtil.formatVietnameseCurrencyWithUnit(amount);
    }
}