package com.playtomic.tests.wallet.store.repository;

import com.playtomic.tests.wallet.store.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {

    List<WalletTransaction> findByWalletId(UUID walletId);

    Optional<WalletTransaction> findByWalletIdAndIdempotencyKey(UUID walletId, UUID idempotencyKey);

}
