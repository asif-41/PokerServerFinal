package com.example.PokerServer.repository;

import com.example.PokerServer.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundRepository extends JpaRepository<Refund,Integer> {

}

