package com.github.hallbm.chesswithcats.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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

import com.github.hallbm.chesswithcats.model.FriendRequest;
import com.github.hallbm.chesswithcats.model.FriendRequest.BlockedBy;
import com.github.hallbm.chesswithcats.model.FriendRequest.FriendRequestStatus;
import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.FriendRequestRepository;
import com.github.hallbm.chesswithcats.repository.PlayerRepository;

@Controller
public class FriendRequestController {

	@Autowired
	private PlayerRepository playerRepo;

	@Autowired
	private FriendRequestRepository friendReqRepo;

	@GetMapping("/friends")
	public String showFriends(Model model, @AuthenticationPrincipal Player currentUser) {

		List<FriendRequest> receivedPendingRequests = friendReqRepo.findByReceiverUsernameAndStatus(
				currentUser.getUsername(), FriendRequest.FriendRequestStatus.PENDING);
		Set<String> requestingPlayerUsernames = receivedPendingRequests.stream().map(FriendRequest::getSender)
				.map(Player::getUsername).collect(Collectors.toCollection(TreeSet::new));
		model.addAttribute("receivedList", requestingPlayerUsernames);

		List<FriendRequest> sentPendingRequests = friendReqRepo.findBySenderUsernameAndStatus(
				currentUser.getUsername(), FriendRequest.FriendRequestStatus.PENDING);
		Set<String> requestedPlayerUsernames = sentPendingRequests.stream().map(FriendRequest::getReceiver)
				.map(Player::getUsername).collect(Collectors.toCollection(TreeSet::new));
		model.addAttribute("pendingList", requestedPlayerUsernames);

		List<FriendRequest> blockedFriendRequests = friendReqRepo.findBySenderUsernameAndStatusAndBlockedBy(
				currentUser.getUsername(), FriendRequest.FriendRequestStatus.BLOCKED, BlockedBy.SENDER);
		Set<String> blockedPlayerUsernames = blockedFriendRequests.stream().map(FriendRequest::getReceiver)
				.map(Player::getUsername).collect(Collectors.toCollection(TreeSet::new));
		blockedFriendRequests = friendReqRepo.findByReceiverUsernameAndStatusAndBlockedBy(
				currentUser.getUsername(), FriendRequest.FriendRequestStatus.BLOCKED, BlockedBy.RECEIVER);
		blockedPlayerUsernames.addAll(blockedFriendRequests.stream().map(FriendRequest::getSender)
				.map(Player::getUsername).collect(Collectors.toSet()));
		model.addAttribute("blockedList", blockedPlayerUsernames);

		List<FriendRequest> acceptedFriendRequests = friendReqRepo.findByReceiverUsernameAndStatus(
				currentUser.getUsername(), FriendRequest.FriendRequestStatus.ACCEPTED);
		TreeSet<String> friendUsernames = acceptedFriendRequests.stream()
				.map(FriendRequest::getSender).map(Player::getUsername).collect(Collectors.toCollection(TreeSet::new));
		acceptedFriendRequests = friendReqRepo.findBySenderUsernameAndStatus(currentUser.getUsername(),
				FriendRequest.FriendRequestStatus.ACCEPTED);
		friendUsernames.addAll(acceptedFriendRequests.stream()
				.map(FriendRequest::getReceiver).map(Player::getUsername).collect(Collectors.toSet()));
		model.addAttribute("friendsList", friendUsernames);

		return "friends";
	}

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
	
}
