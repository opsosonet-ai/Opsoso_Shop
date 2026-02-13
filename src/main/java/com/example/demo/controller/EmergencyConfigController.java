package com.example.demo.controller;

import com.example.demo.service.ApplicationPropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/emergency")
public class EmergencyConfigController {
    
    @Autowired
    private ApplicationPropertiesService applicationPropertiesService;
    
    @GetMapping("/setup")
    public String showEmergencySetup(Model model) {
        model.addAttribute("title", "🚨 Emergency Database Configuration");
        model.addAttribute("message", "Configure database connection when normal startup fails");
        
        // Detect OS
        String os = System.getProperty("os.name").toLowerCase();
        model.addAttribute("isWindows", os.contains("windows"));
        model.addAttribute("isLinux", os.contains("linux"));
        model.addAttribute("osName", os);
        
        return "emergency/setup";
    }
    
    @PostMapping("/configure")
    public String configureDatabase(@RequestParam String host,
                                  @RequestParam int port,
                                  @RequestParam String database,
                                  @RequestParam String username,
                                  @RequestParam String password,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Update application.properties
            applicationPropertiesService.updateDatabaseConfiguration(host, port, database, username, password);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "✅ Database configuration updated successfully! Please restart the application.");
            redirectAttributes.addFlashAttribute("restartRequired", true);
            
            return "redirect:/emergency/setup?success=true";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Failed to update configuration: " + e.getMessage());
            return "redirect:/emergency/setup?error=true";
        }
    }
    
    @GetMapping("/restart-guide")
    public String showRestartGuide(Model model) {
        String os = System.getProperty("os.name").toLowerCase();
        model.addAttribute("isWindows", os.contains("windows"));
        model.addAttribute("isLinux", os.contains("linux"));
        return "emergency/restart-guide";
    }
}