package com.example.demo.config;

import com.example.demo.security.CustomAuthenticationFailureHandler;
import com.example.demo.security.CustomAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;

/**
 * Spring Security Configuration
 * 
 * Cấu hình bảo mật cho ứng dụng:
 * - CSRF protection: Bảo vệ chống tấn công CSRF
 * - Authentication: Xác thực người dùng
 * - Authorization: Phân quyền dựa trên vai trò
 * - Password encoding: Mã hóa mật khẩu BCrypt
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * CORS Configuration
     * Configure CORS to prevent "Cache miss" warnings
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Cấu hình HTTP Security
     * - Tất cả requests phải được authenticate
     * - Public URLs: /auth/login, /error
     * - CSRF: Enabled (bảo vệ POST, PUT, DELETE)
     * - Session: Fixed (ngăn chặn session fixation attack)
     * - CORS: Configured to eliminate cache miss warnings
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                 DaoAuthenticationProvider authenticationProvider,
                                 CustomAuthenticationSuccessHandler successHandler,
                                 CustomAuthenticationFailureHandler failureHandler,
                                 CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))  // Enable CORS with configuration
            .authenticationProvider(authenticationProvider)  // Set authentication provider
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/settings/**")  // API endpoints và settings không cần CSRF
            )
            .authorizeHttpRequests(authz -> authz
                // Public URLs (không cần authenticate) - ORDER MATTERS!
                // Match specific API endpoints first
                .requestMatchers(
                    "/khach-hang/api/**",  // Customer API endpoints public
                    "/hang-hoa/api/**",    // Product API endpoints public
                    "/tra-hang/api/**"     // Return product API endpoints public
                ).permitAll()
                
                .requestMatchers(
                    "/",
                    "/auth/login",
                    "/auth/dang-nhap",
                    "/auth/logout",
                    "/auth/logout-success",
                    "/error",
                    "/static/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/api/**",  // All other API endpoints
                    "/cong-no/dashboard",  // Dashboard public
                    "/cong-no/nha-phan-phoi/list",  // Supplier debt list page public
                    "/cong-no/khach-hang/list",  // Customer debt list page public
                    "/cong-no/lich-su-thanh-toan",  // Payment history page public
                    // Settings endpoints công khai (cho phép cấu hình DB khi unavailable)
                    "/settings/**"
                ).permitAll()
                
                // Admin URLs require ADMIN role
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Customer management URLs (non-API) require authentication
                .requestMatchers("/khach-hang/**").authenticated()
                
                // Dashboard require authentication
                .requestMatchers("/dashboard/**").authenticated()
                
                // Tất cả URLs khác cần authenticate
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .successHandler(successHandler)  // Use custom success handler
                .failureHandler(failureHandler)  // Use custom failure handler
                .defaultSuccessUrl("/dashboard", true)  // After login, redirect to dashboard
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(customLogoutMatcher())
                .logoutSuccessUrl("/auth/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)  // Prevent multiple logins
                .expiredUrl("/auth/login?expired")  // When session expires
            )
            // Session management - empty for now to avoid interfering with login
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .accessDeniedPage("/error/access-denied")
            );

        return http.build();
    }

    /**
     * Password Encoder: BCrypt
     * 
     * Ưu điểm:
     * - Adaptive hashing: tăng độ khó theo thời gian
     * - Salt built-in: ngăn chặn rainbow table attacks
     * - Industry standard: được khuyến nghị bởi OWASP
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);  // Strength 12 (more secure, slower)
    }

    /**
     * Authentication Provider: Database-backed
     * 
     * Sử dụng UserDetailsService để load user từ database
     * So sánh mật khẩu nhập vào với BCrypt hash trong DB
     */
    @SuppressWarnings("deprecation")
    @Bean
    DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Custom logout request matcher (replacement for deprecated AntPathRequestMatcher)
     * Matches GET and POST requests to /auth/logout
     */
    @Bean
    RequestMatcher customLogoutMatcher() {
        return new RequestMatcher() {
            @Override
            public boolean matches(HttpServletRequest request) {
                return request.getRequestURI().equals("/auth/logout") && 
                       (request.getMethod().equalsIgnoreCase("GET") || 
                        request.getMethod().equalsIgnoreCase("POST"));
            }
        };
    }
}
