
package com.sentinelledger.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sentinelledger.dto.LedgerEntryResponse;
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
    public List<LedgerEntryResponse> getEntriesByTransactionId(
            @PathVariable Long transactionId) {

        return ledgerEntryService
                .getEntriesByTransactionId(transactionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/account/{accountId}")
    public List<LedgerEntryResponse> getEntriesByAccountId(
            @PathVariable Long accountId) {

        return ledgerEntryService
                .getEntriesByAccountId(accountId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private LedgerEntryResponse toResponse(LedgerEntry entry) {
        return new LedgerEntryResponse(
                entry.getId(),
                entry.getTransaction().getId(),
                entry.getAccount().getId(),
                entry.getEntryType(),
                entry.getAmount(),
                entry.getCreatedAt());
    }
}
