package com.playtomic.tests.wallet.service.payment;


import com.playtomic.tests.wallet.domain.Payment;
import com.playtomic.tests.wallet.service.exceptions.PaymentServiceException;
import com.playtomic.tests.wallet.service.exceptions.StripeServiceException;
import lombok.NonNull;

import java.math.BigDecimal;

public interface PaymentService {

    Payment charge(String creditCardNumber, BigDecimal amount) throws PaymentServiceException;

    void refund(@NonNull String paymentId) throws StripeServiceException;

    String getProviderId();

}
