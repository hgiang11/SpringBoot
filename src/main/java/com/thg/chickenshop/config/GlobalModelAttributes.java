package com.thg.chickenshop.config;

import com.thg.chickenshop.model.User;
import com.thg.chickenshop.service.CurrentUserService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final CurrentUserService currentUserService;

    public GlobalModelAttributes(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @ModelAttribute("currentUser")
    public User currentUser() {
        return currentUserService.findCurrentUser().orElse(null);
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        return currentUserService.isAdmin();
    }
}