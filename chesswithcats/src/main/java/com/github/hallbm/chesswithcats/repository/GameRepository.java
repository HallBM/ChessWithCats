package com.github.hallbm.chesswithcats.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.hallbm.chesswithcats.domain.GameEnums.GameOutcome;
import com.github.hallbm.chesswithcats.model.Game;

public interface GameRepository extends JpaRepository<Game, Long>{

	public List<Game> findByWhiteUsernameOrBlackUsernameAndOutcomeOrderByStyleAscIdDesc(String white, String black, GameOutcome outcome);
	public List<Game> findByWhiteUsernameOrBlackUsernameAndWinnerNotNullOrderByStyleAscIdDesc(String white, String black);


}
