package com.example.demo.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * DISABLED: Custom error controller causing "getOutputStream() already called" errors
 * Spring's default error handling is more robust
 */
@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Try to get error details, but don't fail if they're not available
        try {
            Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
            Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
            String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
            
            model.addAttribute("status", status != null ? status : "Unknown");
            model.addAttribute("error", message != null ? message : "An error occurred");
            model.addAttribute("exception", exception);
            model.addAttribute("path", path);
            
            // Get status code safely
            if (status != null) {
                try {
                    Integer statusCode = Integer.valueOf(status.toString());
                    
                    if (statusCode == HttpStatus.NOT_FOUND.value()) {
                        return "error/404";
                    } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                        return "error/403";
                    } else if (statusCode == HttpStatus.BAD_REQUEST.value()) {
                        return "error/400";
                    } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                        return "error/500";
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        } catch (Exception e) {
            // If anything fails, just return basic error page
            model.addAttribute("error", "An error occurred processing your request");
        }
        
        return "error/error";
    }
}