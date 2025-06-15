package com.playtomic.tests.wallet.controller;

import com.playtomic.tests.wallet.service.WalletService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private static final Logger LOGGER = LoggerFactory.getLogger(WalletController.class);  // TODO añadir logs

    public record TopUpRequest(
            @NotNull
            @DecimalMin(value = "10.0", message = "Amount must be positive and greater than or equal to 10.0")
            BigDecimal amount,

            @NotBlank(message = "Credit card number cannot be blank")
            String creditCardNumber,

            @NotBlank(message = "Payment provider cannot be blank")
            String paymentProvider
    ) {}
    public record WalletResponse(UUID id, BigDecimal balance) {}

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWalletById(@PathVariable UUID walletId) {
        var wallet = walletService.findById(walletId);
        return ResponseEntity.ok(new WalletResponse(wallet.getId(), wallet.getBalance()));
    }

    @PostMapping("/{walletId}/top-up")
    public ResponseEntity<Void> topUpWallet(@PathVariable UUID walletId, @Valid @RequestBody TopUpRequest request) {
        walletService.topUp(walletId, request.amount(), request.creditCardNumber(), request.paymentProvider());
        return ResponseEntity.accepted().build();
    }

}
