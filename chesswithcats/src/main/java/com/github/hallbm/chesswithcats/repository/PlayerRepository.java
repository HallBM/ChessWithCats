package com.github.hallbm.chesswithcats.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.hallbm.chesswithcats.model.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    public Player findByUsername(String username);
    public Player findByEmail(String email);

    @Query("SELECT p.username FROM Player p WHERE p.username LIKE :search ORDER BY p.username ASC LIMIT 50")
    public Optional<List<String>> searchTenNewFriends(@Param("search") String search); 
    
    public boolean existsByUsername(String username);
    
}
