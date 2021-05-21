package com.example.PokerServer.repository;

import com.example.PokerServer.model.PendingRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PendingRefundRepository extends JpaRepository<PendingRefund,Integer> {

}

