package com.thg.chickenshop.controller;

import com.thg.chickenshop.exception.BusinessException;
import com.thg.chickenshop.exception.NotFoundException;
import com.thg.chickenshop.model.User;
import com.thg.chickenshop.service.CartService;
import com.thg.chickenshop.service.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final CurrentUserService currentUserService;

    public CartController(CartService cartService, CurrentUserService currentUserService) {
        this.cartService = cartService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String view(Model model) {
        User user = currentUserService.getCurrentUser();
        model.addAttribute("cart", cartService.getOrCreateCart(user));
        model.addAttribute("active", "cart");
        return "cart";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long productId,
                      @RequestParam(defaultValue = "1") int quantity,
                      @RequestParam(required = false) String q,
                      @RequestParam(required = false) Long category,
                      @RequestParam(required = false) Integer page,
                      RedirectAttributes ra) {
        try {
            cartService.addItem(currentUserService.getCurrentUser(), productId, quantity);
            ra.addFlashAttribute("message", "Đã thêm món vào giỏ hàng");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        // Quay lại đúng trang menu người dùng đang xem (giữ bộ lọc, từ khóa, số trang)
        if (q != null && !q.isBlank()) ra.addAttribute("q", q);
        if (category != null) ra.addAttribute("category", category);
        if (page != null) ra.addAttribute("page", page);
        return "redirect:/menu";
    }

    @PostMapping("/items/{id}/update")
    public String update(@PathVariable Long id,
                         @RequestParam int quantity,
                         RedirectAttributes ra) {
        try {
            cartService.updateQuantity(currentUserService.getCurrentUser(), id, quantity);
            ra.addFlashAttribute("message", "Đã cập nhật số lượng");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/items/{id}/remove")
    public String remove(@PathVariable Long id, RedirectAttributes ra) {
        cartService.removeItem(currentUserService.getCurrentUser(), id);
        ra.addFlashAttribute("message", "Đã xóa món khỏi giỏ hàng");
        return "redirect:/cart";
    }


    @PostMapping("/api/items/{id}/update")
    @ResponseBody
    public ResponseEntity<?> apiUpdate(@PathVariable Long id, @RequestParam int quantity) {
        try {
            CartService.CartUpdateResult r = cartService.updateQuantity(currentUserService.getCurrentUser(), id, quantity);
            return ResponseEntity.ok(r);
        } catch (BusinessException | NotFoundException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/api/items/{id}/remove")
    @ResponseBody
    public ResponseEntity<?> apiRemove(@PathVariable Long id) {
        try {
            CartService.CartUpdateResult r = cartService.removeItem(currentUserService.getCurrentUser(), id);
            return ResponseEntity.ok(r);
        } catch (BusinessException | NotFoundException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}