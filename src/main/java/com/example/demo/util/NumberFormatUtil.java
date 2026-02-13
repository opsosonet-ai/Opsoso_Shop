package com.example.demo.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class NumberFormatUtil {
    
    private static final DecimalFormat vietnamCurrencyFormat;
    
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.'); // Dấu chấm làm phân cách hàng nghìn
        symbols.setDecimalSeparator(',');   // Dấu phẩy làm phân cách thập phân (nếu cần)
        
        vietnamCurrencyFormat = new DecimalFormat("#,###", symbols);
    }
    
    /**
     * Định dạng số tiền theo chuẩn Việt Nam
     * Ví dụ: 1000000 -> "1.000.000"
     */
    public static String formatVietnameseCurrency(Number amount) {
        if (amount == null) {
            return "0";
        }
        return vietnamCurrencyFormat.format(amount);
    }
    
    /**
     * Định dạng số tiền với đơn vị VNĐ
     * Ví dụ: 1000000 -> "1.000.000 VNĐ"
     */
    public static String formatVietnameseCurrencyWithUnit(Number amount) {
        return formatVietnameseCurrency(amount) + " VNĐ";
    }
}