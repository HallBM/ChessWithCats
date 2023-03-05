package com.github.hallbm.chesswithcats.model;

import java.time.LocalDate;

import com.github.hallbm.chesswithcats.domain.GameEnums.GameOutcome;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameWinner;

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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "games")
public class Game {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "BIGINT UNSIGNED AUTO_INCREMENT")
    private long id;

	@Column(length = 20)
	@Enumerated(EnumType.STRING)
	private GameStyle style;

	@Column(length = 21, nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
	private GameOutcome outcome = GameOutcome.ACCEPTED;

	@Column(length = 5)
	@Enumerated(EnumType.STRING)
	private GameWinner winner = null;

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

	//@OneToOne(mappedBy = "game", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private GamePlay gamePlay = new GamePlay();

	@Column(length = 64, updatable = false, nullable = false)
	@NotNull
	@Size(max = 64)
	private String openingFen;

	@Column(name="moves", length = 1000)
	@Size(max = 1000)
	private String moves = null;

}