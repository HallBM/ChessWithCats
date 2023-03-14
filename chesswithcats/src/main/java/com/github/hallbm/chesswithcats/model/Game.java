package com.github.hallbm.chesswithcats.model;

import java.io.Serializable;
import java.time.LocalDate;

import com.github.hallbm.chesswithcats.domain.GameEnums.GameOutcome;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;

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
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
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
public class Game implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

	@Column(length = 20)
	@NotNull
	@Enumerated(EnumType.STRING)
	private GameStyle style;

	@Column(length = 21, nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
	private GameOutcome outcome = GameOutcome.ACCEPTED;
	
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

	@Column
	@NotNull
	private boolean opponentIsHuman = true;

	@OneToOne(mappedBy = "game", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private GamePlay gamePlay = new GamePlay();

	@Column(length = 64, updatable = false, nullable = false)
	@NotNull
	@Size(max = 64)
	private String openingFen;

	@Column(name="moves", length = 3072)
	private String moves = null;

}
