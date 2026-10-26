package com.thg.chickenshop.model;

import java.util.List;

public enum OrderStatus {
    PENDING("Chờ xác nhận"),
    CONFIRMED("Đã xác nhận"),
    PREPARING("Đang chuẩn bị"),
    DELIVERING("Đang giao"),
    COMPLETED("Hoàn thành"),
    CANCELLED("Đã hủy");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Các trạng thái được phép chuyển tới từ trạng thái hiện tại. */
    public List<OrderStatus> nextStatuses() {
        return switch (this) {
            case PENDING -> List.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> List.of(PREPARING);
            case PREPARING -> List.of(DELIVERING);
            case DELIVERING -> List.of(COMPLETED);
            case COMPLETED, CANCELLED -> List.of();
        };
    }

    public boolean canChangeTo(OrderStatus next) {
        return nextStatuses().contains(next);
    }
}