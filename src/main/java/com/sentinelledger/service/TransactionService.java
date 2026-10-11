
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

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            LedgerEntryRepository ledgerEntryRepository) {

        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
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
        if (transaction.getSenderAccountId() == null
                || transaction.getReceiverAccountId() == null) {
            throw new IllegalArgumentException(
                    "Sender and receiver account IDs are required");
        }

        // 3. Prevent transfers to the same account
        if (transaction.getSenderAccountId()
                .equals(transaction.getReceiverAccountId())) {
            throw new IllegalArgumentException(
                    "Sender and receiver accounts must be different");
        }

        // 4. Find sender
        Account sender = accountRepository
                .findById(transaction.getSenderAccountId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Sender account not found with id: "
                                        + transaction.getSenderAccountId()));

        // 5. Find receiver
        Account receiver = accountRepository
                .findById(transaction.getReceiverAccountId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Receiver account not found with id: "
                                        + transaction.getReceiverAccountId()));

        // 6. Check available balance
        if (sender.getBalance().compareTo(transaction.getAmount()) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient balance in sender account");
        }

        // 7. Update account balances
        sender.setBalance(
                sender.getBalance().subtract(transaction.getAmount()));

        receiver.setBalance(
                receiver.getBalance().add(transaction.getAmount()));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        // 8. Generate transaction details
        transaction.setTransactionReference(
                "TXN-" + UUID.randomUUID());

        transaction.setStatus("SUCCESS");
        transaction.setCreatedAt(LocalDateTime.now());

        // 9. Save the transaction first
        Transaction savedTransaction =
                transactionRepository.save(transaction);

        // 10. Create the sender's DEBIT ledger entry
        LedgerEntry debitEntry = new LedgerEntry();
        debitEntry.setTransaction(savedTransaction);
        debitEntry.setAccount(sender);
        debitEntry.setEntryType("DEBIT");
        debitEntry.setAmount(transaction.getAmount());
        debitEntry.setCreatedAt(LocalDateTime.now());

        ledgerEntryRepository.save(debitEntry);

        // 11. Create the receiver's CREDIT ledger entry
        LedgerEntry creditEntry = new LedgerEntry();
        creditEntry.setTransaction(savedTransaction);
        creditEntry.setAccount(receiver);
        creditEntry.setEntryType("CREDIT");
        creditEntry.setAmount(transaction.getAmount());
        creditEntry.setCreatedAt(LocalDateTime.now());

        ledgerEntryRepository.save(creditEntry);

        // 12. Return the completed transaction
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
