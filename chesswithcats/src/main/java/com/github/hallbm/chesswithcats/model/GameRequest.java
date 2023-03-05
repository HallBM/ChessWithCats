package com.github.hallbm.chesswithcats.model;

import java.time.LocalDateTime;

import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;

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
import jakarta.persistence.UniqueConstraint;
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
@Table (name = "game_requests", uniqueConstraints = {@UniqueConstraint(columnNames = {"sender_username", "receiver_username", "style"})})
public class GameRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
	
	@Column(name = "created_at", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime createdAt = LocalDateTime.now();

	@ManyToOne
	@JoinColumn(name = "sender_username", referencedColumnName = "username")
	@NotNull
	private Player sender;

	@ManyToOne
	@JoinColumn(name = "receiver_username", referencedColumnName = "username")
	@NotNull
	private Player receiver;

	@Column(nullable=false)
	@NotNull
	@Enumerated(EnumType.STRING)
	private GameStyle style;

}
