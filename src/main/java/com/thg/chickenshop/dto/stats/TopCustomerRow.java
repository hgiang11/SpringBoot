package com.thg.chickenshop.dto.stats;

import java.math.BigDecimal;

public record TopCustomerRow(Long userId, String fullName, String phone,
                             Long orders, BigDecimal spent) {}