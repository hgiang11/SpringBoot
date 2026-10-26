package com.thg.chickenshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JollibeeController {

    @GetMapping("/jollibee")
    public String jollibee(Model model) {
        model.addAttribute("active", "jollibee");   // để header tô sáng mục GIỚI THIỆU
        return "jollibee";                          // mở templates/jollibee.html
    }
}