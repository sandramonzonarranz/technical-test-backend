package com.playtomic.tests.wallet.service.payment;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentServiceProvider {

    private final Map<String, PaymentService> services;

    public PaymentServiceProvider(List<PaymentService> paymentServices) {
        this.services = paymentServices.stream()
                .collect(Collectors.toMap(PaymentService::getProviderId, Function.identity()));
    }

    public PaymentService getService(String providerId) {
        return Optional.ofNullable(services.get(providerId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid provider payment: " + providerId));
    }
}
