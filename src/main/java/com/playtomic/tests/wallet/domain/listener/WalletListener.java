package com.playtomic.tests.wallet.domain.listener;

import com.playtomic.tests.wallet.domain.event.PaymentCompletedEvent;
import com.playtomic.tests.wallet.service.WalletUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletListener {

    private final WalletUpdateService walletUpdateService;

    @EventListener
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        walletUpdateService.applyTopUp(event);
    }
}