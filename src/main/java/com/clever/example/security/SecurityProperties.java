package com.clever.example.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties("app.security")
public record SecurityProperties(Jwt jwt, Admin admin) {

    public record Jwt(String secret,
                      @DefaultValue("1h") Duration expiration,
                      @DefaultValue("crud-example") String issuer) {
    }

    public record Admin(@DefaultValue("admin@example.com") String email, String password) {
    }
}
