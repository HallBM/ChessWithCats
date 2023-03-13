package com.github.hallbm.chesswithcats.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.github.hallbm.chesswithcats.domain.FriendEnums.BlockedBy;
import com.github.hallbm.chesswithcats.domain.FriendEnums.FriendRequestStatus;
import com.github.hallbm.chesswithcats.model.FriendRequest;

import jakarta.transaction.Transactional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long>{

	public List<FriendRequest> findByReceiverUsername(String receiver);
	public List<FriendRequest> findBySenderUsername(String sender);

	public List<FriendRequest> findByReceiverUsernameOrSenderUsername(String receiver, String sender);
	public FriendRequest findByReceiverUsernameAndSenderUsername(String receiver, String sender);

	public List<FriendRequest> findByReceiverUsernameAndStatus(String receiver, FriendRequestStatus status);
	public List<FriendRequest> findBySenderUsernameAndStatus(String sender, FriendRequestStatus status);

	public List<FriendRequest> findByReceiverUsernameAndStatusAndBlockedBy(String receiver, FriendRequestStatus status, BlockedBy blockedBy);
	public List<FriendRequest> findBySenderUsernameAndStatusAndBlockedBy(String sender, FriendRequestStatus status, BlockedBy blockedBy);

	@Query(value = "SELECT * FROM friend_requests f "
			+ "WHERE (f.sender_username = :username1 AND f.receiver_username = :username2) OR "
			+ "(f.sender_username = :username2 AND f.receiver_username = :username1)", nativeQuery = true)
	FriendRequest getFriendshipByUsernames(String username1, String username2);
	
	@Modifying
	@Transactional
	public long deleteByReceiverUsernameAndSenderUsername(String receiver, String sender);


}
