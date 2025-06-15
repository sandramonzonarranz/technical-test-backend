package com.playtomic.tests.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtomic.tests.wallet.configuration.SecurityConfig;
import com.playtomic.tests.wallet.configuration.jwt.JwtTokenProvider;
import com.playtomic.tests.wallet.store.Wallet;
import com.playtomic.tests.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(WalletController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
public class WalletControllerTest {

    private static final String CREDIT_CARD_NUMBER = "4242 4242 4242 4242";
    private static final String WALLET_ENDPOINT = "/v1/wallets/";
    private static final String PAYMENT_PROVIDER = "STRIPE";
    private static final String TOP_UP = "/top-up";
    private static final UUID ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private WalletService walletService;

    private WalletController.TopUpRequest topUpRequest;

    @BeforeEach
    void setUp() {
        topUpRequest = new WalletController.TopUpRequest(new BigDecimal("10.00"), CREDIT_CARD_NUMBER, PAYMENT_PROVIDER);
    }

    @Test
    void getWalletByIdNotAuthenticatedReturnUnauthorized() throws Exception {
        mockMvc.perform(get(WALLET_ENDPOINT + ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getWalletByIdReturnOk() throws Exception {
        Wallet wallet = new Wallet(new BigDecimal("123.45"), ID);
        given(walletService.findById(ID)).willReturn(wallet);

        mockMvc.perform(get("/v1/wallets/" + ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(123.45));
    }

    @Test
    @WithMockUser
    void topUpWalletReturnAccepted() throws Exception {
        String requestJson = new ObjectMapper().writeValueAsString(topUpRequest);

        mockMvc.perform(post(WALLET_ENDPOINT + ID + TOP_UP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isAccepted());
        verify(walletService).topUp(eq(ID), any(BigDecimal.class), eq(CREDIT_CARD_NUMBER), eq(PAYMENT_PROVIDER));
    }

    @Test
    @WithMockUser
    void topUpWalletInvalidRequestReturnBadRequest() throws Exception {
        WalletController.TopUpRequest invalidRequest = new WalletController.TopUpRequest(null, null, null);
        String requestJson = new ObjectMapper().writeValueAsString(invalidRequest);

        mockMvc.perform(post(WALLET_ENDPOINT + ID + TOP_UP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

}
