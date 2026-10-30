package com.thg.chickenshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {

    // Đường dẫn /admin/** đã được SecurityConfig giới hạn cho ROLE_ADMIN
    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }
}