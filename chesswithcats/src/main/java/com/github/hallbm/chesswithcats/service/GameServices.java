package com.github.hallbm.chesswithcats.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import com.github.hallbm.chesswithcats.domain.GameEnums.GameColor;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameOutcome;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameWLD;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameWinner;
import com.github.hallbm.chesswithcats.dto.GameDTO;
import com.github.hallbm.chesswithcats.dto.GameRequestDTO;
import com.github.hallbm.chesswithcats.model.Game;
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

	@Autowired
	private GamePlayServices gamePlayServ;
	
	@Transactional
	public List<GameRequestDTO> getReceivedGameRequestDTOs (String username) {
		List<GameRequest> receivedList = gameReqRepo.findByReceiverUsernameOrderByStyleAscCreatedAtDesc(username);
		List<GameRequestDTO> receivedListDTO =
				receivedList.stream().map(game -> createGameRequestDTO(game, username)).collect(Collectors.toList());
		return receivedListDTO;
	}
	
	@Transactional
	public List<GameRequestDTO> getSentGameRequestDTOs (String username) {
		List<GameRequest> pendingList = gameReqRepo.findBySenderUsernameOrderByStyleAscCreatedAtDesc(username);
		List<GameRequestDTO> pendingListDTO =
				pendingList.stream().map(game -> createGameRequestDTO(game, username)).collect(Collectors.toList());
		return pendingListDTO;
	}
	
	@Transactional
	public List<GameDTO> getActiveGameDTOs (String username) {
		List<Game> activeList = gameRepo.findActiveByUsername(username);
		List<GameDTO> activeListDTO =
				activeList.stream().map(game -> createGameDTO(game, username)).collect(Collectors.toList());
		return activeListDTO;
	}
	
	@Transactional
	public List<GameDTO> getCompletedGameDTOs (String username) {
		List<Game> completedList = gameRepo.findCompleteByUsername(username);
		List<GameDTO> completedListDTO =
				completedList.stream().map(game -> createGameDTO(game, username)).collect(Collectors.toList());
		return completedListDTO;
	}
	
	@Transactional
	public GameRequestDTO createGameRequestDTO (GameRequest gameReq, String username) {

		GameRequestDTO gameReqDTO = new GameRequestDTO();
		gameReqDTO.setId(String.valueOf(gameReq.getId()));
		gameReqDTO.setTime(gameReq.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a")));
		gameReqDTO.setOpponent(username.equals(gameReq.getSender().getUsername()) ? gameReq.getReceiver().getUsername() : gameReq.getSender().getUsername());
		gameReqDTO.setStyle(gameReq.getStyle());
		return gameReqDTO;
	}

	@Transactional
	public GameDTO createGameDTO (Game game, String username) {

		if (game == null) {
			return null;
		}
		
		GameDTO gameDTO = new GameDTO();
		boolean isCurrentWhite = username.equals(game.getWhite().getUsername());
		gameDTO.setId(String.format("%06d",game.getId()));
		gameDTO.setColor(isCurrentWhite ? GameColor.WHITE : GameColor.BLACK);
		gameDTO.setOpponent(isCurrentWhite ? game.getBlack().getUsername() : game.getWhite().getUsername());
		gameDTO.setStyle(game.getStyle());
		gameDTO.setOutcome(game.getOutcome());
		if (gameDTO.getOutcome() == GameOutcome.ACCEPTED || gameDTO.getOutcome() == GameOutcome.INCOMPLETE) {
			boolean isWhiteTurn = game.getGamePlay().getHalfMoves() % 2 == 1 ? true : false;
			gameDTO.setTurn((isCurrentWhite == isWhiteTurn) ? "YOURS" : "THEIRS");
		} else {
			if (game.getWinner() == GameWinner.DRAW) {
				gameDTO.setWinLoseDraw(GameWLD.DRAW);
			} else if (isCurrentWhite) {
				gameDTO.setWinLoseDraw(game.getWinner() == GameWinner.WHITE ? GameWLD.WIN : GameWLD.LOSE);
			} else {
				gameDTO.setWinLoseDraw(game.getWinner() != GameWinner.WHITE ? GameWLD.WIN : GameWLD.LOSE);
			}
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

	@Modifying
	@Transactional
	public void forfeitGame(Long id, String username) {
		Game activeGame = gameRepo.findById(id).get();

		if (activeGame.getOutcome() == GameOutcome.ACCEPTED) {
			gameRepo.delete(activeGame);
		} else {
			activeGame.setOutcome(GameOutcome.RESIGNATION);
			activeGame.setWinner(activeGame.getWhite().getUsername().equals(username) ? GameWinner.BLACK : GameWinner.WHITE);
			activeGame.setMoves(activeGame.getGamePlay().getMoveString());
			activeGame.setGamePlay(null);
			gameRepo.save(activeGame);
		}
	}

	@Modifying
	@Transactional
	public Game createGameFromRequest(Long requestId, GameStyle style, String opponentUsername) {

		Game newGame = new Game();
		newGame.getGamePlay().setGame(newGame);

		GameRequest gameReq = gameReqRepo.findById(requestId).get();
		newGame.setStyle(gameReq.getStyle());

		double randNum = Math.random();

		if (randNum < 0.5) {
			newGame.setWhite(gameReq.getSender());
			newGame.setBlack(gameReq.getReceiver());
		} else {
			newGame.setWhite(gameReq.getReceiver());
			newGame.setBlack(gameReq.getSender());
		}

		// only include if opponent is not human: newGame.setOpponentIsHuman(false);
		
		GameBoardServices.setupGameBoard(newGame);
		String openingFen = newGame.getGamePlay().updateFenSet();
		newGame.setOpeningFen(openingFen);

		gameRepo.save(newGame);
		gameReqRepo.delete(gameReq);

		return newGame;
	}

	@Transactional
	public Map<GameStyle, Integer[]> getWinLoseDrawByPlayer (String username){
		Map<GameStyle, Integer[]> stats = new HashMap<>();

		for(GameStyle gs : GameStyle.values()) {
			stats.put(gs,new Integer[] {0,0,0,0}); //TWLD
		}
		
		List<Game> databaseResults = gameRepo.findCompleteByUsername(username);
		
		for (Game game : databaseResults) {
			GameStyle gs = game.getStyle();
			stats.get(gs)[0] += 1;
			if (game.getWinner() == GameWinner.DRAW) {
				stats.get(gs)[3] += 1;
			} else if (game.getWinner() == GameWinner.WHITE) {
				if (game.getWhite().getUsername().equals(username)) {
					stats.get(gs)[1] += 1;
				}else {
					stats.get(gs)[2] += 1;
				}
			} else {
				if (game.getBlack().getUsername().equals(username)) {
					stats.get(gs)[1] += 1;
				}else {
					stats.get(gs)[2] += 1;
				}
			}
		}
		return stats;
	} 
}
