package com.thg.chickenshop.dto.stats;

import java.math.BigDecimal;

public record TopProductRow(Long productId, String name, String imageUrl,
                            Long quantity, BigDecimal revenue) {}