package com.playtomic.tests.wallet.domain.listener;

import com.playtomic.tests.wallet.domain.event.PaymentCompletedEvent;
import com.playtomic.tests.wallet.domain.repository.WalletRepository;
import com.playtomic.tests.wallet.service.exceptions.WalletNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WalletListener {

    private final WalletRepository walletRepository;

    @EventListener
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        try {
            var wallet = walletRepository.findById(event.walletId())
                    .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
            wallet.topUp(event.amount());
            walletRepository.save(wallet);
        } catch (ObjectOptimisticLockingFailureException e) {
            //  TODO pensar en politica de retries, evento pa reconciilar etc
        }
    }
}