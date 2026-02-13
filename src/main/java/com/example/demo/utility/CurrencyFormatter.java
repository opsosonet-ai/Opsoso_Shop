package com.example.demo.utility;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class để format tiền tệ Việt Nam
 * Hiển thị định dạng: 1.234.567 VNĐ
 */
public class CurrencyFormatter {
    
    private static final Locale VIETNAM_LOCALE = new Locale.Builder().setLanguage("vi").setRegion("VN").build();
    private static final String CURRENCY_SYMBOL = "₫"; // Ký hiệu đồng
    
    /**
     * Format BigDecimal sang chuỗi tiền tệ VND
     * Ví dụ: 1234567.50 -> "1.234.567 VNĐ"
     * 
     * @param amount Số tiền cần format
     * @return Chuỗi định dạng tiền tệ
     */
    public static String formatVND(BigDecimal amount) {
        if (amount == null) {
            return "0 VNĐ";
        }
        
        // Tính toán để đạt được số nguyên (không lấy decimals)
        long longAmount = amount.longValue();
        
        return formatVND(longAmount);
    }
    
    /**
     * Format Long sang chuỗi tiền tệ VND
     * 
     * @param amount Số tiền cần format
     * @return Chuỗi định dạng tiền tệ
     */
    public static String formatVND(Long amount) {
        if (amount == null) {
            return "0 VNĐ";
        }
        
        return formatVND(amount.doubleValue());
    }
    
    /**
     * Format Double sang chuỗi tiền tệ VND
     * Ví dụ: 1234567.50 -> "1.234.567 VNĐ"
     * 
     * @param amount Số tiền cần format
     * @return Chuỗi định dạng tiền tệ
     */
    public static String formatVND(Double amount) {
        if (amount == null || amount == 0) {
            return "0 VNĐ";
        }
        
        // Sử dụng NumberFormat với locale Việt Nam
        NumberFormat numberFormat = NumberFormat.getInstance(VIETNAM_LOCALE);
        numberFormat.setMaximumFractionDigits(0); // Không hiển thị phần thập phân
        numberFormat.setGroupingUsed(true); // Sử dụng dấu phân cách hàng nghìn
        
        String formatted = numberFormat.format(amount.longValue());
        
        return formatted + " VNĐ";
    }
    
    /**
     * Format Integer sang chuỗi tiền tệ VND
     * 
     * @param amount Số tiền cần format
     * @return Chuỗi định dạng tiền tệ
     */
    public static String formatVND(Integer amount) {
        if (amount == null) {
            return "0 VNĐ";
        }
        
        return formatVND(amount.doubleValue());
    }
    
    /**
     * Format với ký hiệu đồng (₫)
     * Ví dụ: 1234567 -> "1.234.567₫"
     * 
     * @param amount Số tiền cần format
     * @return Chuỗi định dạng tiền tệ với ký hiệu
     */
    public static String formatVNDWithSymbol(BigDecimal amount) {
        if (amount == null) {
            return "0" + CURRENCY_SYMBOL;
        }
        
        long longAmount = amount.longValue();
        NumberFormat numberFormat = NumberFormat.getInstance(VIETNAM_LOCALE);
        numberFormat.setMaximumFractionDigits(0);
        numberFormat.setGroupingUsed(true);
        
        String formatted = numberFormat.format(longAmount);
        
        return formatted + CURRENCY_SYMBOL;
    }
    
    /**
     * Format với hai chữ số thập phân (để cho chi tiết thanh toán)
     * Ví dụ: 1234567.50 -> "1.234.567,50 VNĐ"
     * 
     * @param amount Số tiền cần format
     * @return Chuỗi định dạng tiền tệ
     */
    public static String formatVNDWithDecimal(BigDecimal amount) {
        if (amount == null) {
            return "0,00 VNĐ";
        }
        
        NumberFormat numberFormat = NumberFormat.getInstance(VIETNAM_LOCALE);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setGroupingUsed(true);
        
        String formatted = numberFormat.format(amount);
        
        return formatted + " VNĐ";
    }
    
    /**
     * Format cho hiển thị ngắn gọn (Triệu đồng)
     * Ví dụ: 1234567 -> "1.23M"
     * 
     * @param amount Số tiền cần format
     * @return Chuỗi định dạng rút gọn
     */
    public static String formatVNDShort(BigDecimal amount) {
        if (amount == null) {
            return "0M";
        }
        
        long value = amount.longValue();
        
        if (value >= 1_000_000) {
            return "%.2f M".formatted(value / 1_000_000.0);
        } else if (value >= 1_000) {
            return "%.2f K".formatted(value / 1_000.0);
        } else {
            return value + "";
        }
    }
    
    /**
     * Chuyển đổi giá trị tiền thành mệnh giá (Triệu đồng)
     * Ví dụ: 1500000 -> 1.5
     * 
     * @param amount Số tiền
     * @return Giá trị tính bằng triệu đồng
     */
    public static Double toMillions(BigDecimal amount) {
        if (amount == null) {
            return 0.0;
        }
        
        return amount.doubleValue() / 1_000_000;
    }
}
