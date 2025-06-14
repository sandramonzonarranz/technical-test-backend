package com.playtomic.tests.wallet.domain.event;

import java.util.UUID;

public record PaymentFailedEvent(UUID walletId, String reason) {}

