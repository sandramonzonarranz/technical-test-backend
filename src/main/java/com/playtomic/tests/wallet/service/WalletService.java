package com.playtomic.tests.wallet.service;
import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.repository.WalletRepository;
import com.playtomic.tests.wallet.domain.event.TopUpRequestedEvent;
import com.playtomic.tests.wallet.service.exceptions.WalletNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Wallet findById(UUID walletId) {
        return walletRepository.findById(walletId).orElseThrow(WalletNotFoundException::new);
    }

    /**
     * Start the top-up process for a wallet in asynchronous way
     * @param walletId         Id of the wallet to top up.
     * @param amount           The amount to top up the wallet.
     * @param creditCardNumber the credit card number to use for the top-up.
     * @param provider         The payment provider to use for the top-up (e.g., "stripe").
     */
    @Transactional
    public void topUp(UUID walletId, BigDecimal amount, String creditCardNumber, String provider) {
        if (!walletRepository.existsById(walletId)) {
            throw new WalletNotFoundException();
        }
        eventPublisher.publishEvent(new TopUpRequestedEvent(walletId, creditCardNumber, amount, provider));
    }

}