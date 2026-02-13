package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler to prevent "getOutputStream() already called" errors
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle IllegalStateException - "getOutputStream() has already been called"
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(
            IllegalStateException e,
            HttpServletResponse response) {
        
        if (e.getMessage() != null && e.getMessage().contains("getOutputStream")) {
            log.warn("⚠️  Response stream already written: {}", e.getMessage());
            log.debug("Full stack trace:", e);
            
            // Check if response is already committed
            if (response.isCommitted()) {
                // Response already sent, can't do anything
                log.debug("Response already committed, cannot send error response");
                return null;
            }
        }
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "Lỗi xử lý");
        error.put("message", "Đã xảy ra lỗi khi xử lý yêu cầu của bạn. Vui lòng thử lại.");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handle IOException (connection reset, broken pipe, etc.)
     */
    @ExceptionHandler(IOException.class)
    public void handleIOException(IOException e) {
        String className = e.getClass().getSimpleName();
        if ("ClientAbortException".equals(className) ||
            (e.getMessage() != null && (e.getMessage().contains("Connection reset") ||
                                       e.getMessage().contains("Broken pipe") ||
                                       e.getMessage().contains("EPIPE")))) {
            log.debug("Client connection closed (expected): {} - {}", className, e.getMessage());
        } else {
            log.warn("Unexpected IOException: {} - {}", className, e.getMessage());
            log.debug("Full stack trace:", e);
        }
    }

    /**
     * Handle RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        log.error("Unexpected RuntimeException:", e);
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "Lỗi hệ thống");
        error.put("message", "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại hoặc liên hệ quản trị viên.");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handle general Exception
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception e) {
        log.error("Unexpected Exception:", e);
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "Lỗi hệ thống");
        error.put("message", e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi không xác định");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
