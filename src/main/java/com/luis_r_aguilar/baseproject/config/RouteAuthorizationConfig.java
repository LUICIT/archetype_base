package com.luis_r_aguilar.baseproject.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RouteAuthorizationConfig {

    private final List<String> withTokenRoutes = new ArrayList<>();
    private final List<String> withoutTokenRoutes = new ArrayList<>();

    public RouteAuthorizationConfig withToken(String route) {
        if (route != null && !route.isBlank()) {
            this.withTokenRoutes.add(route);
        }
        return this;
    }

    public RouteAuthorizationConfig withoutToken(String route) {
        if (route != null && !route.isBlank()) {
            this.withoutTokenRoutes.add(route);
        }
        return this;
    }

    public List<String> getWithTokenRoutes() {
        return Collections.unmodifiableList(withTokenRoutes);
    }

    public List<String> getWithoutTokenRoutes() {
        return Collections.unmodifiableList(withoutTokenRoutes);
    }

    public boolean hasWithTokenRoutes() {
        return !withTokenRoutes.isEmpty();
    }

    public boolean hasWithoutTokenRoutes() {
        return !withoutTokenRoutes.isEmpty();
    }
}