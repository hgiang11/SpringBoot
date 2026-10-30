package com.thg.chickenshop.dto.stats;

import com.thg.chickenshop.model.PaymentMethod;
import java.math.BigDecimal;

public record PaymentRow(PaymentMethod method, Long orders, BigDecimal revenue) {}