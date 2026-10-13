package com.sky.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;

@Validated
@ConfigurationProperties("sky.jwt")
public record JwtProperties(@NotBlank String secret, @NotNull Duration ttl) {
    @Override public String toString() { return "JwtProperties[secret=[REDACTED], ttl="+ttl+"]"; }
}
