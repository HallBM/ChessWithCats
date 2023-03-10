package com.github.hallbm.chesswithcats.dto;

import java.util.ArrayList;
import java.util.List;

import com.github.hallbm.chesswithcats.domain.GameEnums.ChessMove;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameColor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

	
	public void addChessMove (ChessMove chessMove) {
		chessMoves.add(chessMove);
	}
	
	public void addPieceMove(String[] move) {
		pieceMoves.add(move);
	}
	
}
