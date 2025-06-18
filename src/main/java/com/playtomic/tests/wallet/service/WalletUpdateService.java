package com.playtomic.tests.wallet.service;

import com.playtomic.tests.wallet.domain.event.PaymentCompletedEvent;
import com.playtomic.tests.wallet.domain.event.WalletReconciliationEvent;
import com.playtomic.tests.wallet.store.Wallet;
import com.playtomic.tests.wallet.store.repository.WalletRepository;
import com.playtomic.tests.wallet.store.WalletTransaction;
import com.playtomic.tests.wallet.store.repository.WalletTransactionRepository;
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
    private final ApplicationEventPublisher eventPublisher;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletRepository walletRepository;


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
        WalletTransaction transaction = walletTransactionRepository.findByWalletIdAndIdempotencyKey(event.walletId(), event.idempotencyKey())
                .orElseThrow(() -> new IllegalStateException("FATAL: No PENDING transaction found for completed payment."));

        if (transaction.getStatus() != WalletTransaction.Status.PENDING) {
            log.warn("Transaction {} for wallet {} already has status {}. Skipping update.", transaction.getId(), event.walletId(), transaction.getStatus());
            return;
        }

        Wallet wallet = transaction.getWallet();
        wallet.topUp(event.amount());
        transaction.setStatus(WalletTransaction.Status.COMPLETED);

        walletRepository.save(wallet);
        walletTransactionRepository.save(transaction);
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
