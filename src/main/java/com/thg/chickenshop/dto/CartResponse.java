package com.thg.chickenshop.dto;

public record CartResponse(
        String message,
        int cartCount,
        String cartTotal,
        String itemSubtotal) {
}