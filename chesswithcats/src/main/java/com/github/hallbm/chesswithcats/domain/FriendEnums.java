package com.github.hallbm.chesswithcats.domain;

/**
 * List of enum sets for standardizing options for friend requests
 */

public class FriendEnums {

	public enum FriendRequestStatus {
		PENDING, ACCEPTED, BLOCKED;
	}

	public enum BlockedBy{
		SENDER, RECEIVER, NEITHER;
	}

}
