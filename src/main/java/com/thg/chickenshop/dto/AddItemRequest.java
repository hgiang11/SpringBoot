package com.thg.chickenshop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddItemRequest(
        @NotNull Long productId,
        @NotNull @Min(1) @Max(20) Integer quantity) {
}