package com.github.hallbm.chesswithcats.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.hallbm.chesswithcats.model.Player;

import jakarta.transaction.Transactional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    public Player findByUsername(String username);
    public Player findByEmail(String email);

    public Set<Player> findTop20ByOrderByLastLoginDesc();

    @Query("SELECT p.username FROM Player p WHERE p.username LIKE :search ORDER BY p.username ASC")
    public Optional<List<String>> searchNewFriends(@Param("search") String search);

    public boolean existsByUsername(String username);

    @Modifying
    @Transactional
    @Query("UPDATE Player p SET p.isOnline = false WHERE p.username = :username")
    public void logoutPlayerByUsername(@Param("username") String username);

}
