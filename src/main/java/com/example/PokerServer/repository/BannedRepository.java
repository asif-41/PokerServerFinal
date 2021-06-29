package com.example.PokerServer.repository;

import com.example.PokerServer.model.Banned;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BannedRepository extends JpaRepository<Banned,Integer> {

}

