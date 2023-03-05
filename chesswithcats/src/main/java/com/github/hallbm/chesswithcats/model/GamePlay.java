package com.github.hallbm.chesswithcats.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
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
@Table(name = "game_plays")
public class GamePlay {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name="half_moves")
    private short halfMoves = 1;

    @Column(name="fifty_move_clock")
    private byte fiftyMoveClock = 0;

    @Column(name = "en_passant", length = 2)
    @Size(max = 2)
	private String enPassantTargetSquare = null;

    @Column(length=4)
    @Size(min = 0, max = 4)
	private String castling = "KQkq";

	@Lob
	@Embedded
	private GameBoard gameBoard = new GameBoard();
	
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "fen_set")
    private Set<String> fenSet = new HashSet<>();

    public String updateFenSet() {
       	String fen = gameBoard.getFenPositions() + " "
        			+ (getHalfMoves() % 2 == 1 ? "w" : "b") + " "
        			+ getCastling() + " "
        			+ (getEnPassantTargetSquare() == null ? "-" : getEnPassantTargetSquare()) + " "
        			+ String.valueOf(getFiftyMoveClock()) + " "
        			+ String.valueOf((getHalfMoves() - 1)/2 + 1);
       	fenSet.add(fen);
       	return fen;
    }
    
}