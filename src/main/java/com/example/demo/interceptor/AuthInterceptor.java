package com.example.demo.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) 
            throws Exception {
        
        String uri = request.getRequestURI();
        
        // Danh sách các URL không cần kiểm tra đăng nhập
        if (isPublicUrl(uri)) {
            return true;
        }
        
        // Check authentication from SecurityContextHolder (Spring Security 6.1+)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() 
            && !authentication.getName().equals("anonymousUser");
        
        // Nếu chưa đăng nhập thì chuyển về trang login
        if (!isAuthenticated) {
            // Lưu URL hiện tại để redirect sau khi đăng nhập
            HttpSession session = request.getSession();
            session.setAttribute("redirectUrl", uri);
            response.sendRedirect("/auth/login");
            return false;
        }
        
        return true;
    }
    
    /**
     * Kiểm tra xem URL có cần kiểm tra đăng nhập hay không
     */
    private boolean isPublicUrl(String uri) {
        // Các URL không cần đăng nhập
        return uri.equals("/") ||
               uri.equals("/auth/login") ||
               uri.equals("/auth/register") ||
               uri.equals("/auth/logout") ||
               uri.startsWith("/settings") || // Cho phép truy cập settings khi DB unavailable
               uri.startsWith("/css/") ||
               uri.startsWith("/js/") ||
               uri.startsWith("/images/") ||
               uri.startsWith("/static/") ||
               uri.startsWith("/webjars/") ||
               uri.equals("/favicon.ico") ||
               uri.equals("/error");
    }
}