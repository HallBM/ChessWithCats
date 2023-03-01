package com.github.hallbm.chesswithcats.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.hallbm.chesswithcats.model.Game.GameStyle;
import com.github.hallbm.chesswithcats.model.GameRequest;
import com.github.hallbm.chesswithcats.model.Player;

public interface GameRequestRepository extends JpaRepository<GameRequest, Long>{

	public List<GameRequest> findByReceiverUsernameOrderByStyleAscCreatedAtDesc(String receiver);
	public List<GameRequest> findBySenderUsernameOrderByStyleAscCreatedAtDesc(String receiver);
	public void deleteById(Long id);	
	public boolean existsBySenderUsernameAndReceiverUsernameAndStyle(String sender, String receiver, GameStyle style);
	
	public List<GameRequest> findBySenderAndStyle(Player sender, GameStyle style);
}
