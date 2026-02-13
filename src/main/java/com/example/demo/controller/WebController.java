package com.example.demo.controller;

import com.example.demo.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class WebController extends BaseController {

    // Trang chủ - kiểm tra đăng nhập trước
    @GetMapping("/")
    public String home(HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        return "redirect:/dashboard";
    }
}