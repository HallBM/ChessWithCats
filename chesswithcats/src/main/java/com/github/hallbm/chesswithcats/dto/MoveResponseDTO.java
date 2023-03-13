package com.github.hallbm.chesswithcats.dto;

import java.util.ArrayList;
import java.util.List;

import com.github.hallbm.chesswithcats.domain.GameEnums.ChessMove;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameOutcome;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for generating (via MoveValidator classes) and transmitting move response for the AJAX (Fetch API) call
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoveResponseDTO {
	
	private boolean isValid = false;
	private List<ChessMove> chessMoves = new ArrayList<>();
	private List<String[]> pieceMoves = new ArrayList<>();
	private String officialChessMove = "";
	private boolean isChecked;
	private GameOutcome gameOutcome;

	
	public void addChessMove (ChessMove chessMove) {
		chessMoves.add(chessMove);
	}
	
	public void addPieceMove(String[] move) {
		pieceMoves.add(move);
	}
	
}
