package com.playtomic.tests.wallet.controller;

import com.playtomic.tests.wallet.configuration.SecurityConfig;
import com.playtomic.tests.wallet.configuration.jwt.JwtTokenProvider;
import com.playtomic.tests.wallet.service.WalletTransactionService;
import com.playtomic.tests.wallet.store.Wallet;
import com.playtomic.tests.wallet.store.WalletTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletTransactionController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
public class WalletTransactionControllerTest {

    public static final String V1_WALLET_TRANSACTIONS = "/v1/wallets/{walletId}/transactions";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletTransactionService transactionService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void getTransactionsByWalletIdReturnOk() throws Exception {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        WalletTransaction transaction = new WalletTransaction(
                UUID.randomUUID(),
                wallet,
                new BigDecimal("50.00"),
                WalletTransaction.TransactionType.TOP_UP,
                ZonedDateTime.now()
        );

        given(transactionService.findTransactionsByWalletId(walletId)).willReturn(List.of(transaction));

        mockMvc.perform(get(V1_WALLET_TRANSACTIONS, walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(transaction.getId().toString()))
                .andExpect(jsonPath("$[0].amount").value(50.00));
    }

    @Test
    void getTransactionsByWalletIdNotAuthenticatedReturnForbidden() throws Exception {
        UUID walletId = UUID.randomUUID();
        mockMvc.perform(get(V1_WALLET_TRANSACTIONS, walletId))
                .andExpect(status().isForbidden());
    }

}