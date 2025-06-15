package com.playtomic.tests.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtomic.tests.wallet.configuration.SecurityConfig;
import com.playtomic.tests.wallet.configuration.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
public class AuthControllerTest {

    private static final String USER = "user";
    private static final String PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void testLoginWithValidCredentialsReturnToken() throws Exception {
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(USER, PASSWORD);
        User userDetails = new User(USER, PASSWORD, new ArrayList<>());
        String fakeToken = "fake-jwt-token";
        Authentication authentication = new UsernamePasswordAuthenticationToken(USER, PASSWORD);

        given(authenticationManager.authenticate(any())).willReturn(authentication);
        given(userDetailsService.loadUserByUsername(USER)).willReturn(userDetails);
        given(jwtTokenProvider.generateToken(userDetails)).willReturn(fakeToken);

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(fakeToken));
    }

}
