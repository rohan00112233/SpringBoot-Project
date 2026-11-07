
package com.sentinelledger.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sentinelledger.entity.Account;
import com.sentinelledger.exception.ResourceNotFoundException;
import com.sentinelledger.repository.AccountRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(Account account) {

        if (accountRepository.existsByAccountNumber(
                account.getAccountNumber())) {
            throw new IllegalArgumentException(
                    "Account number already exists: "
                    + account.getAccountNumber());
        }

        return accountRepository.save(account);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with id: " + id));
    }

    public Account updateAccount(Long id, Account updatedAccount) {

        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with id: " + id));

        if (accountRepository.existsByAccountNumberAndIdNot(
                updatedAccount.getAccountNumber(), id)) {
            throw new IllegalArgumentException(
                    "Account number already exists: "
                    + updatedAccount.getAccountNumber());
        }

        existingAccount.setAccountHolderName(
                updatedAccount.getAccountHolderName());

        existingAccount.setAccountNumber(
                updatedAccount.getAccountNumber());

        // Balance can only be modified through transaction processing.

        return accountRepository.save(existingAccount);
    }

    public void deleteAccount(Long id) {

        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with id: " + id));

        accountRepository.delete(existingAccount);
    }
}
