package com.playtomic.tests.wallet.configuration.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private static final long EXPIRATION_TIME = 1000 * 60 * 60;
    private static final String PASSWORD = "password";
    private static final String SECRET_KEY = "C2x5A8B4F0E2D6G3H1J4K7L9M1N3P5R8S0T2U4V6W8Y1Z3A5B7D9G2J4L6N8R0";
    private static final String USERNAME = "testUser";

    private JwtTokenProvider jwtTokenProvider;
    private String token;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET_KEY, EXPIRATION_TIME);
        userDetails = new User(USERNAME, PASSWORD, new ArrayList<>());
        token = jwtTokenProvider.generateToken(userDetails);

    }

    @Test
    void testGenerateToken() {
        String token = jwtTokenProvider.generateToken(userDetails);

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo(USERNAME);
    }

    @Test
    void testValidateToken() {
        boolean isValid = jwtTokenProvider.validateToken(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    void testValidateExpiredToken() {
        jwtTokenProvider = new JwtTokenProvider(SECRET_KEY, 1);
        String token = jwtTokenProvider.generateToken(userDetails);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean isValid = jwtTokenProvider.validateToken(token, userDetails);

        assertThat(isValid).isFalse();
    }

    @Test
    void testGetUsernameFromToken() {
        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertThat(username).isEqualTo(USERNAME);
    }

}
