package com.playtomic.tests.wallet;

import com.playtomic.tests.wallet.controller.AuthController;
import com.playtomic.tests.wallet.controller.WalletController;
import com.playtomic.tests.wallet.domain.Payment;
import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.listener.NotificationListener;
import com.playtomic.tests.wallet.domain.listener.PaymentListener;
import com.playtomic.tests.wallet.domain.listener.WalletListener;
import com.playtomic.tests.wallet.domain.repository.WalletRepository;
import com.playtomic.tests.wallet.service.payment.PaymentServiceProvider;
import com.playtomic.tests.wallet.service.payment.StripeService;
import com.playtomic.tests.wallet.service.exceptions.StripeAmountTooSmallException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WalletApplicationIT {

	private static final String INITIAL_BALANCE = "100.00";
	private static final String PAYMENT_PROVIDER = "STRIPE";

	@Autowired
	private TestRestTemplate restTemplate;
	@Autowired
	private WalletRepository walletRepository;

	@MockBean
	private PaymentServiceProvider paymentServiceProvider;

	@Mock
	private StripeService stripeService;

	@SpyBean
	private PaymentListener paymentListener;
	@SpyBean
	private WalletListener walletListener;
	@SpyBean
	private NotificationListener notificationListener;

	@BeforeEach
	void setUp() {
		walletRepository.deleteAll();
	}

	@Test
	void testGetWalletById() {
		Wallet wallet = walletRepository.save(new Wallet(new BigDecimal(INITIAL_BALANCE)));
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(obtainJwtToken());
		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

		ResponseEntity<WalletController.WalletResponse> response = restTemplate.exchange(
				"/v1/wallets/" + wallet.getId(),
				HttpMethod.GET,
				requestEntity,
				WalletController.WalletResponse.class
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(0, new BigDecimal(INITIAL_BALANCE).compareTo(response.getBody().balance()));
	}

	@Test
	void testTopUpWalletOkFlow() {
		Wallet wallet = walletRepository.save(new Wallet(new BigDecimal("10.00")));
		UUID walletId = wallet.getId();
		when(paymentServiceProvider.getService(PAYMENT_PROVIDER)).thenReturn(stripeService);
		when(stripeService.charge(anyString(), any(BigDecimal.class))).thenReturn(new Payment("payment-id-123"));
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(obtainJwtToken());
		var topUpRequest = new WalletController.TopUpRequest(new BigDecimal("50.00"), "4242", PAYMENT_PROVIDER);
		HttpEntity<WalletController.TopUpRequest> requestEntity = new HttpEntity<>(topUpRequest, headers);

		ResponseEntity<Void> response = restTemplate.postForEntity("/v1/wallets/" + walletId + "/top-up", requestEntity, Void.class);

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			Wallet updatedWallet = walletRepository.findById(walletId).get();
			assertEquals(0, new BigDecimal("60.00").compareTo(updatedWallet.getBalance()));
		});
		verify(paymentListener, timeout(1000)).handleTopUpRequest(any());
		verify(walletListener, timeout(1000)).handlePaymentCompleted(any());
		verify(notificationListener, never()).handlePaymentFailure(any());
	}

	@Test
	void testTopUpWalletKOFlow() {
		Wallet wallet = walletRepository.save(new Wallet(new BigDecimal("20.00")));
		UUID walletId = wallet.getId();
		when(paymentServiceProvider.getService(PAYMENT_PROVIDER)).thenReturn(stripeService);
		when(stripeService.charge(anyString(), any(BigDecimal.class))).thenThrow(new StripeAmountTooSmallException());
		String token = obtainJwtToken();
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token);
		var topUpRequest = new WalletController.TopUpRequest(new BigDecimal("5.00"), "1234", PAYMENT_PROVIDER);
		HttpEntity<WalletController.TopUpRequest> requestEntity = new HttpEntity<>(topUpRequest, headers);

		ResponseEntity<Void> response = restTemplate.postForEntity("/v1/wallets/" + walletId + "/top-up", requestEntity, Void.class);
		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

		verify(paymentListener, timeout(1000)).handleTopUpRequest(any());
		verify(notificationListener, timeout(1000)).handlePaymentFailure(any());
		verify(walletListener, never()).handlePaymentCompleted(any());

		Wallet finalWallet = walletRepository.findById(walletId).get();
		assertEquals(0, new BigDecimal("20.00").compareTo(finalWallet.getBalance()));
	}

	private String obtainJwtToken() {
		var loginRequest = new AuthController.LoginRequest("user", "password");
		ResponseEntity<AuthController.LoginResponse> response = restTemplate.postForEntity("/v1/auth/login", loginRequest, AuthController.LoginResponse.class);
		assertNotNull(response.getBody());
		return response.getBody().token();
	}

}
