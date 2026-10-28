package com.thg.chickenshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterForm {

    @NotBlank(message = "{validation.username.required}")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,30}$", message = "{validation.username.pattern}")
    private String username;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 6, max = 72, message = "{validation.password.size}")
    private String password;

    @NotBlank(message = "{validation.confirm.required}")
    private String confirmPassword;

    @NotBlank(message = "{validation.fullName.required}")
    @Size(max = 100, message = "{validation.fullName.size}")
    private String fullName;

    @NotBlank(message = "{validation.phone.required}")
    @Pattern(regexp = "^(0|\\+84)\\d{9}$", message = "{validation.phone.pattern}")
    private String phone;

    @Size(max = 255, message = "{validation.address.size}")
    private String address;
}