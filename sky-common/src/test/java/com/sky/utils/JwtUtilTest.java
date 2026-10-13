package com.sky.utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    private static final String SECRET = Base64.getEncoder().encodeToString(new byte[32]);
    private static final Clock NOW = Clock.fixed(Instant.parse("2026-10-13T00:00:00Z"), ZoneOffset.UTC);

    @Test void signedTokenRetainsEmployeeIdentity() {
        JwtUtil jwt = new JwtUtil(SECRET, Duration.ofHours(2), NOW);
        assertEquals(42L, jwt.employeeId(jwt.createToken(42L)));
    }
    @Test void expiredTokenIsRejected() {
        String token = new JwtUtil(SECRET, Duration.ofHours(2), NOW).createToken(42L);
        JwtUtil later = new JwtUtil(SECRET, Duration.ofHours(2), Clock.offset(NOW, Duration.ofHours(3)));
        assertThrows(ExpiredJwtException.class, () -> later.employeeId(token));
    }
    @Test void wrongSignatureIsRejected() {
        String token = new JwtUtil(SECRET, Duration.ofHours(2), NOW).createToken(42L);
        byte[] different = new byte[32]; different[0] = 1;
        JwtUtil other = new JwtUtil(Base64.getEncoder().encodeToString(different), Duration.ofHours(2), NOW);
        assertThrows(JwtException.class, () -> other.employeeId(token));
    }
    @Test void malformedTokenIsRejected() {
        assertThrows(JwtException.class, () -> new JwtUtil(SECRET, Duration.ofHours(2), NOW).employeeId("not-a-token"));
    }
    @Test void weakKeyAndInvalidTtlAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil("YWJj", Duration.ofHours(2), NOW));
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil(SECRET, Duration.ZERO, NOW));
    }
}
