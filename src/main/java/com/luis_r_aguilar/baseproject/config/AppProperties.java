package com.luis_r_aguilar.baseproject.config;

import com.luis_r_aguilar.baseproject.config.properties.Database;
import com.luis_r_aguilar.baseproject.config.properties.Jwt;
import com.luis_r_aguilar.baseproject.config.properties.Security;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Security security = new Security();
    private Database database = new Database();
    private Jwt jwt = new Jwt();

}
