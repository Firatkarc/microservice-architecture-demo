package com.firatdeneme.customer_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testSecret = "my-super-secret-key-that-is-at-least-32-bytes-long!";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        // Inject the secret key and expiration into private fields without Spring
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 3600000L);
    }

    @Test
    void shouldGenerateValidJwtToken() {

        String username = "testuser";


        String token = jwtUtil.generateToken(username);


        assertNotNull(token);
        assertFalse(token.isEmpty());

        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes());

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();


        assertEquals(username, claims.getSubject());
        assertNotNull(claims.getExpiration());
    }
}