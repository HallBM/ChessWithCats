package com.github.hallbm.chesswithcats.model;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(name = "games")
public class Game {

	public enum GameOutcome {
		CHECKMATE, RESIGNATION, TIMEOUT, STALEMATE, INSUFFICIENT_MATERIAL, EXCESSIVE_MOVE_RULE, REPETITION, AGREEMENT, ABORTED, INCOMPLETE; 
	}

	public enum GameWinner {
		WHITE, BLACK, DRAW; 
	}
	
	public enum GameWLD{
		WIN, LOSE, DRAW;
	}
	
	public enum GameColor {
		WHITE, BLACK, NEUTRAL
	}
	
	public enum GameStyle {
		CLASSIC("Classic Chess"), 
		OBSTRUCTIVE("Obstructive Kitties"), 
		AMBIGUOUS("Ambiguous Kitties"), 
		DEFIANT("Defiant Kitties");
		
		private final String description;

		private GameStyle(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}

	@Id
	@Column(nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long gameId;

	@Column(length = 20)
	@Enumerated(EnumType.STRING)
	private GameStyle style;

	@Column(length = 21, nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
	private GameOutcome outcome;
	
	@Column(length = 5)
	@Enumerated(EnumType.STRING)
	private GameWinner winner;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@NotNull
	@JoinColumn(name = "white_username", referencedColumnName = "username")
	private Player white;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@NotNull
	@JoinColumn(name = "black_username", referencedColumnName = "username")
	private Player black;

	@Column(updatable = false)
	@NotNull
	@Temporal(TemporalType.DATE)
	private Date acceptDate = new Date();

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private ZonedDateTime startTime;

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private ZonedDateTime endTime;
	
	@Column
	@NotNull
	private boolean opponentIsHuman;
	
	// TODO setup game move details
	// private GamePlay gamePlay;

	// TODO setup white time remaining (for timed games)
	// private GamePlay gamePlay;

	// TODO setup black time remaining (for timed games)
	// private GamePlay gamePlay;

}
