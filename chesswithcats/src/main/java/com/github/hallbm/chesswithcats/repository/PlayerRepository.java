package com.github.hallbm.chesswithcats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.hallbm.chesswithcats.model.Player;

//@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    public Player findByUsername(String username);
    public Player findByEmail(String email);
    
    @Query("SELECT player.username FROM Player player where player.username =:username")
    public String findUsername(@Param("username") String username);
    
    @Query("SELECT player.email FROM Player player where player.email =:email")
    public String findEmail(@Param("email") String email);
}
