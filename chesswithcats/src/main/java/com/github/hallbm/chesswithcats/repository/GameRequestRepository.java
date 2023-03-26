package com.github.hallbm.chesswithcats.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;
import com.github.hallbm.chesswithcats.model.GameRequest;
import com.github.hallbm.chesswithcats.model.Player;

public interface GameRequestRepository extends JpaRepository<GameRequest, Long>{

	public List<GameRequest> findByReceiverUsernameOrderByStyleAscCreatedAtDesc(String receiver);
	public List<GameRequest> findBySenderUsernameOrderByStyleAscCreatedAtDesc(String receiver);
	public void deleteById(Long id);
	public boolean existsBySenderUsernameAndReceiverUsernameAndStyle(String sender, String receiver, GameStyle style);

	public boolean existsByReceiverUsername(String receiver);
	
	public List<GameRequest> findBySenderAndStyle(Player sender, GameStyle style);
	
	@Query("SELECT g.receiver FROM GameRequest g WHERE g.sender = :sender AND style = :style")
	public Set<Player> getReceiverBySenderAndStyle(Player sender, GameStyle style);
}
