package com.github.hallbm.chesswithcats.model;

import java.time.LocalDate;

import com.github.hallbm.chesswithcats.domain.FriendEnums.BlockedBy;
import com.github.hallbm.chesswithcats.domain.FriendEnums.FriendRequestStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "friend_requests")
public class FriendRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

	@ManyToOne
	@JoinColumn(name = "sender_username", referencedColumnName = "username")
	@NotNull
	private Player sender;

	@ManyToOne
	@JoinColumn(name = "receiver_username", referencedColumnName = "username")
	@NotNull
	private Player receiver;

	@Column(updatable = false)
	@Temporal(TemporalType.DATE)
	private LocalDate requestDate;

	@Column
	@Temporal(TemporalType.DATE)
	private LocalDate lastModifiedDate;

	@Column(length = 10, nullable=false)
	@NotNull
	@Enumerated(EnumType.STRING)
	private FriendRequestStatus status;

	@Column(length = 10)
	@Enumerated(EnumType.STRING)
	private BlockedBy blockedBy;

}
