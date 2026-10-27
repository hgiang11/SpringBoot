package com.thg.chickenshop.controller;

import com.thg.chickenshop.dto.AddItemRequest;
import com.thg.chickenshop.dto.CartResponse;
import com.thg.chickenshop.dto.UpdateItemRequest;
import com.thg.chickenshop.model.User;
import com.thg.chickenshop.service.CartService;
import com.thg.chickenshop.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    private final CartService cartService;
    private final CurrentUserService currentUserService;

    public CartRestController(CartService cartService, CurrentUserService currentUserService) {
        this.cartService = cartService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/add")
    public CartResponse add(@Valid @RequestBody AddItemRequest request) {
        User user = currentUserService.getCurrentUser();
        cartService.addItem(user, request.productId(), request.quantity());
        return summary(user, "Đã thêm món vào giỏ hàng", null);
    }

    @PutMapping("/items/{id}")
    public CartResponse update(@PathVariable Long id, @Valid @RequestBody UpdateItemRequest request) {
        User user = currentUserService.getCurrentUser();
        cartService.updateQuantity(user, id, request.quantity());
        return summary(user, "Đã cập nhật số lượng",
                money(cartService.getItemSubtotal(user, id)));
    }

    @DeleteMapping("/items/{id}")
    public CartResponse remove(@PathVariable Long id) {
        User user = currentUserService.getCurrentUser();
        cartService.removeItem(user, id);
        return summary(user, "Đã xóa món khỏi giỏ hàng", null);
    }

    private CartResponse summary(User user, String message, String itemSubtotal) {
        return new CartResponse(message,
                cartService.countItems(user),
                money(cartService.getTotal(user)),
                itemSubtotal);
    }

    // Định dạng tiền ở server (35.000 ₫) để JavaScript không phải tự xử lý
    private static String money(BigDecimal value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", symbols).format(value) + " ₫";
    }
}