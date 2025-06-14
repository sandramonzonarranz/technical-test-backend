package com.playtomic.tests.wallet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class Wallet {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, scale = 2, precision = 4)
    private BigDecimal balance = BigDecimal.ZERO;

    @Version
    private Long version;

    public Wallet(BigDecimal initialBalance) {
        this.balance = initialBalance;
    }

    public void topUp(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top-up amount must be positive.");
        }
        this.balance = this.balance.add(amount);
    }
}

