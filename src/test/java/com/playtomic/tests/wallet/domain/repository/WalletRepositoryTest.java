package com.playtomic.tests.wallet.domain.repository;


import com.playtomic.tests.wallet.domain.Wallet;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
public class WalletRepositoryTest {

    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("100.50");

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void testSaveAndFindById() {
        Wallet newWallet = new Wallet(INITIAL_BALANCE);

        Wallet savedWallet = walletRepository.save(newWallet);
        entityManager.flush();
        entityManager.clear();
        Wallet foundWallet = walletRepository.findById(savedWallet.getId()).orElse(null);

        assertNotNull(foundWallet);
        assertEquals(savedWallet.getId(), foundWallet.getId());
        assertEquals(0, INITIAL_BALANCE.compareTo(foundWallet.getBalance()));
        assertEquals(0L, foundWallet.getVersion());
    }

    @Test
    void optimisticLocking_shouldIncrementVersionOnUpdate() {
        Wallet wallet = walletRepository.saveAndFlush(new Wallet(INITIAL_BALANCE));
        assertEquals(0L, wallet.getVersion());

        wallet.topUp(new BigDecimal("50.00"));
        walletRepository.saveAndFlush(wallet);
        entityManager.clear();

        Wallet updatedWallet = walletRepository.findById(wallet.getId()).orElseThrow(() -> new IllegalStateException("Wallet not found with ID: " + wallet.getId()));
        assertEquals(1L, updatedWallet.getVersion());
        assertEquals(0, new BigDecimal("150.50").compareTo(updatedWallet.getBalance()));
    }

    @Test
    void testConcurrentUpdatesThrowException() {
        Wallet wallet1 = walletRepository.saveAndFlush(new Wallet(new BigDecimal("300.00")));
        Wallet walletInstance1 = walletRepository.findById(wallet1.getId()).orElseThrow(() -> new IllegalStateException("Wallet not found with ID: " + wallet1.getId()));
        entityManager.clear();
        Wallet walletInstance2 = walletRepository.findById(wallet1.getId()).orElseThrow(() -> new IllegalStateException("Wallet not found with ID: " + wallet1.getId()));
        walletInstance2.topUp(new BigDecimal("20.00"));
        walletRepository.saveAndFlush(walletInstance2);

        walletInstance1.topUp(new BigDecimal("10.00"));

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> walletRepository.saveAndFlush(walletInstance1));
    }

}