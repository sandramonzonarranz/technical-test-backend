package com.playtomic.tests.wallet.domain.listener;


import com.playtomic.tests.wallet.domain.event.PaymentFailedEvent;
import com.playtomic.tests.wallet.service.FakeEmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final FakeEmailSenderService emailSender;


    @Async
    @EventListener
    public void handlePaymentFailure(PaymentFailedEvent event) {
        String userEmail = "user-" + event.walletId().toString().substring(0, 8) + "@example.com";  // Fake simulation of user email with walletId
        String subject = "Error topping up your wallet";
        String body = String.format(
                "Dear user, we are so sorry to inform you that your top-up has failed due to: %s.",
                event.reason()
        );

        emailSender.send(userEmail, subject, body);
    }
}
