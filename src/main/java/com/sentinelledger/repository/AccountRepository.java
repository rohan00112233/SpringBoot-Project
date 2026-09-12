package com.sentinelledger.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sentinelledger.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
}