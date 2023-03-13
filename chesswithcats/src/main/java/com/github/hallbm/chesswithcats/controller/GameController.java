package com.github.hallbm.chesswithcats.controller;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameOutcome;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;
import com.github.hallbm.chesswithcats.dto.GameRequestDTO;
import com.github.hallbm.chesswithcats.dto.MoveDTO;
import com.github.hallbm.chesswithcats.dto.MoveResponseDTO;
import com.github.hallbm.chesswithcats.model.Game;
import com.github.hallbm.chesswithcats.model.GamePlay;
import com.github.hallbm.chesswithcats.model.GameRequest;
import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.GamePlayRepository;
import com.github.hallbm.chesswithcats.repository.GameRepository;
import com.github.hallbm.chesswithcats.repository.GameRequestRepository;
import com.github.hallbm.chesswithcats.repository.PlayerRepository;
import com.github.hallbm.chesswithcats.service.FriendServices;
import com.github.hallbm.chesswithcats.service.GamePlayServices;
import com.github.hallbm.chesswithcats.service.GameServices;

import jakarta.servlet.http.HttpSession;

/**
 * Controller related to CRUD for long-term storage of games played
 */

@Controller
public class GameController {

	@Autowired
	private PlayerRepository playerRepo;

	@Autowired
	private GameRequestRepository gameReqRepo;

	@Autowired
	private GameRepository gameRepo;

	@Autowired
	private GameServices gameServ;

	@Autowired
	private FriendServices friendServ;

	@Autowired
	private GamePlayServices gamePlayServ;

	@Autowired
	private GamePlayRepository gamePlayRepo;

	/**
	 * Generates and displays lists of games based on status of request
	 * (received request, pending request, active games (accepted, unfinished)
	 * and completed games) for display on the 'games' page.
	 */
	@GetMapping("/games")
	public String showGamesPage(Model model, @AuthenticationPrincipal Player currentUser) {

		model.addAttribute("receivedList", gameServ.getReceivedGameRequestDTOs(currentUser.getUsername()));
		model.addAttribute("pendingList", gameServ.getSentGameRequestDTOs(currentUser.getUsername()));
		model.addAttribute("activeList", gameServ.getActiveGameDTOs(currentUser.getUsername()));
		model.addAttribute("archiveList", gameServ.getCompletedGameDTOs(currentUser.getUsername()));

		model.addAttribute("gameStyles", GameStyle.values());
		model.addAttribute("friends", friendServ.getFriendUsernames(currentUser.getUsername()));
		model.addAttribute("gameReq", new GameRequestDTO());

		return "games";
	}

	/**
	 * Creates new game request. Player able to select a friend from a drop down menu,
	 * or select a 'random' player based on last login. 
	 */
	@PostMapping("/gameRequest")
	public String handleGameRequest(Model model, @ModelAttribute("gameReq") GameRequestDTO gameReq,
			BindingResult result, @AuthenticationPrincipal Player currentUser) throws SQLException {

		GameRequest newReq = new GameRequest();

		Player sender = playerRepo.findByUsername(currentUser.getUsername());
		Player receiver;

		if (gameReq.getOpponent().equals("1")) {
			
			receiver = gameServ.findRandomOpponent(sender, gameReq.getStyle());

			if (receiver == null) {
				return "redirect:/games";
			}
		
		} else {
			receiver = playerRepo.findByUsername(gameReq.getOpponent());
		}

		newReq.setSender(sender);
		newReq.setReceiver(receiver);
		newReq.setStyle(gameReq.getStyle());
		gameReqRepo.save(newReq); 

		return "redirect:/games";
			
		
	}
	
	/**
	 * Decline game request (deletes record). 
	 */
	@ResponseBody
	@PostMapping("/gameRequest/decline/{id}")
	public ModelAndView declineGameRequest(@PathVariable("id") Long id) {

		gameReqRepo.deleteById(id);

		return new ModelAndView("redirect:/games");
	}

	/**
	 * Forfeit game. If no game moves were made, simply deletes record.
	 * If the game already started, marks the game complete and counts
	 * as a loss for the forfeiting party. 
	 */
	@PostMapping("/game/forfeit/{id}")
	public String forfeit(@PathVariable("id") String id, @AuthenticationPrincipal Player currentUser) {

		gameServ.forfeitGame(Long.parseLong(id), currentUser.getUsername());

		return "redirect:/games";
	}

	/**
	 * Request a draw.
	 * 
	 * Currently written to force draw for POC. TODO Need to implement logic for
	 * requesting acceptance of a requested draw.
	 */
	@PostMapping("/game/draw/{id}")
	public String initiateDraw(@PathVariable("id") String id, @AuthenticationPrincipal Player currentUser) {

		gameServ.drawGame(Long.parseLong(id), currentUser.getUsername());

		return "redirect:/games";
	}

	/**
	 * Accepting a game request generates a new game, assigns random color (black / white)
	 * and redirects user to the chess game. 
	 */
	@PostMapping("/gameRequest/accept/{id}/{style}/{opponent}")
	public String acceptGameRequest(@PathVariable long id, @PathVariable GameStyle style, @PathVariable String opponent,
			@AuthenticationPrincipal Player currentUser) {

		Game newGame = gameServ.createGameFromRequest(id, style, opponent);
		return "redirect:/game/" + newGame.getStyle().toString().toLowerCase() + "/"
				+ String.format("%06d", newGame.getId());
	}

	/**
	 * Endpoint for entering a game, where the player will be redirected to their 
	 * appropriate color if they are part of the game, or if not, redirected to game page.
	 */
	@GetMapping("/game/{style}/{id}")
	public String retrieveGame(Model model, @PathVariable String style, @PathVariable String id,
			@AuthenticationPrincipal Player currentUser) {
		Game game = gameRepo.findById(Long.parseLong(id)).orElse(null);

		if (game == null) {
			return "redirect:/games";
		} else {
			String playerColor = game.getWhite().getUsername().equals(currentUser.getUsername()) ? "white" : "black";
			return "redirect:/game/" + style.toLowerCase() + "/" + id + "/" + playerColor;
		}
	}

	/**
	 * Final endpoint for entering the game. The color is interpreted by front end
	 * to affect the way the board is displayed (from white or black player view).
	 * Other model attributes included to direct game logic and info display, such as 
	 * who's turn it is.
	 */
	@GetMapping("/game/{style}/{id}/{color}")
	public String enterGame(Model model, @PathVariable String style, @PathVariable String id,
			@PathVariable String color, @AuthenticationPrincipal Player currentUser) throws JsonProcessingException {

		Game game = gameRepo.findById(Long.parseLong(id)).orElse(null);
		if (game == null || game.getWinner() != null) {
			return "redirect:/games";
		}

		String playerColor = game.getWhite().getUsername().equals(currentUser.getUsername()) ? "white" : "black";

		if (!color.equals(playerColor)) {
			return "redirect:/game/" + style + "/" + id;
		}

		ObjectMapper objectMapper = new ObjectMapper();
		String pieceMapJson = objectMapper.writeValueAsString(game.getGamePlay().getGameBoard().getPieceMap());

		model.addAttribute("pieceMapJson", pieceMapJson);
		model.addAttribute("color", "render_" + playerColor);
		model.addAttribute("turn", game.getGamePlay().getHalfMoves() % 2 == 0 ? "black-turn" : "white-turn");
		model.addAttribute("whitePlayer", game.getWhite().getUsername());
		model.addAttribute("blackPlayer", game.getBlack().getUsername());
		model.addAttribute("whiteOnline", game.getWhite().isLogged());
		model.addAttribute("blackOnline", game.getBlack().isLogged());
		model.addAttribute("moves", game.getGamePlay().getMoveString());
		model.addAttribute("moves2", game.getGamePlay().getMoves().toString());

		return "chessboard";
	}
	
	/**
	 * AJAX for making a move, with move validation prior to acceptance of move.
	 * Moves are filtered on the front end via javascript to only allow moves where 
	 * pieces are dropped either on empty squares or player pieces of the opposite color.
	 * All other game logic handled at this end point for validating the move.
	 * 
	 * Move validation occurs in two steps.
	 * 1) GamePlayServices evaluates moveDTO (from front end) and current state of game (gamePlay).
	 * This function attempts to generate a MoveResponseDTO object that contains inferences about
	 * the move. If the move is plausible (according to rules of how pieces move), and before and after the move,
	 * the user is not in check, the move proceeds to the next function, where the move is persisted.
	 * 2) GamePlayServices persists the move and updates game state via the finalizeAndSaveGameState function,
	 * which uses the moveDTO, gamePlay and generated moveResponseDTO.
	 * 
	 * TODO Other than checkmate, other endings need to be implemented, such as 50 move rule, stalemate, etc.
	 * 
	 * For "ambiguous" games, implemented a session attribute for tracking the number of failed moves
	 * (likely, given that all the pieces look the same and the player may have lost track of
	 * what piece they are currently trying to move. User gets 3 attempts to make a move before their move is forfeited. 
	 */
	@ResponseBody
	@PostMapping("/game/move")
	public ResponseEntity<MoveResponseDTO> getUserByUserName(@RequestBody MoveDTO moveDTO,
			@AuthenticationPrincipal Player currentUser, HttpSession session) {
	
		GamePlay gamePlay = gamePlayRepo.findByGameId(Long.parseLong(moveDTO.getGameId()));
		MoveResponseDTO moveResponseDTO = gamePlayServ.validateMove(moveDTO, gamePlay).orElse(new MoveResponseDTO());

		if (GameStyle.valueOf(moveDTO.getGameStyle().toUpperCase()) == GameStyle.AMBIGUOUS) {
			String attemptsKey = "attempts_" + moveDTO.getGameId() + "_" + currentUser.getUsername();
			Integer chancesRemaining = (Integer) session.getAttribute(attemptsKey);
			
			if (chancesRemaining == null) {
				chancesRemaining = 2;
				session.setAttribute(attemptsKey, chancesRemaining);
			}
			
			if (moveResponseDTO.isValid()) {
				session.setAttribute(attemptsKey, 2);
			}else if (chancesRemaining > 0) {
				session.setAttribute(attemptsKey, --chancesRemaining);
			}else {
				moveResponseDTO.setOfficialChessMove("XXX ");
				gamePlay.incrementFiftyMoveClock();
				gamePlay.addMove(moveResponseDTO.getOfficialChessMove()); // updates move for both String and StringBuffer
				gamePlay.incrementHalfMoves(); 
				gamePlay.updateFenSet();
				gamePlayRepo.save(gamePlay);
				session.setAttribute(attemptsKey, 2);
			}
		}

		if (moveResponseDTO.isValid()) {
			gamePlayServ.finalizeAndSaveGameState(moveDTO, moveResponseDTO, gamePlay);
		}

		// TODO: POC: check ==> checkmate; update with real implementation of checking
		// checkmate/stalemate

		if (moveResponseDTO.getGameOutcome() == GameOutcome.CHECKMATE) {
			Game activeGame = gamePlay.getGame();
			activeGame.setOutcome(moveResponseDTO.getGameOutcome());
			activeGame.setWinner(gamePlay.getHalfMoves() % 2 == 0 ? activeGame.getWhite().getUsername()
					: activeGame.getBlack().getUsername());
			activeGame.setMoves(activeGame.getGamePlay().getMoveString());
			activeGame.setGamePlay(null);
			gameRepo.save(activeGame);
		}

		return new ResponseEntity<MoveResponseDTO>(moveResponseDTO, HttpStatus.OK);
	}

}
