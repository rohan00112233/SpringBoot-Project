package com.sentinelledger.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sentinelledger.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySenderAccountId(Long senderAccountId);

    List<Transaction> findByReceiverAccountId(Long receiverAccountId);

    List<Transaction> findBySenderAccountIdOrReceiverAccountId(
            Long senderAccountId,
            Long receiverAccountId);
}