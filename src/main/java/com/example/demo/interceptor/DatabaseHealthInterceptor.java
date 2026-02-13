package com.example.demo.interceptor;

import com.example.demo.service.DatabaseHealthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class DatabaseHealthInterceptor implements HandlerInterceptor {
    
    @Autowired
    private DatabaseHealthService databaseHealthService;
    
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) 
            throws Exception {
        
        String uri = request.getRequestURI();
        
        // Cho phép truy cập các URL cơ bản và settings
        if (isPublicUrl(uri) || uri.startsWith("/settings")) {
            return true;
        }
        
        // Nếu database không khả dụng, redirect về settings
        if (!databaseHealthService.isDatabaseAvailable()) {
            response.sendRedirect("/settings?error=database_unavailable");
            return false;
        }
        
        return true;
    }
    
    private boolean isPublicUrl(String uri) {
        return uri.equals("/") ||
               uri.startsWith("/css/") ||
               uri.startsWith("/js/") ||
               uri.startsWith("/images/") ||
               uri.startsWith("/static/") ||
               uri.startsWith("/webjars/") ||
               uri.equals("/favicon.ico") ||
               uri.equals("/error");
    }
}
