package com.luis_r_aguilar.baseproject.config.properties;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Security {

    private boolean loginEnabled;
    private String loginIdentifier;


}
