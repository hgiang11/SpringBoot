package com.thg.chickenshop.model;

public enum PaymentMethod {
    COD("Tiền mặt khi nhận hàng"),
    BANK_QR("Chuyển khoản QR"),
    E_WALLET("Ví điện tử");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}