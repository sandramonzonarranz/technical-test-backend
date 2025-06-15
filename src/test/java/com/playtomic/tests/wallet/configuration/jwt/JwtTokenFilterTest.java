package com.playtomic.tests.wallet.configuration.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenFilterTest {

    private static final String VALID_TOKEN = "valid-token";
    private static final String INVALID_TOKEN = "invalid-token";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String BASIC_AUTH_HEADER = "Basic dXNlcjpwYXNzd29yZA==";
    public static final String AUTHORIZATION = "Authorization";

    @Mock
    private FilterChain filterChain;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @InjectMocks
    private JwtTokenFilter jwtTokenFilter;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testFilterWithValidToken() throws ServletException, IOException {
        UserDetails userDetails = new User(USERNAME, PASSWORD, new ArrayList<>());

        when(request.getHeader(AUTHORIZATION)).thenReturn(BEARER_PREFIX + VALID_TOKEN);
        when(tokenProvider.getUsernameFromToken(VALID_TOKEN)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(tokenProvider.validateToken(VALID_TOKEN, userDetails)).thenReturn(true);

        jwtTokenFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(USERNAME);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testFilterWithInValidToken() throws ServletException, IOException {
        UserDetails userDetails = new User(USERNAME, PASSWORD, new ArrayList<>());

        when(request.getHeader(AUTHORIZATION)).thenReturn(BEARER_PREFIX + INVALID_TOKEN);
        when(tokenProvider.getUsernameFromToken(INVALID_TOKEN)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(tokenProvider.validateToken(INVALID_TOKEN, userDetails)).thenReturn(false);

        jwtTokenFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testFilterWithoutHeader() throws ServletException, IOException {
        when(request.getHeader(AUTHORIZATION)).thenReturn(null);

        jwtTokenFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(tokenProvider, userDetailsService);
    }

    @Test
    void testFilterWithoutBearer() throws ServletException, IOException {
        when(request.getHeader(AUTHORIZATION)).thenReturn(BASIC_AUTH_HEADER);

        jwtTokenFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(tokenProvider, userDetailsService);
    }

}
