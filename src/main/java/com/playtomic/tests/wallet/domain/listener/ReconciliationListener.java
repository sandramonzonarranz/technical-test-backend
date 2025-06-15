package com.playtomic.tests.wallet.domain.listener;

import com.playtomic.tests.wallet.domain.event.WalletReconciliationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReconciliationListener {

    @Async
    @EventListener
    public void handleReconciliation(WalletReconciliationEvent event) {
        log.warn("Starting Reconciliation process for Wallet ID: {}, PaymentId: {} Amount: {} ", event.walletId(), event.paymentId(), event.amount());
        // TODO: Here we'll do the logic for reconciliation
    }

 }
