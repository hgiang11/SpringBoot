package com.thg.chickenshop.dto.stats;

import java.math.BigDecimal;

public record StatsSummary(BigDecimal revenue, long totalOrders, long completedOrders,
                           long cancelledOrders, double cancellationRate,
                           BigDecimal averageOrderValue) {}