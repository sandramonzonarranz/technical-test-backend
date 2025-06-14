package com.playtomic.tests.wallet.domain.listener;

import com.playtomic.tests.wallet.domain.event.PaymentCompletedEvent;
import com.playtomic.tests.wallet.domain.event.PaymentFailedEvent;
import com.playtomic.tests.wallet.domain.event.TopUpRequestedEvent;
import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.exceptions.StripeServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentListener {

    private final StripeService stripeService;  // posible ampliacion a cualquier servicio de pago
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @TransactionalEventListener
    public void handleTopUpRequest(TopUpRequestedEvent event) {
        try {
            var payment = stripeService.charge(event.paymentId(), event.amount());
            eventPublisher.publishEvent(new PaymentCompletedEvent(event.walletId(), payment.getId(), event.amount()));
        } catch (StripeServiceException e) {
            eventPublisher.publishEvent(new PaymentFailedEvent(event.walletId(), e.getClass().getSimpleName()));  //TODO añadir motivo
        }
    }
}