
package service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sentinelledger.service.FraudDetectionService;

class FraudDetectionServiceTest {

    private FraudDetectionService fraudDetectionService;

    @BeforeEach
    void setUp() {
        fraudDetectionService = new FraudDetectionService();
    }

    @Test
    void shouldNotFlagTransactionBelowThreshold() {
        BigDecimal amount = new BigDecimal("49999.99");

        assertFalse(
                fraudDetectionService.isHighValueTransaction(amount));
    }

    @Test
    void shouldNotFlagTransactionAtThreshold() {
        BigDecimal amount = new BigDecimal("50000.00");

        assertFalse(
                fraudDetectionService.isHighValueTransaction(amount));
    }

    @Test
    void shouldFlagTransactionAboveThreshold() {
        BigDecimal amount = new BigDecimal("50000.01");

        assertTrue(
                fraudDetectionService.isHighValueTransaction(amount));
    }

    @Test
    void shouldReturnFraudReasonForHighValueTransaction() {
        BigDecimal amount = new BigDecimal("60000.00");

        assertTrue(
                fraudDetectionService.getFraudReason(amount)
                        .contains("High-value transaction"));
    }

    @Test
    void shouldReturnNullReasonForNormalTransaction() {
        BigDecimal amount = new BigDecimal("100.00");

        assertNull(
                fraudDetectionService.getFraudReason(amount));
    }

    @Test
    void shouldNotFlagNullAmount() {
        assertFalse(
                fraudDetectionService.isHighValueTransaction(null));
    }

    @Test
    void shouldNotFlagZeroOrNegativeAmount() {
        assertFalse(
                fraudDetectionService.isHighValueTransaction(
                        new BigDecimal("0")));

        assertFalse(
                fraudDetectionService.isHighValueTransaction(
                        new BigDecimal("-100")));
    }
}
