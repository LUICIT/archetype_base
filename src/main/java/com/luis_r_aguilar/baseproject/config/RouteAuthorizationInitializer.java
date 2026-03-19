package com.luis_r_aguilar.baseproject.config;

import com.luisraguilar.luisprojectscore.config.RouteAuthorizationConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration("baseProjectRouteAuthorizationInitializer")
public class RouteAuthorizationInitializer {

    @Bean("baseProjectRouteAuthorizationConfig")
    @Primary
    public RouteAuthorizationConfig routeAuthorizationConfig() {
        return new RouteAuthorizationConfig()
                .withToken("/users/**");
    }
}