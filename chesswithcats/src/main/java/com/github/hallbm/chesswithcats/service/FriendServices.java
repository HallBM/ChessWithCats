package com.github.hallbm.chesswithcats.service;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.hallbm.chesswithcats.model.FriendRequest;
import com.github.hallbm.chesswithcats.model.FriendRequest.BlockedBy;
import com.github.hallbm.chesswithcats.model.FriendRequest.FriendRequestStatus;
import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.FriendRequestRepository;
import com.github.hallbm.chesswithcats.repository.PlayerRepository;

import jakarta.transaction.Transactional;

@Service
public class FriendServices {
	@Autowired
	private PlayerRepository playerRepo;

	@Autowired
	private FriendRequestRepository friendReqRepo;
	
	@Transactional
	public Set<String> getReceivedFriendRequestUsernames(String currentUsername){
		List<FriendRequest> receivedPendingRequests = 
				friendReqRepo.findByReceiverUsernameAndStatus(currentUsername, FriendRequestStatus.PENDING);
		Set<String> requestingPlayerUsernames = receivedPendingRequests.stream()
					.map(FriendRequest::getSender)
					.map(Player::getUsername)
					.collect(Collectors.toCollection(TreeSet::new));
		return requestingPlayerUsernames;
	}
	
	@Transactional
	public Set<String> getPendingFriendRequestUsernames(String currentUsername) {
		List<FriendRequest> sentPendingRequests = 
				friendReqRepo.findBySenderUsernameAndStatus(currentUsername, FriendRequestStatus.PENDING);
		Set<String> requestedPlayerUsernames = sentPendingRequests.stream()
				.map(FriendRequest::getReceiver)
				.map(Player::getUsername)
				.collect(Collectors.toCollection(TreeSet::new));
		return requestedPlayerUsernames;
	}
	
	@Transactional
	public Set<String> getBlockedUsernames (String currentUsername) {
		List<FriendRequest> blockedFriendRequests = 
				friendReqRepo.findBySenderUsernameAndStatusAndBlockedBy(currentUsername, FriendRequestStatus.BLOCKED, BlockedBy.SENDER);
		Set<String> blockedUsernames = blockedFriendRequests.stream()
				.map(FriendRequest::getReceiver)
				.map(Player::getUsername)
				.collect(Collectors.toCollection(TreeSet::new));
		blockedFriendRequests = 
				friendReqRepo.findByReceiverUsernameAndStatusAndBlockedBy(currentUsername, FriendRequestStatus.BLOCKED, BlockedBy.RECEIVER);
		blockedUsernames.addAll(blockedFriendRequests.stream()
				.map(FriendRequest::getSender)
				.map(Player::getUsername)
				.collect(Collectors.toSet()));
		return blockedUsernames;
	}
	
	@Transactional
	public Set<String> getFriendUsernames (String currentUsername){

		List<FriendRequest> acceptedFriendRequests = 
				friendReqRepo.findByReceiverUsernameAndStatus(currentUsername, FriendRequestStatus.ACCEPTED);
		Set<String> friendUsernames = acceptedFriendRequests.stream()
				.map(FriendRequest::getSender)
				.map(Player::getUsername)
				.collect(Collectors.toCollection(TreeSet::new));
		acceptedFriendRequests = 
				friendReqRepo.findBySenderUsernameAndStatus(currentUsername, FriendRequestStatus.ACCEPTED);
		friendUsernames.addAll(acceptedFriendRequests.stream()
				.map(FriendRequest::getReceiver)
				.map(Player::getUsername)
				.collect(Collectors.toSet()));
		return friendUsernames;

	}
	
	@Transactional
	public Set<String> getAllConnectionUsernamesAndSelf (String currentUsername){
		List<FriendRequest> connections = 
				friendReqRepo.findByReceiverUsernameOrSenderUsername(currentUsername, currentUsername);
		Set<String> connectionUsernames = connections.stream()
				.map(FriendRequest::getSender)
				.map(Player::getUsername)
				.collect(Collectors.toSet());
		connectionUsernames.addAll(connections.stream()
				.map(FriendRequest::getReceiver)
				.map(Player::getUsername)
				.collect(Collectors.toSet()));
		connectionUsernames.add(currentUsername);
		return connectionUsernames;
	}
}
