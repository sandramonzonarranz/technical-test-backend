package com.playtomic.tests.wallet.domain.event;

import java.math.BigDecimal;
import java.util.UUID;

public record TopUpRequestedEvent(UUID walletId, String paymentId, BigDecimal amount) {}

