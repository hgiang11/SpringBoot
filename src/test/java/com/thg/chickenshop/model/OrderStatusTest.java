package com.thg.chickenshop.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.thg.chickenshop.model.OrderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Test
    @DisplayName("Đơn chờ xác nhận chỉ được xác nhận hoặc hủy")
    void pending_canOnlyGoToConfirmedOrCancelled() {
        assertThat(PENDING.nextStatuses()).containsExactly(CONFIRMED, CANCELLED);
    }

    @Test
    @DisplayName("Đi đúng thứ tự từ xác nhận đến hoàn thành")
    void happyPath_followsTheExpectedOrder() {
        assertThat(CONFIRMED.canChangeTo(PREPARING)).isTrue();
        assertThat(PREPARING.canChangeTo(DELIVERING)).isTrue();
        assertThat(DELIVERING.canChangeTo(COMPLETED)).isTrue();
    }

    @Test
    @DisplayName("Trạng thái cuối không chuyển được nữa")
    void finalStatuses_haveNoNextStatus() {
        assertThat(COMPLETED.nextStatuses()).isEmpty();
        assertThat(CANCELLED.nextStatuses()).isEmpty();
    }

    @ParameterizedTest(name = "{0} -> {1} bị từ chối")
    @CsvSource({
            "PENDING,COMPLETED",
            "CONFIRMED,DELIVERING",
            "PREPARING,COMPLETED",
            "DELIVERING,CANCELLED",
            "COMPLETED,PENDING",
            "CANCELLED,PENDING"
    })
    @DisplayName("Chuyển trạng thái sai thứ tự bị từ chối")
    void invalidTransitions_areRejected(OrderStatus from, OrderStatus to) {
        assertThat(from.canChangeTo(to)).isFalse();
    }
}
