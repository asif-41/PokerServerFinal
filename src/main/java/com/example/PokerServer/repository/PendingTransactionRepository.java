package com.example.PokerServer.repository;

import com.example.PokerServer.model.PendingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PendingTransactionRepository extends JpaRepository<PendingTransaction,Integer> {

}

