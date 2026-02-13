package com.example.demo.util;

import java.math.BigDecimal;

public class NumberToWordsUtil {
    
    private static final String[] ones = {
        "", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"
    };
    
    private static final String[] teens = {
        "mười", "mười một", "mười hai", "mười ba", "mười bốn", "mười lăm", 
        "mười sáu", "mười bảy", "mười tám", "mười chín"
    };
    
    private static final String[] scale = {
        "", "nghìn", "triệu", "tỷ", "nghìn tỷ", "triệu tỷ"
    };
    
    public static String convertToWords(BigDecimal number) {
        if (number == null || number.compareTo(BigDecimal.ZERO) == 0) {
            return "Không đồng";
        }
        
        long longValue = number.longValue();
        if (longValue == 0) {
            return "Không đồng";
        }
        
        String result = convertLongToWords(longValue);
        return capitalize(result) + " đồng";
    }
    
    private static String convertLongToWords(long number) {
        if (number == 0) return "";
        
        StringBuilder result = new StringBuilder();
        int scaleIndex = 0;
        
        while (number > 0) {
            int group = (int)(number % 1000);
            if (group > 0) {
                String groupWords = convertHundreds(group);
                if (scaleIndex > 0) {
                    groupWords += " " + scale[scaleIndex];
                }
                if (result.length() > 0) {
                    result.insert(0, groupWords + " ");
                } else {
                    result.insert(0, groupWords);
                }
            }
            number /= 1000;
            scaleIndex++;
        }
        
        return result.toString().trim();
    }
    
    private static String convertHundreds(int number) {
        StringBuilder result = new StringBuilder();
        
        int hundreds = number / 100;
        int remainder = number % 100;
        
        if (hundreds > 0) {
            result.append(ones[hundreds]).append(" trăm");
            if (remainder > 0) {
                result.append(" ");
            }
        }
        
        if (remainder >= 10 && remainder < 20) {
            result.append(teens[remainder - 10]);
        } else {
            int tensDigit = remainder / 10;
            int onesDigit = remainder % 10;
            
            if (tensDigit > 0) {
                if (tensDigit == 1) {
                    result.append("mười");
                } else {
                    result.append(ones[tensDigit]).append(" mười");
                }
                if (onesDigit > 0) {
                    result.append(" ");
                }
            }
            
            if (onesDigit > 0) {
                // Handle special case for "lăm" (5) when it's the ones digit after tens
                if (onesDigit == 5 && tensDigit > 0) {
                    result.append("lăm");
                } else {
                    result.append(ones[onesDigit]);
                }
            }
        }
        
        return result.toString().trim();
    }
    
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}