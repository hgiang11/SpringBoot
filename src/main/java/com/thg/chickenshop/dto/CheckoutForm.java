package com.thg.chickenshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CheckoutForm {

    @NotBlank(message = "{validation.address.required}")
    @Size(max = 255, message = "{validation.address.size}")
    private String deliveryAddress;

    @NotBlank(message = "{validation.phone.required}")
    @Pattern(regexp = "^(0|\\+84)\\d{9}$", message = "{validation.phone.pattern}")
    private String phone;
}