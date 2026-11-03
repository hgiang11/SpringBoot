package com.thg.chickenshop.dto;

import com.thg.chickenshop.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;

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

    @NotBlank(message = "{validation.receiverName.required}")
    @Size(max = 100, message = "{validation.receiverName.size}")
    private String receiverName;

    @NotNull(message = "{validation.payment.required}")
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    private Double deliveryLat;
    private Double deliveryLng;
}