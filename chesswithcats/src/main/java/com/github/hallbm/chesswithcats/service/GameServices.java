package com.github.hallbm.chesswithcats.service;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.hallbm.chesswithcats.dto.GameDTO;
import com.github.hallbm.chesswithcats.dto.GameRequestDTO;
import com.github.hallbm.chesswithcats.model.Game;
import com.github.hallbm.chesswithcats.model.Game.GameColor;
import com.github.hallbm.chesswithcats.model.Game.GameOutcome;
import com.github.hallbm.chesswithcats.model.Game.GameStyle;
import com.github.hallbm.chesswithcats.model.Game.GameWLD;
import com.github.hallbm.chesswithcats.model.Game.GameWinner;
import com.github.hallbm.chesswithcats.model.GameRequest;
import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.GameRepository;
import com.github.hallbm.chesswithcats.repository.GameRequestRepository;
import com.github.hallbm.chesswithcats.repository.PlayerRepository;

import jakarta.transaction.Transactional;

@Service
public class GameServices {

	@Autowired
	private GameRequestRepository gameReqRepo;
	
	@Autowired
	private GameRepository gameRepo;

	@Autowired
	private PlayerRepository playerRepo;
	
	@Transactional
	public GameRequestDTO createGameRequestDTO (GameRequest gameReq, String username) {
		
		GameRequestDTO gameReqDTO = new GameRequestDTO();
		gameReqDTO.setId(gameReq.getId());
		gameReqDTO.setTime(gameReq.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a")));
		gameReqDTO.setOpponent(username.equals(gameReq.getSender().getUsername()) ? gameReq.getReceiver().getUsername() : gameReq.getSender().getUsername());
		gameReqDTO.setStyle(gameReq.getStyle());
		return gameReqDTO;
	}

	@Transactional
	public GameDTO createGameDTO (Game game, String username) {

		GameDTO gameDTO = new GameDTO();
		boolean currentIsWhite = username.equals(game.getWhite().getUsername());
		gameDTO.setGameId(String.format("%06d",game.getGameId()));
		gameDTO.setColor(currentIsWhite ? GameColor.WHITE : GameColor.BLACK);
		gameDTO.setOpponent(currentIsWhite ? game.getBlack().getUsername() : game.getWhite().getUsername());
		gameDTO.setStyle(game.getStyle());
		gameDTO.setOutcome(game.getOutcome());
		if (gameDTO.getOutcome().equals(GameOutcome.INCOMPLETE)) {
			gameDTO.setTurn(/*currentIsWhite && game.getGamePlay().getMoveNumber % 2 == 0  TODO ///gameplay not ready yet///*/ false ? "Yours" : "Theirs");
		} else {
			gameDTO.setWinLoseDraw(game.getWinner().equals(GameWinner.DRAW) ? GameWLD.DRAW :
				(currentIsWhite && game.getWinner().equals(GameWinner.WHITE) ? GameWLD.WIN : GameWLD.LOSE));
		}
		
		return gameDTO;
	}
	
	@Transactional
	public Player findRandomOpponent(Player sender, GameStyle style) {
		
		List<Player> potentialOpponent = playerRepo.findTop20ByIsLoggedAndIsPlayingOrderByLastLoginDesc(true, false)
				.orElse(playerRepo.findTop20ByOrderByLastLoginDesc().orElse(null));
		
		if (potentialOpponent == null || potentialOpponent.size() == 1) {
			return null;
		}
		
		potentialOpponent.remove(sender);
		
        Random rand = new Random();
        int randIndex = rand.nextInt(potentialOpponent.size());
        return potentialOpponent.get(randIndex);
	}
	
}
