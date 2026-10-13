package com.sky.config;

import com.sky.utils.JwtUtil;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {
    @Bean Clock clock() { return Clock.system(ZoneId.of("Asia/Shanghai")); }
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean JwtUtil jwtUtil(JwtProperties properties, Clock clock) {
        return new JwtUtil(properties.secret(), properties.ttl(), clock);
    }
}
