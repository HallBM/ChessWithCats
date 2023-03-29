package com.github.hallbm.chesswithcats.model;

import java.time.LocalDate;

import com.github.hallbm.chesswithcats.domain.AmbiguousMoveValidator;
import com.github.hallbm.chesswithcats.domain.DefiantMoveValidator;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameOutcome;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;
import com.github.hallbm.chesswithcats.domain.MoveValidator;
import com.github.hallbm.chesswithcats.domain.ObstructiveMoveValidator;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Persistence of long-term game data. 
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "games")
public class Game{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@Column(length = 20)
	@NotNull
	@Enumerated(EnumType.STRING)
	private GameStyle style;

	@Column(length = 21, nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
	private GameOutcome outcome = GameOutcome.ACCEPTED;
	
	@Column(length = 30)
	@Size(max=30)
	private String winner = null;

	@ManyToOne(fetch = FetchType.LAZY)
	@NotNull
	@JoinColumn(name = "white_username", referencedColumnName = "username")
	private Player white;

	@ManyToOne(fetch = FetchType.LAZY)
	@NotNull
	@JoinColumn(name = "black_username", referencedColumnName = "username")
	private Player black;

	@Column(updatable = false)
	@NotNull
	@Temporal(TemporalType.DATE)
	private LocalDate acceptDate = LocalDate.now();

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private GamePlay gamePlay = new GamePlay();

	@Column(length = 64, updatable = false, nullable = false)
	@NotNull
	@Size(max = 64)
	private String openingFen;

	@Column(name = "full_move_history", length = 3072)
	private String moves = null;
	
	@Transient
	private MoveValidator moveValidator;
	
	@PostLoad
	public void setValidator(){
		switch(style) {
		case OBSTRUCTIVE -> moveValidator = new ObstructiveMoveValidator();
		case DEFIANT -> moveValidator = new DefiantMoveValidator();
		case AMBIGUOUS -> moveValidator = new AmbiguousMoveValidator();
		case CLASSIC -> moveValidator = new MoveValidator();
		}
	}
}
