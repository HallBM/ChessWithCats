package com.github.hallbm.chesswithcats.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;
import com.github.hallbm.chesswithcats.model.Game;

public interface GameRepository extends JpaRepository<Game, Long>{
	
	@Query(value = "SELECT * FROM games g "
			+ "WHERE (g.white_username = :username OR g.black_username = :username) AND g.winner IS NULL "
			+ "ORDER BY g.id", nativeQuery = true)
	List<Game> findActiveByUsername(String username);
	
	@Query(value = "SELECT * FROM games g "
			+ "WHERE (g.white_username = :username OR g.black_username = :username) AND g.winner IS NOT NULL "
			+ "ORDER BY g.id", nativeQuery = true)
	List<Game> findCompleteByUsername(String username);

	@Query(value = "SELECT g.style, g.outcome FROM games g "
			+ "WHERE (g.white_username = :username1 AND g.black_username = :username2) OR "
			+ "(g.white_username = username2 AND g.black_username = :username1)", nativeQuery = true)
	List<Game> findCompleteByOpponents(String username1, String username2);

}
