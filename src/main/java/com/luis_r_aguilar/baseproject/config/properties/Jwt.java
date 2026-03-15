package com.luis_r_aguilar.baseproject.config.properties;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Jwt {

    private String secret;
    private long expiration;

}
