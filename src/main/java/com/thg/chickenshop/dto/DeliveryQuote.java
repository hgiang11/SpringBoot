package com.thg.chickenshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DeliveryQuote(double distanceKm, BigDecimal shippingFee,
                            int totalMinutes, LocalDateTime eta) {}