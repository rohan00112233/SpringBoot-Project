package com.sentinelledger.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sentinelledger.entity.LedgerEntry;
import com.sentinelledger.service.LedgerEntryService;

@RestController
@RequestMapping("/api/ledger")
public class LedgerEntryController {

    private final LedgerEntryService ledgerEntryService;

    public LedgerEntryController(
            LedgerEntryService ledgerEntryService) {
        this.ledgerEntryService = ledgerEntryService;
    }

    @GetMapping("/transaction/{transactionId}")
    public List<LedgerEntry> getEntriesByTransactionId(
            @PathVariable Long transactionId) {
        return ledgerEntryService
                .getEntriesByTransactionId(transactionId);
    }

    @GetMapping("/account/{accountId}")
    public List<LedgerEntry> getEntriesByAccountId(
            @PathVariable Long accountId) {
        return ledgerEntryService
                .getEntriesByAccountId(accountId);
    }
}
