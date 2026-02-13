package com.example.demo.config;

import java.math.BigDecimal;

/**
 * Utility class để format tiền tệ trong Thymeleaf templates
 * Sử dụng: [[${#format.vnd(amount)}]] trong templates
 */
public class FormatUtility {
    
    /**
     * Format tiền tệ VND (không decimals)
     * @param amount Số tiền
     * @return Chuỗi "1.234.567 VNĐ"
     */
    public String vnd(Object amount) {
        if (amount == null) {
            return "0 VNĐ";
        }
        
        if (amount instanceof Number) {
            BigDecimal bd;
            if (amount instanceof BigDecimal decimal) {
                bd = decimal;
            } else if (amount instanceof Double double1) {
                bd = BigDecimal.valueOf(double1);
            } else if (amount instanceof Long long1) {
                bd = BigDecimal.valueOf(long1);
            } else if (amount instanceof Integer integer) {
                bd = BigDecimal.valueOf(integer.longValue());
            } else {
                return amount.toString() + " VNĐ";
            }
            
            return com.example.demo.utility.CurrencyFormatter.formatVND(bd);
        }
        
        return amount.toString() + " VNĐ";
    }
    
    /**
     * Format tiền tệ VND với ký hiệu (₫)
     * @param amount Số tiền
     * @return Chuỗi "1.234.567₫"
     */
    public String vndSymbol(Object amount) {
        if (amount instanceof BigDecimal decimal) {
            return com.example.demo.utility.CurrencyFormatter.formatVNDWithSymbol(decimal);
        }
        return vnd(amount);
    }
    
    /**
     * Format tiền tệ VND với decimals
     * @param amount Số tiền
     * @return Chuỗi "1.234.567,50 VNĐ"
     */
    public String vndDecimal(Object amount) {
        if (amount instanceof BigDecimal decimal) {
            return com.example.demo.utility.CurrencyFormatter.formatVNDWithDecimal(decimal);
        }
        return vnd(amount);
    }
    
    /**
     * Format rút gọn (Triệu)
     * @param amount Số tiền
     * @return Chuỗi "1.23M"
     */
    public String vndShort(Object amount) {
        if (amount instanceof BigDecimal decimal) {
            return com.example.demo.utility.CurrencyFormatter.formatVNDShort(decimal);
        }
        return vnd(amount);
    }
    
    /**
     * Chuyển đổi thành Triệu đồng
     * @param amount Số tiền
     * @return Giá trị double
     */
    public Double millions(Object amount) {
        if (amount instanceof BigDecimal decimal) {
            return com.example.demo.utility.CurrencyFormatter.toMillions(decimal);
        }
        return 0.0;
    }
}
