package com.thg.chickenshop.controller;

import com.thg.chickenshop.dto.CheckoutForm;
import com.thg.chickenshop.exception.BusinessException;
import com.thg.chickenshop.model.Cart;
import com.thg.chickenshop.model.Order;
import com.thg.chickenshop.model.User;
import com.thg.chickenshop.service.CartService;
import com.thg.chickenshop.service.CurrentUserService;
import com.thg.chickenshop.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final CurrentUserService currentUserService;

    public OrderController(OrderService orderService,
                           CartService cartService,
                           CurrentUserService currentUserService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/checkout")
    public String checkout(Model model, RedirectAttributes ra) {
        User user = currentUserService.getCurrentUser();
        Cart cart = cartService.getOrCreateCart(user);
        if (cart.getItems().isEmpty()) {
            ra.addFlashAttribute("error", "Giỏ hàng trống, hãy chọn món trước");
            return "redirect:/cart";
        }

        CheckoutForm form = new CheckoutForm();
        form.setDeliveryAddress(user.getAddress());   // điền sẵn từ hồ sơ, khách có thể sửa
        form.setPhone(user.getPhone());

        model.addAttribute("checkoutForm", form);
        model.addAttribute("cart", cart);
        model.addAttribute("active", "cart");
        return "checkout";
    }

    @PostMapping("/orders")
    public String placeOrder(@Valid @ModelAttribute("checkoutForm") CheckoutForm form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes ra) {
        User user = currentUserService.getCurrentUser();

        if (result.hasErrors()) {
            model.addAttribute("cart", cartService.getOrCreateCart(user));
            model.addAttribute("active", "cart");
            return "checkout";
        }

        try {
            Order order = orderService.placeOrder(user, form);
            ra.addFlashAttribute("message", "Đặt hàng thành công! Mã đơn #" + order.getId());
            return "redirect:/orders/" + order.getId();
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/orders")
    public String myOrders(Model model) {
        User user = currentUserService.getCurrentUser();
        model.addAttribute("orders", orderService.findByUser(user));
        model.addAttribute("active", "orders");
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String detail(@PathVariable Long id, Model model) {
        User user = currentUserService.getCurrentUser();
        model.addAttribute("order", orderService.findByIdForUser(id, user));
        model.addAttribute("active", "orders");
        return "order-detail";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        try {
            orderService.cancel(id, currentUserService.getCurrentUser());
            ra.addFlashAttribute("message", "Đã hủy đơn hàng #" + id);
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }
}