package com.luis_r_aguilar.baseproject.config;

import com.luis_r_aguilar.baseproject.security.JwtFilter;
import com.luis_r_aguilar.baseproject.security.LoginRateLimitFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfiguration.class);

    private final LoginRateLimitFilter loginRateLimitFilter;
    private final RouteAuthorizationConfig routeAuthorizationConfig;
    private final JwtFilter jwtFilter;
    private final AppProperties appProperties;

    public SecurityConfiguration(
            LoginRateLimitFilter loginRateLimitFilter, RouteAuthorizationConfig routeAuthorizationConfig,
            JwtFilter jwtFilter,
            AppProperties appProperties
    ) {
        this.loginRateLimitFilter = loginRateLimitFilter;
        this.routeAuthorizationConfig = routeAuthorizationConfig;
        this.jwtFilter = jwtFilter;
        this.appProperties = appProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            LOGGER.debug("AuthenticationEntryPoint -> 401 for path={}", req.getRequestURI());
                            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            LOGGER.debug("AccessDeniedHandler -> 403 for path={}, user={}", req.getRequestURI(), req.getRemoteUser());
                            res.sendError(HttpServletResponse.SC_FORBIDDEN);
                        })
                );

        if (!appProperties.getSecurity().isLoginEnabled()) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(
                    "/auth/**",
                    "/actuator/health",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
            ).permitAll();

            if (routeAuthorizationConfig.hasWithoutTokenRoutes()) {
                auth.requestMatchers(routeAuthorizationConfig.getWithoutTokenRoutes().toArray(new String[0]))
                        .permitAll();
            }

            if (routeAuthorizationConfig.hasWithTokenRoutes()) {
                auth.requestMatchers(routeAuthorizationConfig.getWithTokenRoutes().toArray(new String[0]))
                        .authenticated();
            }

            auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
            auth.anyRequest().permitAll();
        });

        http.addFilterBefore(loginRateLimitFilter, SecurityContextHolderFilter.class);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}