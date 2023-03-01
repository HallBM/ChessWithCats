package com.github.hallbm.chesswithcats.controller;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.github.hallbm.chesswithcats.dto.GameDTO;
import com.github.hallbm.chesswithcats.dto.GameRequestDTO;
import com.github.hallbm.chesswithcats.model.Game;
import com.github.hallbm.chesswithcats.model.Game.GameOutcome;
import com.github.hallbm.chesswithcats.model.Game.GameStyle;
import com.github.hallbm.chesswithcats.model.Game.GameWinner;
import com.github.hallbm.chesswithcats.model.GameRequest;
import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.GameRepository;
import com.github.hallbm.chesswithcats.repository.GameRequestRepository;
import com.github.hallbm.chesswithcats.repository.PlayerRepository;
import com.github.hallbm.chesswithcats.service.FriendServices;
import com.github.hallbm.chesswithcats.service.GameServices;

import jakarta.transaction.Transactional;

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
	
	@GetMapping("/games")
	public String showGamesPage(Model model, @AuthenticationPrincipal Player currentUser) {

		List<GameRequest> receivedList = gameReqRepo.findByReceiverUsernameOrderByStyleAscCreatedAtDesc(currentUser.getUsername());
		List<GameRequestDTO> receivedListDTO = 
				receivedList.stream().map(game -> gameServ.createGameRequestDTO(game, currentUser.getUsername())).collect(Collectors.toList());
		model.addAttribute("receivedList", receivedListDTO);
		
		List<GameRequest> pendingList = gameReqRepo.findBySenderUsernameOrderByStyleAscCreatedAtDesc(currentUser.getUsername());
		List<GameRequestDTO> pendingListDTO = 
				pendingList.stream().map(game -> gameServ.createGameRequestDTO(game, currentUser.getUsername())).collect(Collectors.toList());
		model.addAttribute("pendingList", pendingListDTO);
				
		List<Game> activeList = 
				gameRepo.findByWhiteUsernameOrBlackUsernameAndOutcomeOrderByStyleAscGameIdDesc(currentUser.getUsername(), currentUser.getUsername(), GameOutcome.INCOMPLETE);
		List<GameDTO> activeListDTO = 
				activeList.stream().map(game -> gameServ.createGameDTO(game, currentUser.getUsername())).collect(Collectors.toList());
		model.addAttribute("activeList", activeListDTO);
		
		List<Game> archiveList = 
				gameRepo.findByWhiteUsernameOrBlackUsernameAndWinnerNullOrderByStyleAscGameIdDesc(currentUser.getUsername(), currentUser.getUsername());
		List<GameDTO> archiveListDTO = 
				archiveList.stream().map(game -> gameServ.createGameDTO(game, currentUser.getUsername())).collect(Collectors.toList());
		model.addAttribute("archiveList", archiveListDTO);
		
		model.addAttribute("gameStyles", GameStyle.values());
		model.addAttribute("friends", friendServ.getFriendUsernames(currentUser.getUsername()));		
		model.addAttribute("gameReq", new GameRequestDTO());
		
		
		return "games";

	}

	@PostMapping("/gamerequest")
	public String handleGameRequest(Model model, @ModelAttribute("gameReq") GameRequestDTO gameReq, BindingResult result, @AuthenticationPrincipal Player currentUser) {
		
		//catch: existsBySenderUsernameAndReceiverUsernameAndStyle(String sender, String receiver, GameStyle style);

		
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
	@PostMapping("/gamerequest/decline/{id}")
	public ModelAndView declineGameRequest(@PathVariable("id") Long id) {

		gameReqRepo.deleteById(id);

		return new ModelAndView("redirect:/games");
	}
	
	@Modifying
	@Transactional
	@PostMapping("/gamerequest/accept/{id}")
	public String acceptGameRequest(@PathVariable("id") Long id) {

		GameRequest gameReq = gameReqRepo.findById(id).orElse(null);
		
		Game newGame = new Game();
		newGame.setStyle(gameReq.getStyle());
		
		double randNum = Math.random();
		
		if (randNum < 0.5) {
			newGame.setWhite(gameReq.getSender());
			newGame.setBlack(gameReq.getReceiver());
		} else {
			newGame.setWhite(gameReq.getReceiver());
			newGame.setBlack(gameReq.getSender());
		}

		newGame.setOpponentIsHuman(true);
		newGame.setOutcome(GameOutcome.INCOMPLETE);
		
		gameRepo.save(newGame);
		gameReqRepo.delete(gameReq);
		
		return "redirect:/newgame/";
	}
	
	@Modifying
	@Transactional
	@PostMapping("/game/forfeit/{id}")
	public String forfeitGame(@PathVariable("id") String id, @AuthenticationPrincipal Player currentUser) {

		Game activeGame = gameRepo.findByGameId(Long.parseLong(id));
		if (activeGame.getStartTime() == null) {
			gameRepo.delete(activeGame);
		} else {
			activeGame.setEndTime(ZonedDateTime.now());
			activeGame.setOutcome(GameOutcome.ABORTED);
			activeGame.setWinner(activeGame.getWhite().getUsername().equals(currentUser.getUsername()) ? GameWinner.BLACK : GameWinner.WHITE);
		}

		gameRepo.save(activeGame);
		
		return "redirect:/games";
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	@ResponseBody
	@GetMapping("friends/playerSearch")
	public ResponseEntity<List<String>> friendSearch(@RequestParam("userInput") String userInput,
			@AuthenticationPrincipal Player currentUser) {

		List<FriendRequest> connections = friendReqRepo
				.findByReceiverUsernameOrSenderUsername(currentUser.getUsername(), currentUser.getUsername());
		Set<String> connectionNames = new HashSet<>(Arrays.asList(currentUser.getUsername()));
		
		for (FriendRequest connection : connections) {
			connectionNames.add(connection.getReceiver().getUsername());
			connectionNames.add(connection.getSender().getUsername());
		}
		
		List<String> results = new ArrayList<>();
		results = playerRepo.searchTenNewFriends(userInput + "%").orElse(null);

		results.removeAll(connectionNames);

		if (!results.isEmpty()) {
			results.subList(0, Math.min(results.size(), 10));
		}

		return new ResponseEntity<List<String>>(results, HttpStatus.OK);
	}

	@ResponseBody
	@PostMapping("/friendrequest/send/{username}")
	public ModelAndView requestNewFriend(Model model, @PathVariable("username") String receiverUsername,
			@AuthenticationPrincipal Player currentUser) {

		Player sender = playerRepo.findByUsername(currentUser.getUsername());
		Player receiver = playerRepo.findByUsername(receiverUsername);
		FriendRequest newFriendReq = new FriendRequest();
		newFriendReq.setSender(sender);
		newFriendReq.setReceiver(receiver);
		newFriendReq.setRequestDate(new Date());
		newFriendReq.setLastModifiedDate(new Date());
		newFriendReq.setBlockedBy(BlockedBy.NEITHER);
		newFriendReq.setStatus(FriendRequestStatus.PENDING);
		friendReqRepo.save(newFriendReq);

		return new ModelAndView("redirect:/friends");
	}

	@ResponseBody
	@PostMapping("/block/{username}")
	public ModelAndView blockPlayer(Model model, @PathVariable("username") String receiverUsername,
			@AuthenticationPrincipal Player currentUser) {

		Player sender = playerRepo.findByUsername(currentUser.getUsername());
		Player receiver = playerRepo.findByUsername(receiverUsername);
		FriendRequest friendBlockRequest = new FriendRequest();
		friendBlockRequest.setSender(sender);
		friendBlockRequest.setReceiver(receiver);
		friendBlockRequest.setRequestDate(new Date());
		friendBlockRequest.setLastModifiedDate(new Date());
		friendBlockRequest.setStatus(FriendRequestStatus.BLOCKED);
		friendBlockRequest.setBlockedBy(BlockedBy.SENDER);

		friendReqRepo.save(friendBlockRequest);

		return new ModelAndView("redirect:/friends");
	}

	@ResponseBody
	@PostMapping("/friendrequest/accept/{username}")
	public ModelAndView acceptFriendRequest(Model model, @PathVariable("username") String sender,
			@AuthenticationPrincipal Player currentUser) {
		String receiver = currentUser.getUsername();

		FriendRequest approvedFriendReq = friendReqRepo.findByReceiverUsernameAndSenderUsername(receiver, sender);
		approvedFriendReq.setStatus(FriendRequestStatus.ACCEPTED);
		approvedFriendReq.setLastModifiedDate(new Date());
		friendReqRepo.save(approvedFriendReq);

		return new ModelAndView("redirect:/friends");
	}

	@ResponseBody
	@PostMapping("/friendrequest/block/{username}")
	public ModelAndView blockFriendRequest(Model model, @PathVariable("username") String sender,
			@AuthenticationPrincipal Player currentUser) {

		String receiver = currentUser.getUsername();

		FriendRequest blockedFriendReq = friendReqRepo.findByReceiverUsernameAndSenderUsername(receiver, sender);
		blockedFriendReq.setStatus(FriendRequestStatus.BLOCKED);
		blockedFriendReq.setBlockedBy(BlockedBy.RECEIVER);
		blockedFriendReq.setLastModifiedDate(new Date());
		friendReqRepo.save(blockedFriendReq);

		return new ModelAndView("redirect:/friends");
	}

	@ResponseBody
	@PostMapping("/friendrequest/decline/{username}")
	public ModelAndView declineFriendRequest(Model model, @PathVariable("username") String sender,
			@AuthenticationPrincipal Player currentUser) {

		String receiver = currentUser.getUsername();

		Long recordNumber = friendReqRepo.deleteByReceiverUsernameAndSenderUsername(receiver, sender);
		
		if (recordNumber != 1L) {
			System.out.println("ERROR WITH FRIEND REQUEST DELETION");
		}

		return new ModelAndView("redirect:/friends");
	}


	@ResponseBody
	@PostMapping("/pendingrequest/cancel/{receiver}")
	public ModelAndView cancelFriendRequest(Model model, @PathVariable("receiver") String receiver,
			@AuthenticationPrincipal Player currentUser) {

		String sender = currentUser.getUsername();
		Long recordNumber = friendReqRepo.deleteByReceiverUsernameAndSenderUsername(receiver, sender);
		
		if (recordNumber != 1L) {
			System.out.println("ERROR WITH FRIEND REQUEST DELETION");
		}
		
		return new ModelAndView("redirect:/friends");
	}

	@ResponseBody
	@PostMapping("/pendingrequest/block/{receiver}")
	public ModelAndView blockPendingRequest(Model model, @PathVariable("receiver") String receiver,
			@AuthenticationPrincipal Player currentUser) {

		String sender = currentUser.getUsername();
		FriendRequest blockedPendingReq = friendReqRepo.findByReceiverUsernameAndSenderUsername(receiver, sender);
		blockedPendingReq.setStatus(FriendRequestStatus.BLOCKED);
		blockedPendingReq.setBlockedBy(BlockedBy.SENDER);
		blockedPendingReq.setLastModifiedDate(new Date());

		friendReqRepo.save(blockedPendingReq);

		return new ModelAndView("redirect:/friends");
	}
	
	@ResponseBody
	@PostMapping("/friendrequest/unfriend/{username}")
	public ModelAndView unfriendPlayer(Model model, @PathVariable("username") String friendUsername,
			@AuthenticationPrincipal Player currentUser) {

		String currentUsername = currentUser.getUsername();
		
		Long recordNumber = friendReqRepo.deleteByReceiverUsernameAndSenderUsername(friendUsername, currentUsername);
		
		if (recordNumber == 0L) {
			recordNumber = friendReqRepo.deleteByReceiverUsernameAndSenderUsername(currentUsername, friendUsername);
		}
	
		if (recordNumber != 1L) {
			System.out.println("ERROR WITH FRIEND REQUEST DELETION");
		}
		
		return new ModelAndView("redirect:/friends");
	}

	@ResponseBody
	@PostMapping("/unblock/{blockedPlayer}")
	public ModelAndView unblockPlayer(Model model, @PathVariable("blockedPlayer") String friendUsername,
			@AuthenticationPrincipal Player currentUser) {

		String currentUsername = currentUser.getUsername();
		
		Long recordNumber = friendReqRepo.deleteByReceiverUsernameAndSenderUsername(friendUsername, currentUsername);
		
		if (recordNumber == 0L) {
			recordNumber = friendReqRepo.deleteByReceiverUsernameAndSenderUsername(currentUsername, friendUsername);
		}
	
		if (recordNumber != 1L) {
			System.out.println("ERROR WITH FRIEND REQUEST DELETION");
		}
		return new ModelAndView("redirect:/friends");
	}
	*/
}
