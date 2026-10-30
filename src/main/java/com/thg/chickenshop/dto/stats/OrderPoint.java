package com.thg.chickenshop.dto.stats;

import com.thg.chickenshop.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderPoint(LocalDateTime orderDate, BigDecimal totalPrice, OrderStatus status) {}