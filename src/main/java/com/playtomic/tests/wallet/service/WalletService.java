package com.playtomic.tests.wallet.service;
import com.playtomic.tests.wallet.store.Wallet;
import com.playtomic.tests.wallet.store.WalletTransaction;
import com.playtomic.tests.wallet.store.repository.WalletRepository;
import com.playtomic.tests.wallet.domain.event.TopUpRequestedEvent;
import com.playtomic.tests.wallet.service.exceptions.WalletNotFoundException;
import com.playtomic.tests.wallet.store.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final ApplicationEventPublisher eventPublisher;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    @Transactional
    public Wallet findById(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseGet(() -> createNewWallet(walletId));
    }

    private Wallet createNewWallet(UUID walletId) {
        return walletRepository.save(new Wallet(BigDecimal.ZERO, walletId));
    }

    /**
     * Start the top-up process for a wallet in asynchronous way
     * @param walletId         Id of the wallet to top up.
     * @param idempotencyKey   Idempotency Key to top up just once
     * @param amount           The amount to top up the wallet.
     * @param creditCardNumber the credit card number to use for the top-up.
     * @param provider         The payment provider to use for the top-up (e.g., "stripe").
     */
    @Transactional
    public void topUp(UUID walletId, UUID idempotencyKey, BigDecimal amount, String creditCardNumber, String provider) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(WalletNotFoundException::new);
        try {
            WalletTransaction transaction = new WalletTransaction(null, wallet, amount, WalletTransaction.TransactionType.TOP_UP, null, idempotencyKey, WalletTransaction.Status.PENDING);
            transactionRepository.save(transaction);
        } catch (DataIntegrityViolationException e) {
            log.warn("Idempotent request for walletId {} and key {} is a duplicate. Skipping.", walletId, idempotencyKey);
            return;
        }
        eventPublisher.publishEvent(new TopUpRequestedEvent(walletId, idempotencyKey, creditCardNumber, amount, provider));
    }

}