package com.playtomic.tests.wallet.service;

import com.playtomic.tests.wallet.store.repository.WalletTransaction;
import com.playtomic.tests.wallet.store.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class WalletTransactionService {

    private final WalletTransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<WalletTransaction> findTransactionsByWalletId(UUID walletId) {
        return transactionRepository.findByWalletId(walletId);
    }

}
