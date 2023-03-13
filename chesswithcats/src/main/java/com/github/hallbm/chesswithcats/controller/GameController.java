package com.github.hallbm.chesswithcats.controller;

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

	@PostMapping("/gameRequest")
	public String handleGameRequest(Model model, @ModelAttribute("gameReq") GameRequestDTO gameReq,
			BindingResult result, @AuthenticationPrincipal Player currentUser) {

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

	@ResponseBody
	@PostMapping("/gameRequest/decline/{id}")
	public ModelAndView declineGameRequest(@PathVariable("id") Long id) {

		gameReqRepo.deleteById(id);

		return new ModelAndView("redirect:/games");
	}

	@PostMapping("/game/forfeit/{id}")
	public String forfeit(@PathVariable("id") String id, @AuthenticationPrincipal Player currentUser) {

		gameServ.forfeitGame(Long.parseLong(id), currentUser.getUsername());

		return "redirect:/games";
	}

	@PostMapping("/game/draw/{id}")
	public String initiateDraw(@PathVariable("id") String id, @AuthenticationPrincipal Player currentUser) {

		gameServ.drawGame(Long.parseLong(id), currentUser.getUsername());

		return "redirect:/games";
	}

	@PostMapping("/gameRequest/accept/{id}/{style}/{opponent}")
	public String acceptGameRequest(@PathVariable long id, @PathVariable GameStyle style, @PathVariable String opponent,
			@AuthenticationPrincipal Player currentUser) {

		Game newGame = gameServ.createGameFromRequest(id, style, opponent);
		return "redirect:/game/" + newGame.getStyle().toString().toLowerCase() + "/"
				+ String.format("%06d", newGame.getId());
	}

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
