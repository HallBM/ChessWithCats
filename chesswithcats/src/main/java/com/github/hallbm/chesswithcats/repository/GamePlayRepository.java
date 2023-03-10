package com.github.hallbm.chesswithcats.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.hallbm.chesswithcats.model.GamePlay;

public interface GamePlayRepository extends JpaRepository<GamePlay, Long>{

	public GamePlay findByGameId(Long gameId);
}
