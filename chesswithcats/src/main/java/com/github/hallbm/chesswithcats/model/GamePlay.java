package com.github.hallbm.chesswithcats.model;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.github.hallbm.chesswithcats.domain.GameEnums.GameColor;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Persistence of short-term game data used for evaluating/validating and tracking moves.
 * GamePlay data (and other associated connections other than 'Game') are deleted upon 
 * game completion. When a game is finished, only game moves (extended piece notation move string) 
 * are transmitted to Game class for long-term persistence (and can be used to recreate game
 * move history with future 'game replay' functionality).  
 * 
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "game_plays")
public class GamePlay {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_id", referencedColumnName = "id")
	private Game game;

	@Column(name = "half_moves")
	private short halfMoves = 1;

	@Column(name = "fifty_move_clock")
	private byte fiftyMoveClock = 0;

	@Column(name = "en_passant", length = 2)
	@Size(max = 2)
	private String enPassantTargetSquare = null;

	@Column(length = 4)
	@Size(min = 0, max = 4)
	private String castling = "KQkq";

	@Lob
	@Embedded
	private GameBoard gameBoard = new GameBoard();

	private GameColor isChecked;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "fen_set")
	@Column(length = 100)
	@JoinColumn(name = "game_plays_id", referencedColumnName = "id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<String> fenSet = new HashSet<>();

	@Column(length = 3072)
	private StringBuffer moves = new StringBuffer();

	@Column(length = 3072)
	private String moveString = new String();

	public String updateFenSet() {
		String fen = gameBoard.getFenPositions() + " " + (halfMoves % 2 == 1 ? "w" : "b") + " "
				+ (castling.equals("") ? "-" : castling) + " "
				+ (enPassantTargetSquare == null ? "-" : enPassantTargetSquare) + " "
				+ String.valueOf(fiftyMoveClock) + " " + String.valueOf((halfMoves - 1) / 2 + 1);
		fenSet.add(fen);
		return fen;
	}

	public void incrementHalfMoves() {
		halfMoves++;
	}

	public void incrementFiftyMoveClock() {
		fiftyMoveClock++;
	}

	public void resetFiftyMoveClock() {
		fiftyMoveClock = 0;
	}

	public void addMove(String newMove) {
		moveString += newMove;
		moves.append(newMove);
	}

	public void removeCastling(String castle) {
		castling.replace(castle, "");
	}

}
