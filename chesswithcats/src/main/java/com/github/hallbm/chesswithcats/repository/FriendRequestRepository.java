package com.github.hallbm.chesswithcats.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.github.hallbm.chesswithcats.domain.FriendEnums.FriendRequestStatus;
import com.github.hallbm.chesswithcats.model.FriendRequest;

import jakarta.transaction.Transactional;

@Transactional
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long>{

	@Query(value = "SELECT * FROM friend_requests f "
			+ "WHERE (f.sender_username = :username OR f.receiver_username = :username)", nativeQuery = true)
	public List<FriendRequest> getByUsername(String username);
	
	public FriendRequest findByReceiverUsernameAndSenderUsername(String receiver, String sender);

	public List<FriendRequest> findByReceiverUsernameAndStatus(String receiver, FriendRequestStatus status);
	public List<FriendRequest> findBySenderUsernameAndStatus(String sender, FriendRequestStatus status);
	
	@Query(value = "SELECT * FROM friend_requests f "
			+ "WHERE (f.sender_username = :username OR f.receiver_username = :username) AND f.status = :status", nativeQuery = true)
	public List<FriendRequest> getByUsernameAndStatus(String username, String status);
	
	@Query(value = "SELECT * FROM friend_requests f "
			+ "WHERE (f.sender_username = :username AND f.blocked_by = 'SENDER' AND f.status = 'BLOCKED') OR "
			+ "(f.receiver_username = :username AND f.blocked_by = 'RECEIVER' AND f.status = 'BLOCKED')", nativeQuery = true)
	public List<FriendRequest> getBlockedByUsername(String username);

	@Query(value = "SELECT * FROM friend_requests f "
			+ "WHERE (f.sender_username = :username1 AND f.receiver_username = :username2) OR "
			+ "(f.sender_username = :username2 AND f.receiver_username = :username1)", nativeQuery = true)
	FriendRequest getFriendshipByUsernames(String username1, String username2);
	
	@Modifying
	@Query(value = "DELETE FROM friend_requests f "
			+ "WHERE (f.sender_username = :username1 AND f.receiver_username = :username2) OR "
			+ "(f.sender_username = :username2 AND f.receiver_username = :username1)", nativeQuery = true)
	public int deleteByUsernames(String username1, String username2);


}
