package com.luis_r_aguilar.baseproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteAuthorizationInitializer {

    @Bean
    public RouteAuthorizationConfig routeAuthorizationConfig() {
        return new RouteAuthorizationConfig()
                .withToken("/users/**")
                .withoutToken("/prices/**");
    }
}