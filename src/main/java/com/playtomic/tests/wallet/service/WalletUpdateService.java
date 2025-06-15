package com.playtomic.tests.wallet.service;

import com.playtomic.tests.wallet.domain.event.PaymentCompletedEvent;
import com.playtomic.tests.wallet.domain.event.WalletReconciliationEvent;
import com.playtomic.tests.wallet.domain.repository.WalletRepository;
import com.playtomic.tests.wallet.service.exceptions.WalletNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletUpdateService {

    private static final int MAX_ATTEMPTS = 3;
    private final WalletRepository walletRepository;
    private final ApplicationEventPublisher eventPublisher;


    /**
     * Doing the top-up wallet operation and that throws ObjectOptimisticLockingFailureException,
     * We'll retry 3 times with dealy of 100ms.
     */
    @Retryable(
            value = { ObjectOptimisticLockingFailureException.class },
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public void applyTopUp(PaymentCompletedEvent event) {
        var wallet = walletRepository.findById(event.walletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for ID: " + event.walletId()));

        wallet.topUp(event.amount());
        walletRepository.saveAndFlush(wallet);
    }

    /**
     * If all retry fails we will throw the WalletReconciliationRequiredEvent
     */
    @Recover
    public void recover(ObjectOptimisticLockingFailureException e, PaymentCompletedEvent event) {
        log.error("FATAL: Not able to update wallet {} salary due to concurrent updates. Please check the wallet balance and reconcile manually.",
                event.walletId(),
                e
        );
        eventPublisher.publishEvent(
                new WalletReconciliationEvent(event.walletId(), event.paymentId(), event.amount())
        );
    }
}
