package com.playtomic.tests.wallet.domain.listener;

import com.playtomic.tests.wallet.domain.event.PaymentCompletedEvent;
import com.playtomic.tests.wallet.service.WalletUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletListener {

    private final WalletUpdateService walletUpdateService;

    @EventListener
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Received PaymentCompletedEvent for Wallet ID: {}. Applying top-up of amount: {}.", event.walletId(), event.amount());
        walletUpdateService.applyTopUp(event);
    }
}