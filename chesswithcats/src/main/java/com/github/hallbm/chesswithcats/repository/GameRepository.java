package com.github.hallbm.chesswithcats.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.hallbm.chesswithcats.model.Game;
import com.github.hallbm.chesswithcats.model.Game.GameOutcome;

public interface GameRepository extends JpaRepository<Game, Long>{

	public List<Game> findByWhiteUsernameOrBlackUsernameAndOutcomeOrderByStyleAscGameIdDesc(String white, String black, GameOutcome outcome);
	public List<Game> findByWhiteUsernameOrBlackUsernameAndWinnerNullOrderByStyleAscGameIdDesc(String white, String black);
	public Game findByGameId(Long gameId);
	
}
