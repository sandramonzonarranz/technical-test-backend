package com.playtomic.tests.wallet.domain.listener;

import com.playtomic.tests.wallet.domain.event.PaymentCompletedEvent;
import com.playtomic.tests.wallet.domain.event.PaymentFailedEvent;
import com.playtomic.tests.wallet.domain.event.TopUpRequestedEvent;
import com.playtomic.tests.wallet.service.exceptions.StripeServiceException;
import com.playtomic.tests.wallet.service.payment.PaymentService;
import com.playtomic.tests.wallet.service.payment.PaymentServiceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentListener {

    private final PaymentServiceProvider paymentServiceProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @TransactionalEventListener
    public void handleTopUpRequest(TopUpRequestedEvent event) {
        try {
            PaymentService paymentService = paymentServiceProvider.getService(event.provider());
            var payment = paymentService.charge(event.creditCardNumber(), event.amount());
            log.info("Payment successful via {}. Payment ID: {}. Publishing PaymentCompletedEvent.", event.provider(), payment.getId());
            eventPublisher.publishEvent(new PaymentCompletedEvent(event.walletId(), event.idempotencyKey(), payment.getId(), event.amount()));
        } catch (StripeServiceException e) {
            log.error("Payment failed for Wallet ID: {}. Reason: {}. Publishing PaymentFailedEvent.", event.walletId(), e.getClass().getSimpleName(), e);
            eventPublisher.publishEvent(new PaymentFailedEvent(event.walletId(), e.getClass().getSimpleName()));
        }
    }
}