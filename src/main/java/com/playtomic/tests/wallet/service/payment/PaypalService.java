package com.playtomic.tests.wallet.service.payment;


import com.playtomic.tests.wallet.domain.Payment;
import com.playtomic.tests.wallet.service.exceptions.PaymentServiceException;
import com.playtomic.tests.wallet.service.exceptions.StripeServiceException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@Slf4j
public class PaypalService implements PaymentService {

    public static final String PAYPAL = "PAYPAL";

    @Override
    public Payment charge(String creditCardNumber, BigDecimal amount) throws PaymentServiceException {
        // TODO : Implement charge logic for PayPal
        throw new UnsupportedOperationException("Charge method is not implemented yet.");
    }

    @Override
    public void refund(@NonNull String paymentId) throws StripeServiceException {
        // TODO : Implement refund logic for PayPal
        throw new UnsupportedOperationException("Charge method is not implemented yet.");
    }

    @Override
    public String getProviderId() {
        return PAYPAL;
    }

}
