package com.thg.chickenshop.controller;

import com.thg.chickenshop.dto.AddItemRequest;
import com.thg.chickenshop.dto.CartResponse;
import com.thg.chickenshop.dto.UpdateItemRequest;
import com.thg.chickenshop.model.User;
import com.thg.chickenshop.service.CartService;
import com.thg.chickenshop.service.CurrentUserService;
import com.thg.chickenshop.service.MessageService;
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
    private final MessageService messageService;

    public CartRestController(CartService cartService,
                              CurrentUserService currentUserService,
                              MessageService messageService) {
        this.cartService = cartService;
        this.currentUserService = currentUserService;
        this.messageService = messageService;
    }

    @PostMapping("/add")
    public CartResponse add(@Valid @RequestBody AddItemRequest request) {
        User user = currentUserService.getCurrentUser();
        cartService.addItem(user, request.productId(), request.quantity());
        return summary(user, "cart.added", null);
    }

    @PutMapping("/items/{id}")
    public CartResponse update(@PathVariable Long id, @Valid @RequestBody UpdateItemRequest request) {
        User user = currentUserService.getCurrentUser();
        cartService.updateQuantity(user, id, request.quantity());
        return summary(user, "cart.updated", money(cartService.getItemSubtotal(user, id)));
    }

    @DeleteMapping("/items/{id}")
    public CartResponse remove(@PathVariable Long id) {
        User user = currentUserService.getCurrentUser();
        cartService.removeItem(user, id);
        return summary(user, "cart.removed", null);
    }

    private CartResponse summary(User user, String messageKey, String itemSubtotal) {
        int count = cartService.countItems(user);
        return new CartResponse(
                messageService.get(messageKey),
                count,
                messageService.get("cart.itemCount", count),
                money(cartService.getTotal(user)),
                itemSubtotal);
    }

    private static String money(BigDecimal value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", symbols).format(value) + " ₫";
    }
}