package com.github.hallbm.chesswithcats.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.hallbm.chesswithcats.domain.FriendEnums.FriendRequestStatus;
import com.github.hallbm.chesswithcats.model.FriendRequest;
import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.FriendRequestRepository;

import jakarta.transaction.Transactional;

/**
 * Services for maintaining player relationships.
 */

@Service
public class FriendServices {

	@Autowired
	private FriendRequestRepository friendReqRepo;

	/**
	 * Returns a set of usernames that have sent friend requests to the @Param currentUsername.
	 */
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

	/**
	 * Returns a set of usernames that have been sent friend requests by the @Param currentUsername.
	 */
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

	/**
	 * Returns a set of usernames that have been blocked by the @Param currentUsername.
	 */
	@Transactional
	public Set<String> getBlockedUsernames (String currentUsername) {
		List<FriendRequest> blockedFriendRequests =
				friendReqRepo.getBlockedByUsername(currentUsername);
		
		Set<String> blockedUsernames = new TreeSet<>();
		
		for (FriendRequest fr : blockedFriendRequests) {
			blockedUsernames.add(fr.getSender().getUsername());
			blockedUsernames.add(fr.getReceiver().getUsername());
		}
		
		blockedUsernames.remove(currentUsername);
		
		return blockedUsernames;
	}
	
	@Transactional
	public Set<Player> getBlockedPlayers (String currentUsername) {
		List<FriendRequest> blockedFriendRequests =
				friendReqRepo.getByUsernameAndStatus(currentUsername,FriendRequestStatus.BLOCKED.toString());
		
		Set<Player> blockedPlayers = new HashSet<>();
		
		for (FriendRequest fr : blockedFriendRequests) {
			blockedPlayers.add(fr.getSender());
			blockedPlayers.add(fr.getReceiver());
		}
		
		return blockedPlayers;
	}
	
	/**
	 * Returns a set of usernames that have been befriended by the @Param currentUsername.
	 */
	@Transactional
	public Set<String> getFriendUsernames (String currentUsername){

		List<FriendRequest> acceptedFriendRequests =
				friendReqRepo.getByUsernameAndStatus(currentUsername, FriendRequestStatus.ACCEPTED.toString());
		Set<String> friendsUsernames = new TreeSet<>();
		
		for (FriendRequest fr : acceptedFriendRequests) {
			friendsUsernames.add(fr.getSender().getUsername());
			friendsUsernames.add(fr.getReceiver().getUsername());
		}
		
		friendsUsernames.remove(currentUsername);
		return friendsUsernames;
	}

	/**
	 * Returns a set of usernames for all associated 'friend requests' in the database regardless of status for the @Param currentUsername.
	 * Includes accepted, sent/received pending requests, and blocked status (either party).
	 */
	@Transactional
	public Set<String> getAllConnectionUsernamesAndSelf (String currentUsername){
		List<FriendRequest> connections =
				friendReqRepo.getByUsername(currentUsername);
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
