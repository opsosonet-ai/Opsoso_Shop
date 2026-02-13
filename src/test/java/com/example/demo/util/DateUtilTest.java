package com.example.demo.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class DateUtilTest {
    
    @Test
    public void testCurrentDate() {
        LocalDate date = LocalDate.now();
        assertNotNull(date);
    }
    
    @Test
    public void testCurrentDateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        assertNotNull(dateTime);
    }
    
    @Test
    public void testDateFormat() {
        LocalDate date = LocalDate.of(2025, 11, 1);
        assertNotNull(date.toString());
        assertTrue(date.toString().contains("2025"));
    }
}
