package com.github.hallbm.chesswithcats.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.github.hallbm.chesswithcats.model.FriendRequest;
import com.github.hallbm.chesswithcats.model.FriendRequest.BlockedBy;
import com.github.hallbm.chesswithcats.model.FriendRequest.FriendRequestStatus;

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
	
	@Modifying
	@Transactional
	public long deleteByReceiverUsernameAndSenderUsername(String receiver, String sender);
	
    
}
