package com.luis_r_aguilar.baseproject.validation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EmailValidationModel {

    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    private final String email;

    public EmailValidationModel(String email) {
        this.email = email;
    }

}
