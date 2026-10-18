
package com.sentinelledger.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Service
public class FraudDetectionService {

    private static final BigDecimal HIGH_VALUE_THRESHOLD =
            new BigDecimal("50000.00");

    public boolean isHighValueTransaction(BigDecimal amount) {

        if (amount == null || amount.signum() <= 0) {
            return false;
        }

        return amount.compareTo(HIGH_VALUE_THRESHOLD) > 0;
    }

    public String getFraudReason(BigDecimal amount) {

        if (isHighValueTransaction(amount)) {
            return "High-value transaction requires review";
        }

        return null;
    }
}
