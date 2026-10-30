package com.thg.chickenshop.dto.stats;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DayRevenue(LocalDate date, BigDecimal revenue) {}