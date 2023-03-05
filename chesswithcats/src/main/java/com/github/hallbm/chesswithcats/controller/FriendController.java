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

@Controller
public class FriendController {

	@Autowired
	private PlayerRepository playerRepo;

	@Autowired
	private FriendRequestRepository friendReqRepo;

	@Autowired
	private FriendServices friendServ;

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

	@ResponseBody
	@PostMapping("/friendrequest/block/{username}")
	public ModelAndView blockFriendRequest(Model model, @PathVariable("username") String sender,
			@AuthenticationPrincipal Player currentUser) {

		String receiver = currentUser.getUsername();

		FriendRequest blockedFriendReq = friendReqRepo.findByReceiverUsernameAndSenderUsername(receiver, sender);
		blockedFriendReq.setStatus(FriendRequestStatus.BLOCKED);
		blockedFriendReq.setBlockedBy(BlockedBy.RECEIVER);
		blockedFriendReq.setLastModifiedDate(LocalDate.now());
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
		blockedPendingReq.setLastModifiedDate(LocalDate.now());

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
