
package com.sentinelledger.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.sentinelledger.entity.Account;
import com.sentinelledger.entity.Transaction;
import com.sentinelledger.entity.LedgerEntry;
import com.sentinelledger.repository.AccountRepository;
import com.sentinelledger.repository.TransactionRepository;
import com.sentinelledger.repository.LedgerEntryRepository;

@SpringBootTest
@ActiveProfiles("test")
class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    private Account sender;
    private Account receiver;

    @BeforeEach
    void setUp() {
        // Clear test data in foreign-key order.
        ledgerEntryRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();

        sender = new Account(
                "Test Sender",
                "TEST001",
                new BigDecimal("7700.00")
        );

        receiver = new Account(
                "Test Receiver",
                "TEST002",
                new BigDecimal("12300.00")
        );

        sender = accountRepository.saveAndFlush(sender);
        receiver = accountRepository.saveAndFlush(receiver);
    }

    private Transaction createTransfer(BigDecimal amount) {
        Transaction transaction = new Transaction();

        transaction.setSenderAccountId(sender.getId());
        transaction.setReceiverAccountId(receiver.getId());
        transaction.setAmount(amount);
        transaction.setTransactionType("TRANSFER");

        return transaction;
    }

    @Test
    void shouldTransferMoneySuccessfully() {

        Transaction result = transactionService.createTransaction(
                createTransfer(new BigDecimal("1000.00"))
        );

        Account updatedSender = accountRepository
                .findById(sender.getId())
                .orElseThrow();

        Account updatedReceiver = accountRepository
                .findById(receiver.getId())
                .orElseThrow();

        assertEquals("SUCCESS", result.getStatus());

        assertEquals(
                new BigDecimal("6700.00"),
                updatedSender.getBalance()
        );

        assertEquals(
                new BigDecimal("13300.00"),
                updatedReceiver.getBalance()
        );

        List<LedgerEntry> entries =
                ledgerEntryRepository.findByTransactionId(result.getId());

        assertEquals(2, entries.size());
    }

    @Test
    void shouldRejectTransactionWithInsufficientBalance() {

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.createTransaction(
                        createTransfer(new BigDecimal("8000.00"))
                )
        );

        Account unchangedSender = accountRepository
                .findById(sender.getId())
                .orElseThrow();

        assertEquals(
                new BigDecimal("7700.00"),
                unchangedSender.getBalance()
        );

        assertEquals(0, transactionRepository.count());
        assertEquals(0, ledgerEntryRepository.count());
    }

    @Test
    void shouldPreventConcurrentOverspending() throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Future<Boolean> request1 = executor.submit(() -> {

                ready.countDown();
                start.await();

                try {
                    transactionService.createTransaction(
                            createTransfer(new BigDecimal("5000.00"))
                    );
                    return true;

                } catch (IllegalArgumentException exception) {
                    return false;
                }
            });

            Future<Boolean> request2 = executor.submit(() -> {

                ready.countDown();
                start.await();

                try {
                    transactionService.createTransaction(
                            createTransfer(new BigDecimal("5000.00"))
                    );
                    return true;

                } catch (IllegalArgumentException exception) {
                    return false;
                }
            });

            assertTrue(
                    ready.await(10, TimeUnit.SECONDS),
                    "Both requests should be ready"
            );

            start.countDown();

            boolean result1 = request1.get();
            boolean result2 = request2.get();

            // Exactly one transaction should succeed.
            assertNotEquals(result1, result2);

            assertEquals(1, transactionRepository.count());
            assertEquals(2, ledgerEntryRepository.count());

            Account updatedSender = accountRepository
                    .findById(sender.getId())
                    .orElseThrow();

            Account updatedReceiver = accountRepository
                    .findById(receiver.getId())
                    .orElseThrow();

            assertEquals(
                    new BigDecimal("2700.00"),
                    updatedSender.getBalance()
            );

            assertEquals(
                    new BigDecimal("17300.00"),
                    updatedReceiver.getBalance()
            );

        } finally {
            executor.shutdownNow();
        }
    }
}
