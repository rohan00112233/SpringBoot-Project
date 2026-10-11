
package com.sentinelledger.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sentinelledger.entity.LedgerEntry;
import com.sentinelledger.repository.LedgerEntryRepository;

@Service
public class LedgerEntryService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerEntryService(
            LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    public List<LedgerEntry> getEntriesByTransactionId(
            Long transactionId) {
        return ledgerEntryRepository
                .findByTransactionId(transactionId);
    }

    public List<LedgerEntry> getEntriesByAccountId(
            Long accountId) {
        return ledgerEntryRepository
                .findByAccountId(accountId);
    }
}
