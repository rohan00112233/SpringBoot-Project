
package com.sentinelledger.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LedgerEntryResponse {

    private Long id;
    private Long transactionId;
    private Long accountId;
    private String entryType;
    private BigDecimal amount;
    private LocalDateTime createdAt;

    public LedgerEntryResponse(
            Long id,
            Long transactionId,
            Long accountId,
            String entryType,
            BigDecimal amount,
            LocalDateTime createdAt) {

        this.id = id;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.entryType = entryType;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getEntryType() {
        return entryType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}