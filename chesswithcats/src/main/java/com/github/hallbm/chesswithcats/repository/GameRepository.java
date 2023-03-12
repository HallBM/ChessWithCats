package com.github.hallbm.chesswithcats.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.github.hallbm.chesswithcats.model.Game;

public interface GameRepository extends JpaRepository<Game, Long>{
	
	@Query(value = "SELECT * FROM games g "
			+ "WHERE (g.white_username = :username OR g.black_username = :username) AND g.winner IS NULL "
			+ "ORDER BY g.id", nativeQuery = true)
	List<Game> getActiveByUsername(String username);
	
	@Query(value = "SELECT * FROM games g "
			+ "WHERE (g.white_username = :username OR g.black_username = :username) AND g.winner IS NOT NULL "
			+ "ORDER BY g.id DESC", nativeQuery = true)
	List<Game> getCompleteByUsername(String username);

	@Query(value = "SELECT * FROM games g "
			+ "WHERE (g.white_username = :username OR g.black_username = :username) "
			+ "AND g.winner IS NOT NULL "
			+ "ORDER BY g.style", nativeQuery = true)
	List<Game> getCompleteByUsernameOrderByStyle(String username);
	
	@Query(value = "SELECT * FROM games g "
			+ "WHERE ((g.white_username = :username1 AND g.black_username = :username2) OR "
			+ "(g.white_username = :username2 AND g.black_username = :username1)) "
			+ "AND g.winner IS NOT NULL "
			+ "ORDER BY g.style", nativeQuery = true)
	List<Game> getCompleteByOpponentsOrderByStyle(String username1, String username2);
	
	@Query(value = "SELECT * FROM games g "
			+ "WHERE g.winner IS NOT NULL AND g.winner <> 'Draw' AND g.style =:style", nativeQuery = true)
	Optional<List<Game>> getWonOrLostGamesByStyle(String style);
	
	
}
