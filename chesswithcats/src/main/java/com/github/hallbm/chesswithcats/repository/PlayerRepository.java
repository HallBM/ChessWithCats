package com.github.hallbm.chesswithcats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.hallbm.chesswithcats.model.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    public Player findByUsername(String username);
    public Player findByEmail(String email);
}
