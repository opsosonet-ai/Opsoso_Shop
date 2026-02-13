package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor để tự động thêm FormatUtility vào tất cả Model
 * Giúp sử dụng #format trong tất cả templates
 */
@Component
public class FormatInterceptor implements HandlerInterceptor {
    
    @Autowired
    private FormatUtility formatUtility;
    
    @Override
    public void postHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            @Nullable ModelAndView modelAndView) throws Exception {
        
        // Thêm formatUtility vào tất cả model
        if (modelAndView != null) {
            modelAndView.addObject("format", formatUtility);
        }
    }
}
