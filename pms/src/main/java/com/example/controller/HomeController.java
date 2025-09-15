package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class HomeController {
 
    @GetMapping("/") // Main page - no conflict now
    public String home(Model model) {
        model.addAttribute("title", "Vehicle Parking Management System");
        return "index";
    }
 
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About - Parking Management");
        return "about"; // You might want separate templates
    }
 
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("title", "Contact - Parking Management");
        return "contact"; // You might want separate templates
    }
}
