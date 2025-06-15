package com.playtomic.tests.wallet.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtomic.tests.wallet.domain.Payment;
import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.exceptions.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.exceptions.StripeServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RestClientTest(StripeService.class)
public class StripeServiceTest {

    private static final String CREDIT_CARD_NUMBER = "4242 4242 4242 4242";
    private static final String PAYMENT_ID = "id123";

    private final URI testChargesUri = URI.create("https://sandbox.playtomic.io/v1/stripe-simulator/charges");
    private final URI testRefundsUri = URI.create("testUrl/refunds");

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;
    @Autowired
    private MockRestServiceServer mockServer;
    @Autowired
    private StripeService stripeService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeTestMethod
    public void setUp() {
        RestTemplate restTemplate = restTemplateBuilder.build();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        stripeService = new StripeService(testChargesUri, testRefundsUri, restTemplateBuilder);
    }

    @Test
    public void testCharge() throws StripeServiceException, JsonProcessingException {
        Payment expectedPayment = new Payment(PAYMENT_ID);
        String paymentJsonResponse = objectMapper.writeValueAsString(expectedPayment);

        mockServer.expect(ExpectedCount.once(), requestTo(testChargesUri))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(paymentJsonResponse));

        Payment payment = stripeService.charge(CREDIT_CARD_NUMBER, new BigDecimal(10));

        assertNotNull(payment);
        assertNotNull(payment.getId());
        mockServer.verify();
    }

    @Test
    public void testChargeStripeAmountTooSmallException() {
        mockServer.expect(ExpectedCount.once(), requestTo(testChargesUri))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        assertThrows(StripeAmountTooSmallException.class, () -> stripeService.charge(CREDIT_CARD_NUMBER, new BigDecimal(5)));
        mockServer.verify();
    }

}

