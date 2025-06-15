package com.playtomic.tests.wallet.domain.listener;

import com.playtomic.tests.wallet.domain.event.PaymentCompletedEvent;
import com.playtomic.tests.wallet.domain.event.PaymentFailedEvent;
import com.playtomic.tests.wallet.domain.event.TopUpRequestedEvent;
import com.playtomic.tests.wallet.service.exceptions.StripeServiceException;
import com.playtomic.tests.wallet.service.payment.PaymentService;
import com.playtomic.tests.wallet.service.payment.PaymentServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentListener {

    private final PaymentServiceProvider paymentServiceProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @TransactionalEventListener
    public void handleTopUpRequest(TopUpRequestedEvent event) {
        try {
            PaymentService paymentService = paymentServiceProvider.getService(event.provider());
            var payment = paymentService.charge(event.creditCardNumber(), event.amount());
            eventPublisher.publishEvent(new PaymentCompletedEvent(event.walletId(), payment.getId(), event.amount()));
        } catch (StripeServiceException e) {
            eventPublisher.publishEvent(new PaymentFailedEvent(event.walletId(), e.getClass().getSimpleName()));
        }
    }
}