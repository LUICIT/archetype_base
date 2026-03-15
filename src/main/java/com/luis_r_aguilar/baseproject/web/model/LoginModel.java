package com.luis_r_aguilar.baseproject.web.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginModel {

    @NotBlank
    @Size(min = 10, max = 120)
    private String username;

    @NotBlank
    private String password;

}
