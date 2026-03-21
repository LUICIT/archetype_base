#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.config;

import io.github.luicit.luisprojectscore.config.RouteAuthorizationConfig;
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