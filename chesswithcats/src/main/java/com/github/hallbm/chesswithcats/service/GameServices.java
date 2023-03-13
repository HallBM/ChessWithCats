package com.github.hallbm.chesswithcats.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.github.hallbm.chesswithcats.dto.GameDTO;
import com.github.hallbm.chesswithcats.dto.GameRequestDTO;
import com.github.hallbm.chesswithcats.model.Game;
import com.github.hallbm.chesswithcats.model.GameRequest;
import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.GameRepository;
import com.github.hallbm.chesswithcats.repository.GameRequestRepository;
import com.github.hallbm.chesswithcats.repository.PlayerRepository;

/**
 * Services associated with CRUD for GameRequests long-term persisted Games. 
 */

@Service
public class GameServices {

	@Autowired
	private GameRequestRepository gameReqRepo;

	@Autowired
	private GameRepository gameRepo;

	@Autowired
	private PlayerRepository playerRepo;

	/**
	 * Returns GameRequestDTO created from pending gameRequests sent from @Param username for front end display 
	 */
	public List<GameRequestDTO> getReceivedGameRequestDTOs(String username) {
		List<GameRequest> receivedList = gameReqRepo.findByReceiverUsernameOrderByStyleAscCreatedAtDesc(username);
		List<GameRequestDTO> receivedListDTO = receivedList.stream().map(game -> createGameRequestDTO(game, username))
				.collect(Collectors.toList());
		return receivedListDTO;
	}
	
	/**
	 * Returns GameRequestDTO created from pending gameRequests sent by @Param username for front end display
	 */
	public List<GameRequestDTO> getSentGameRequestDTOs(String username) {
		List<GameRequest> pendingList = gameReqRepo.findBySenderUsernameOrderByStyleAscCreatedAtDesc(username);
		List<GameRequestDTO> pendingListDTO = pendingList.stream().map(game -> createGameRequestDTO(game, username))
				.collect(Collectors.toList());
		return pendingListDTO;
	}

	/**
	 * Returns GameDTO created from active games with @Param username listed as a player for front end display
	 */
	public List<GameDTO> getActiveGameDTOs(String username) {
		List<Game> activeList = gameRepo.getActiveByUsername(username);
		List<GameDTO> activeListDTO = activeList.stream().map(game -> createGameDTO(game, username))
				.collect(Collectors.toList());
		return activeListDTO;
	}
	
	/**
	 * Returns GameDTO created from completed games with @Param username listed as a player for front end display
	 */
	public List<GameDTO> getCompletedGameDTOs(String username) {
		List<Game> completedList = gameRepo.getCompleteByUsername(username);
		List<GameDTO> completedListDTO = completedList.stream().map(game -> createGameDTO(game, username))
				.collect(Collectors.toList());
		return completedListDTO;
	}

	/**
	 * Helper function for converting persisted GameRequest into GameRequestDTO 
	 */
	private GameRequestDTO createGameRequestDTO(GameRequest gameReq, String username) {

		GameRequestDTO gameReqDTO = new GameRequestDTO();
		gameReqDTO.setId(String.valueOf(gameReq.getId()));
		gameReqDTO.setTime(gameReq.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a")));
		gameReqDTO.setOpponent(username.equals(gameReq.getSender().getUsername()) ? gameReq.getReceiver().getUsername()
				: gameReq.getSender().getUsername());
		gameReqDTO.setStyle(gameReq.getStyle());
		return gameReqDTO;
	}

	/**
	 * Helper function for converting persisted Game into GameDTO 
	 */
	private GameDTO createGameDTO(Game game, String username) {

		if (game == null) {
			return null;
		}

		GameDTO gameDTO = new GameDTO();
		boolean isCurrentWhite = username.equals(game.getWhite().getUsername());
		gameDTO.setId(String.format("%06d", game.getId()));
		gameDTO.setColor(isCurrentWhite ? GameColor.WHITE : GameColor.BLACK);
		gameDTO.setOpponent(isCurrentWhite ? game.getBlack().getUsername() : game.getWhite().getUsername());
		gameDTO.setStyle(game.getStyle());
		gameDTO.setOutcome(game.getOutcome());
		if (gameDTO.getOutcome() == GameOutcome.ACCEPTED || gameDTO.getOutcome() == GameOutcome.INCOMPLETE) {
			boolean isWhiteTurn = game.getGamePlay().getHalfMoves() % 2 == 1 ? true : false;
			gameDTO.setTurn((isCurrentWhite == isWhiteTurn) ? "YOURS" : "THEIRS");
		} else {
			if (game.getWinner().equals("Draw")) {
				gameDTO.setWinLoseDraw(GameWLD.DRAW);
			} else {
				gameDTO.setWinLoseDraw(game.getWinner().equals(username) ? GameWLD.WIN : GameWLD.LOSE);
			}
		}
		return gameDTO;
	}

	/**
	 * Method for matching user with a random player.
	 * List of random players prioritized by last login, with preference towards those logged in but not actively playing.
	 * Returns random player or null if no players can be found. 
	 */
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

	/**
	 * Method for forfeiting game.
	 * If no moves are made, game is simply deleted.
	 * Otherwise, game outcome updated as 'resignation', and 
	 * player requesting forfeit take a loss, and opponent take a win.
	 */
	@Modifying
	public void forfeitGame(Long id, String username) {
		Game activeGame = gameRepo.findById(id).get();

		if (activeGame.getOutcome() == GameOutcome.ACCEPTED) {
			gameRepo.delete(activeGame);
		} else {
			activeGame.setOutcome(GameOutcome.RESIGNATION);

			if (activeGame.getWhite().getUsername().equals(username)) {
				activeGame.setWinner(activeGame.getBlack().getUsername());
			} else {
				activeGame.setWinner(activeGame.getWhite().getUsername());
			}

			activeGame.setMoves(activeGame.getGamePlay().getMoveString());
			activeGame.setGamePlay(null);
			gameRepo.save(activeGame);
		}
	}
	
	/**
	 * Method for draw of game.
	 * TODO Game currently forces acceptance of a draw request from the opponent, but
	 * need to implement logic for draw to be accepted. Also, need to implement other
	 * methods for reaching a draw, like stalemate, 50 move rule, etc.
	 */
	@Modifying
	public void drawGame(Long id, String username) {
		Game activeGame = gameRepo.findById(id).get();
		activeGame.setOutcome(GameOutcome.AGREEMENT);
		activeGame.setWinner("Draw");
		activeGame.setMoves(activeGame.getGamePlay().getMoveString());
		activeGame.setGamePlay(null);
		gameRepo.save(activeGame);
	}

	/**
	 * Method creating game from a game request.
	 * Persists newly created game and deletes game request used to make game.
	 * Returns new game.
	 */
	@Modifying
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

		// TODO only include if opponent is not human: newGame.setOpponentIsHuman(false);

		GameBoardServices.setupGameBoard(newGame);
		String openingFen = newGame.getGamePlay().updateFenSet();
		newGame.setOpeningFen(openingFen);

		gameRepo.save(newGame);
		gameReqRepo.delete(gameReq);

		return newGame;
	}

	/**
	 * Method for generating game statistics for user's own profile page.
	 * Calculates Total games, and Win,Lose,Draw percentages of completed games for user
	 * Returns HashMap of GameStyle and an integer array of [Total games, Win%, Draw%, Lose%]
	 */
	public Map<GameStyle, Integer[]> getWinDrawLosePercentageByPlayer(String username) {
		return getWinDrawLosePercentage(gameRepo.getCompleteByUsernameOrderByStyle(username), username);
	}
	
	/**
	 * Method for generating game statistics for other user's profile page.
	 * Calculates Total games, and Win,Lose,Draw percentages of completed games
	 * in which both indicated users have played against each other
	 * Returns HashMap of GameStyle and an integer array of [Total games, Win%, Draw%, Lose%]
	 */
	public Map<GameStyle, Integer[]> getWinDrawLosePercentageByOpponents(String currentUsername, String username2) {
		return getWinDrawLosePercentage(gameRepo.getCompleteByOpponentsOrderByStyle(currentUsername, username2),
				currentUsername);
	}
	
	/**
	 * Helper function for calculating total games, and win,lose,draw percentages.
	 * Returns HashMap of GameStyle and an integer array of [Total games, Win%, Draw%, Lose%]
	 */
	public Map<GameStyle, Integer[]> getWinDrawLosePercentage(List<Game> databaseResults, String currentUsername) {

		Map<GameStyle, Integer[]> stats = new HashMap<>();

		for (GameStyle gs : GameStyle.values()) {
			stats.put(gs, new Integer[] { 0, 0, 0, 0 }); // TWDL
		}
		for (Game game : databaseResults) {
			GameStyle gs = game.getStyle();
			stats.get(gs)[0] += 1;
			if (game.getWinner().equals("Draw")) {
				stats.get(gs)[2] += 1;
			} else if (game.getWinner().equals(currentUsername)) {
				stats.get(gs)[1] += 1;
			} else {
				stats.get(gs)[3] += 1;
			}
		}

		for (GameStyle gs : stats.keySet()) {
			if (stats.get(gs)[0] == 0) {
				continue;
			}
			stats.get(gs)[1] = Math.round((float) stats.get(gs)[1] / (float) stats.get(gs)[0] * 100);
			stats.get(gs)[2] = Math.round((float) stats.get(gs)[2] / (float) stats.get(gs)[0] * 100);
			stats.get(gs)[3] = Math.round((float) stats.get(gs)[3] / (float) stats.get(gs)[0] * 100);
		}
		return stats;
	}
	
	/**
	 * Method for calculating leaderboard stats, i.e., top 5 players for a given game style.
	 * Player scores calculated by total wins minus total losses per game style (WIN +1, DRAW 0, LOSS -1). 
	 * @Param GameStyle 
	 * Returns an ArrayList of up to 5 Object[], representing [Player, score]
	 */
	public List<Object[]> getTopPlayers(GameStyle style) {
		List<Game> games = gameRepo.getWonOrLostGamesByStyle(style.toString()).orElse(null);

		if (games == null) {
			return null;
		}

		Map<String, Integer> rankMap = new HashMap<>();

		for (Game game : games) {
			String white = game.getWhite().getUsername();
			String black = game.getBlack().getUsername();
			String winner = game.getWinner();

			rankMap.putIfAbsent(white, 0);
			rankMap.putIfAbsent(black, 0);

			if (white.equals(winner)) {
				rankMap.put(white, rankMap.get(white) + 1);
				rankMap.put(black, rankMap.get(black) - 1);
			} else if (black.equals(winner)) {
				rankMap.put(white, rankMap.get(white) - 1);
				rankMap.put(black, rankMap.get(black) + 1);
			} else {

			}
		}

		List<Map.Entry<String, Integer>> top5Players = new ArrayList<>(rankMap.entrySet());

		Collections.sort(top5Players, new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		List<Object[]> ranking = new ArrayList<>();

		int index = 0;
		for (Map.Entry<String, Integer> rank : top5Players.subList(0, Math.min(top5Players.size(), 5))) {
			index++;
			ranking.add(new Object[] { index, rank.getKey(), rank.getValue() });
		}

		return ranking;

	}
}
