
package com.sentinelledger.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sentinelledger.entity.LedgerEntry;

public interface LedgerEntryRepository
        extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByTransactionId(Long transactionId);

    List<LedgerEntry> findByAccountId(Long accountId);
}
