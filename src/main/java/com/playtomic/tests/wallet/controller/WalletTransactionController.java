package com.playtomic.tests.wallet.controller;

import com.playtomic.tests.wallet.service.WalletTransactionService;
import com.playtomic.tests.wallet.store.repository.WalletTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/wallets")
@RequiredArgsConstructor
public class WalletTransactionController {

    private final WalletTransactionService transactionService;

    public record TransactionResponse(UUID id, BigDecimal amount, String type, ZonedDateTime createdAt) {}

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByWalletId(@PathVariable UUID walletId) {
        List<WalletTransaction> transactions = transactionService.findTransactionsByWalletId(walletId);
        List<TransactionResponse> response = transactions.stream()
                .map(t -> new TransactionResponse(t.getId(), t.getAmount(), t.getType().name(), t.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
