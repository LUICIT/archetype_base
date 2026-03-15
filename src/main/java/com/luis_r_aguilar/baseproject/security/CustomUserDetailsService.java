package com.luis_r_aguilar.baseproject.security;

import com.luis_r_aguilar.baseproject.config.AppProperties;
import com.luis_r_aguilar.baseproject.domain.entity.BaseUserEntity;
import com.luis_r_aguilar.baseproject.domain.repository.BaseUserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppProperties appProperties;
    private final BaseUserRepository userRepository;

    public CustomUserDetailsService(AppProperties appProperties, BaseUserRepository userRepository) {
        this.appProperties = appProperties;
        this.userRepository = userRepository;
    }
    // carga por email (username en este proyecto es el email)
    @NullMarked
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        BaseUserEntity user;

        if ("username".equalsIgnoreCase(appProperties.getSecurity().getLoginIdentifier())) {
            user = userRepository.findByUsernameAndDeletedAtIsNull(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        } else {
            user = userRepository.findByEmailAndDeletedAtIsNull(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

        return new org.springframework.security.core.userdetails.User(
                identifier,
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                user.getLockedUntil() == null || user.getLockedUntil().isBefore(LocalDateTime.now()),
                Collections.emptyList()
        );
    }
}
