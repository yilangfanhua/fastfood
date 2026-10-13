package com.sky.utils;

import java.time.Clock;
import java.time.Duration;
import java.util.Date;
import java.util.Base64;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {
    private final SecretKey key;
    private final Duration ttl;
    private final Clock clock;

    public JwtUtil(String secret, Duration ttl, Clock clock) {
        byte[] bytes = Base64.getDecoder().decode(secret);
        if (bytes.length != 32) throw new IllegalArgumentException("JWT_SECRET must encode exactly 32 bytes");
        if (ttl == null || ttl.isZero() || ttl.isNegative()) throw new IllegalArgumentException("JWT TTL must be positive");
        this.key = Keys.hmacShaKeyFor(bytes);
        this.ttl = ttl;
        this.clock = clock;
    }
    public String createToken(Long employeeId) {
        if (employeeId == null || employeeId <= 0) throw new IllegalArgumentException("Invalid employee ID");
        return Jwts.builder().issuer("sky-admin").subject(employeeId.toString())
                .issuedAt(Date.from(clock.instant())).expiration(Date.from(clock.instant().plus(ttl)))
                .signWith(key, Jwts.SIG.HS256).compact();
    }
    public Long employeeId(String token) {
        var signed = Jwts.parser().verifyWith(key).requireIssuer("sky-admin")
                .clock(() -> Date.from(clock.instant())).build().parseSignedClaims(token);
        if (!"HS256".equals(signed.getHeader().getAlgorithm()) || signed.getPayload().getExpiration() == null) {
            throw new IllegalArgumentException("Invalid token claims");
        }
        long id = Long.parseLong(signed.getPayload().getSubject());
        if (id <= 0) throw new IllegalArgumentException("Invalid employee ID");
        return id;
    }
}
