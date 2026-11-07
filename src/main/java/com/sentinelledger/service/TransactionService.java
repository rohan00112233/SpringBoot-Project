
package com.sentinelledger.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sentinelledger.entity.Account;
import com.sentinelledger.entity.LedgerEntry;
import com.sentinelledger.entity.Transaction;
import com.sentinelledger.exception.ResourceNotFoundException;
import com.sentinelledger.repository.AccountRepository;
import com.sentinelledger.repository.LedgerEntryRepository;
import com.sentinelledger.repository.TransactionRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final FraudDetectionService fraudDetectionService;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            LedgerEntryRepository ledgerEntryRepository,
            FraudDetectionService fraudDetectionService) {

        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.fraudDetectionService = fraudDetectionService;
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction) {

        // 1. Validate transaction amount
        if (transaction.getAmount() == null
                || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transaction amount must be greater than zero");
        }

        // 2. Validate account IDs
        Long senderId = transaction.getSenderAccountId();
        Long receiverId = transaction.getReceiverAccountId();

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException(
                    "Sender and receiver account IDs are required");
        }

        // 3. Prevent transfers to the same account
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException(
                    "Sender and receiver accounts must be different");
        }

        // 4. Apply fraud detection
        if (fraudDetectionService.isHighValueTransaction(
                transaction.getAmount())) {

            throw new IllegalArgumentException(
                    fraudDetectionService.getFraudReason(
                            transaction.getAmount())
                    + "; transaction was not processed");
        }

        // 5. Lock accounts in ascending ID order.
        // This reduces the risk of deadlocks between opposite transfers.

        Account firstAccount;
        Account secondAccount;

        if (senderId < receiverId) {

            firstAccount = accountRepository.findByIdForUpdate(senderId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Sender account not found with id: "
                                            + senderId));

            secondAccount = accountRepository.findByIdForUpdate(receiverId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Receiver account not found with id: "
                                            + receiverId));

        } else {

            firstAccount = accountRepository.findByIdForUpdate(receiverId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Receiver account not found with id: "
                                            + receiverId));

            secondAccount = accountRepository.findByIdForUpdate(senderId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Sender account not found with id: "
                                            + senderId));
        }

        // 6. Identify sender and receiver after locking
        Account sender = senderId.equals(firstAccount.getId())
                ? firstAccount : secondAccount;

        Account receiver = receiverId.equals(firstAccount.getId())
                ? firstAccount : secondAccount;

        // 7. Check available balance after acquiring locks
        if (sender.getBalance().compareTo(transaction.getAmount()) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient balance in sender account");
        }

        // 8. Update account balances
        sender.setBalance(
                sender.getBalance().subtract(transaction.getAmount()));

        receiver.setBalance(
                receiver.getBalance().add(transaction.getAmount()));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        // 9. Generate transaction details
        LocalDateTime transactionTime = LocalDateTime.now();

        transaction.setTransactionReference(
                "TXN-" + UUID.randomUUID());

        transaction.setStatus("SUCCESS");
        transaction.setCreatedAt(transactionTime);

        // 10. Save transaction
        Transaction savedTransaction =
                transactionRepository.save(transaction);

        // 11. Create sender DEBIT ledger entry
        LedgerEntry debitEntry = new LedgerEntry();
        debitEntry.setTransaction(savedTransaction);
        debitEntry.setAccount(sender);
        debitEntry.setEntryType("DEBIT");
        debitEntry.setAmount(transaction.getAmount());
        debitEntry.setCreatedAt(transactionTime);

        ledgerEntryRepository.save(debitEntry);

        // 12. Create receiver CREDIT ledger entry
        LedgerEntry creditEntry = new LedgerEntry();
        creditEntry.setTransaction(savedTransaction);
        creditEntry.setAccount(receiver);
        creditEntry.setEntryType("CREDIT");
        creditEntry.setAmount(transaction.getAmount());
        creditEntry.setCreatedAt(transactionTime);

        ledgerEntryRepository.save(creditEntry);

        // 13. Return completed transaction
        return savedTransaction;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Transaction not found with id: " + id));
    }

    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        return transactionRepository
                .findBySenderAccountIdOrReceiverAccountId(
                        accountId, accountId);
    }
}
