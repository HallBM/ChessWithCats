package com.github.hallbm.chesswithcats.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.github.hallbm.chesswithcats.domain.FriendEnums.BlockedBy;
import com.github.hallbm.chesswithcats.domain.FriendEnums.FriendRequestStatus;
import com.github.hallbm.chesswithcats.model.FriendRequest;
import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.FriendRequestRepository;
import com.github.hallbm.chesswithcats.repository.PlayerRepository;
import com.github.hallbm.chesswithcats.service.FriendServices;

/**
 * Controller for CRUD related to maintaining relationships with other player
 */

@Controller
public class FriendController {

	@Autowired
	private PlayerRepository playerRepo;

	@Autowired
	private FriendRequestRepository friendReqRepo;

	@Autowired
	private FriendServices friendServ;

	
	/**
	 * Generates and displays lists of player usernames based on relationship status 
	 * (received request, pending request, blocked players, accepted friend requests)
	 * for display on the 'friends' page.
	 * BlockedList only shows players that the user has blocked (not players that have blocked the user).
	 * In this way, only users that initiate a block can unblock, and the blocked player is unaware of the block.
	 */
	
	@GetMapping("/friends")
	public String showFriends(Model model, @AuthenticationPrincipal Player currentUser) {

		Set<String> receivedRequests = friendServ.getReceivedFriendRequestUsernames(currentUser.getUsername());
		model.addAttribute("receivedList", receivedRequests);

		Set<String> pendingRequests = friendServ.getPendingFriendRequestUsernames(currentUser.getUsername());
		model.addAttribute("pendingList", pendingRequests);

		Set<String> blockedUsernames = friendServ.getBlockedUsernames(currentUser.getUsername());
		model.addAttribute("blockedList", blockedUsernames);

		Set<String> friendUsernames = friendServ.getFriendUsernames(currentUser.getUsername());
		model.addAttribute("friendsList", friendUsernames);

		return "friends";
	}

	/**
	 * AJAX endpoint for searching for partially matched usernames
	 * (searches based on match to the first part of name, i.e, "inputString%")
	 * Searches exclude players involved in accepted, pending or received requests
	 * and players that are blocked (by either party).
	 */
	
	@ResponseBody
	@GetMapping("friends/playerSearch")
	public ResponseEntity<List<String>> friendSearch(@RequestParam("userInput") String userInput,
			@AuthenticationPrincipal Player currentUser) {

		Set<String> connectionNames = friendServ.getAllConnectionUsernamesAndSelf(currentUser.getUsername());

		List<String> results = playerRepo.searchNewFriends(userInput + "%").orElse(null);

		if (!results.isEmpty()) {
			results.removeAll(connectionNames);
			results.subList(0, Math.min(results.size(), 10));
		}

		return new ResponseEntity<List<String>>(results, HttpStatus.OK);
	}

	/**
	 * Creates friend request in database. Request pending until other player accepts/rejects/blocks. 
	 */
	
	@ResponseBody
	@PostMapping("/friendrequest/send/{username}")
	public ModelAndView requestNewFriend(Model model, @PathVariable("username") String receiverUsername,
			@AuthenticationPrincipal Player currentUser) {

		Player sender = playerRepo.findByUsername(currentUser.getUsername());
		Player receiver = playerRepo.findByUsername(receiverUsername);
		FriendRequest newFriendReq = new FriendRequest();
		newFriendReq.setSender(sender);
		newFriendReq.setReceiver(receiver);
		newFriendReq.setRequestDate(LocalDate.now());
		newFriendReq.setLastModifiedDate(LocalDate.now());
		newFriendReq.setBlockedBy(BlockedBy.NEITHER);
		newFriendReq.setStatus(FriendRequestStatus.PENDING);
		friendReqRepo.save(newFriendReq);

		return new ModelAndView("redirect:/friends");
	}

	/**
	 * Blocks player from the player search (no prior request made).
	 * Blocking tracks who initiated the block. In this way, only players
	 * who blocked the other user has the option on the friends page to unblock. 
	 */
	@ResponseBody
	@PostMapping("/block/{username}")
	public ModelAndView blockPlayer(Model model, @PathVariable("username") String receiverUsername,
			@AuthenticationPrincipal Player currentUser) {

		Player sender = playerRepo.findByUsername(currentUser.getUsername());
		Player receiver = playerRepo.findByUsername(receiverUsername);
		FriendRequest friendBlockRequest = new FriendRequest();
		friendBlockRequest.setSender(sender);
		friendBlockRequest.setReceiver(receiver);
		friendBlockRequest.setRequestDate(LocalDate.now());
		friendBlockRequest.setLastModifiedDate(LocalDate.now());
		friendBlockRequest.setStatus(FriendRequestStatus.BLOCKED);
		friendBlockRequest.setBlockedBy(BlockedBy.SENDER);

		friendReqRepo.save(friendBlockRequest);

		return new ModelAndView("redirect:/friends");
	}
	
	/**
	 * Accepts received friend request. 
	 */
	@ResponseBody
	@PostMapping("/friendrequest/accept/{username}")
	public ModelAndView acceptFriendRequest(Model model, @PathVariable("username") String sender,
			@AuthenticationPrincipal Player currentUser) {
		String receiver = currentUser.getUsername();

		FriendRequest approvedFriendReq = friendReqRepo.findByReceiverUsernameAndSenderUsername(receiver, sender);
		approvedFriendReq.setStatus(FriendRequestStatus.ACCEPTED);
		approvedFriendReq.setLastModifiedDate(LocalDate.now());
		friendReqRepo.save(approvedFriendReq);

		return new ModelAndView("redirect:/friends");
	}

	/**
	 * Blocks player that sent a friend request. 
	 * Blocking tracks who initiated the block. In this way, only players
	 * who blocked the other user has the option on the friends page to unblock.
	 */
	@ResponseBody
	@PostMapping("/friendrequest/block/{username}")
	public ModelAndView blockFriendRequest(Model model, @PathVariable("username") String username1,
			@AuthenticationPrincipal Player currentUser) {

		String currentUsername = currentUser.getUsername();

		FriendRequest blockedFriendReq = friendReqRepo.getFriendshipByUsernames(username1, currentUsername);
		blockedFriendReq.setStatus(FriendRequestStatus.BLOCKED);

		if (blockedFriendReq.getSender().getUsername().equals(currentUsername)) {
			blockedFriendReq.setBlockedBy(BlockedBy.SENDER);
		} else {
			blockedFriendReq.setBlockedBy(BlockedBy.RECEIVER);
		}

		blockedFriendReq.setLastModifiedDate(LocalDate.now());
		friendReqRepo.save(blockedFriendReq);

		return new ModelAndView("redirect:/friends");
	}

	/**
	 * Declines friend request. Previously created friend request is deleted from database. 
	 */
	@ResponseBody
	@PostMapping("/friendrequest/decline/{username}")
	public ModelAndView declineFriendRequest(Model model, @PathVariable("username") String sender,
			@AuthenticationPrincipal Player currentUser) {

		String receiver = currentUser.getUsername();

		int deletedRecords = friendReqRepo.deleteByUsernames(receiver, sender);

		if (deletedRecords != 1) {
			System.out.println("ERROR WITH FRIEND REQUEST DELETION");
		}

		return new ModelAndView("redirect:/friends");
	}
	
	/**
	 * Cancels sent friend request (deletes friend request).
	 * Will unfriend player if they have already accepted. 
	 */
	@ResponseBody
	@PostMapping("/pendingrequest/cancel/{receiver}")
	public ModelAndView cancelFriendRequest(Model model, @PathVariable("receiver") String receiver,
			@AuthenticationPrincipal Player currentUser) {

		String sender = currentUser.getUsername();
		int deletedRecords = friendReqRepo.deleteByUsernames(receiver, sender);

		if (deletedRecords != 1) {
			System.out.println("ERROR WITH FRIEND REQUEST DELETION");
		}

		return new ModelAndView("redirect:/friends");
	}

	/**
	 * Blocks user from a sent and pending friend request. 
	 */
	@ResponseBody
	@PostMapping("/pendingrequest/block/{receiver}")
	public ModelAndView blockPendingRequest(Model model, @PathVariable("receiver") String receiver,
			@AuthenticationPrincipal Player currentUser) {

		String sender = currentUser.getUsername();
		FriendRequest blockedPendingReq = friendReqRepo.findByReceiverUsernameAndSenderUsername(receiver, sender);
		blockedPendingReq.setStatus(FriendRequestStatus.BLOCKED);
		blockedPendingReq.setBlockedBy(BlockedBy.SENDER);
		blockedPendingReq.setLastModifiedDate(LocalDate.now());

		friendReqRepo.save(blockedPendingReq);

		return new ModelAndView("redirect:/friends");
	}
	
	/**
	 * Unfriends player from a previously accepted friend request.
	 * Deletes record from database. 
	 */
	@ResponseBody
	@PostMapping("/friendrequest/unfriend/{username}")
	public ModelAndView unfriendPlayer(Model model, @PathVariable("username") String friendUsername,
			@AuthenticationPrincipal Player currentUser) {

		String currentUsername = currentUser.getUsername();

		int deletedRecords = friendReqRepo.deleteByUsernames(friendUsername, currentUsername);

		if (deletedRecords != 1) {
			System.out.println("ERROR WITH FRIEND REQUEST DELETION");
		}

		return new ModelAndView("redirect:/friends");
	}

	/**
	 * Unblocks a player that the user has previously blocked.
	 * Only the blocking party has access to this ability.
	 * Deletes record from database. 
	 */
	@ResponseBody
	@PostMapping("/unblock/{blockedPlayer}")
	public ModelAndView unblockPlayer(Model model, @PathVariable("blockedPlayer") String friendUsername,
			@AuthenticationPrincipal Player currentUser) {

		String currentUsername = currentUser.getUsername();

		int deletedRecords = friendReqRepo.deleteByUsernames(friendUsername, currentUsername);

		if (deletedRecords != 1) {
			System.out.println("ERROR WITH FRIEND REQUEST DELETION");
		}
		return new ModelAndView("redirect:/friends");
	}

}
