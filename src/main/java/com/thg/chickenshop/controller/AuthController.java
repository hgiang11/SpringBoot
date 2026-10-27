package com.thg.chickenshop.controller;

import com.thg.chickenshop.dto.RegisterForm;
import com.thg.chickenshop.exception.BusinessException;
import com.thg.chickenshop.service.CurrentUserService;
import com.thg.chickenshop.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final CurrentUserService currentUserService;

    public AuthController(UserService userService, CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/login")
    public String login() {
        if (currentUserService.findCurrentUser().isPresent()) {
            return "redirect:/menu";       // đã đăng nhập thì không cần vào trang đăng nhập
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (currentUserService.findCurrentUser().isPresent()) {
            return "redirect:/menu";
        }
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult result,
                           RedirectAttributes ra) {

        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Mật khẩu nhập lại không khớp");
        }

        if (!result.hasErrors()) {
            try {
                userService.register(form);
                ra.addFlashAttribute("message", "Đăng ký thành công, hãy đăng nhập");
                return "redirect:/login";
            } catch (BusinessException ex) {
                result.rejectValue("username", "duplicate", ex.getMessage());
            }
        }
        return "register";
    }
}