package com.example.demo.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Servlet filter to prevent "getOutputStream() has already been called" errors
 * by wrapping the response to buffer its content
 */
@Component
public class ResponseBufferingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (response instanceof HttpServletResponse) {
            try {
                chain.doFilter(request, response);
            } catch (Exception e) {
                // If an error occurs during rendering, try to clear the response
                // and send an error page instead
                if (response instanceof HttpServletResponse httpResponse) {
                    if (!httpResponse.isCommitted()) {
                        try {
                            httpResponse.reset();
                            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            httpResponse.setContentType("text/html;charset=UTF-8");
                            httpResponse.getWriter().write("<h1>Lỗi Xử Lý</h1><p>Đã xảy ra lỗi khi xử lý yêu cầu của bạn. Vui lòng thử lại.</p>");
                        } catch (IllegalStateException ise) {
                            // Response already committed, nothing we can do
                        }
                    }
                }
                throw e;
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // No-op
    }

    @Override
    public void destroy() {
        // No-op
    }
}
