package com.sentinelledger.service;
import com.sentinelledger.exception.ResourceNotFoundException;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sentinelledger.entity.Account;
import com.sentinelledger.repository.AccountRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found with id: " + id));
    }
    
    public Account updateAccount(Long id, Account updatedAccount) {
    	
    	Account existingAccount = accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account not found"+ id) );
    	
    	existingAccount.setAccountHolderName(updatedAccount.getAccountHolderName());
    	
    	existingAccount.setAccountHolderName(updatedAccount.getAccountHolderName());
    	
    	existingAccount.setBalance(updatedAccount.getBalance());
    	
    	return accountRepository.save(existingAccount);
    }

	public void deleteAccount(long id) {
		Account existingAccount = accountRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Account not found with id:" + id));
		
		accountRepository.delete(existingAccount);
		// TODO Auto-generated method stub
		
	}
}