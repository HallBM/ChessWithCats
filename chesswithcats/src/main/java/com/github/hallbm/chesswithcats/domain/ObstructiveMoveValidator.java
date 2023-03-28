package com.github.hallbm.chesswithcats.domain;

import com.github.hallbm.chesswithcats.domain.GameEnums.ChessMove;
import com.github.hallbm.chesswithcats.domain.GameEnums.PieceNotation;
import com.github.hallbm.chesswithcats.service.GameBoardServices;

public class ObstructiveMoveValidator extends MoveValidator{

	@Override	
	protected boolean validatePawnMove() {
		
		if (!isValidPawnMovement())
			return false;
		if (!isValidPawnDirection())
			return false;

		if (absColDisp == 1 && absRowDisp == 1) {
			if (isPieceAttack()) {
				chessMoves.add(ChessMove.CAPTURE);
			} else if (isValidEnPassantAttack()) {
				chessMoves.add(ChessMove.EN_PASSANT_CAPTURE);
				chessMoves.add(ChessMove.CAPTURE);
				moveResponseDTO.addPieceMove(new String[] { GameBoardServices.getSquare(startRow, endCol), null });
			} else {
				return false;
			}
		} else if (absColDisp == 0 && absRowDisp == 2) {
			// modified rules allowing only pawns to jump forward over obstructive cats 
			if (mockBoard[startRow+(isWhiteMove ? -1 : 1)][startCol] == PieceNotation.C && !isPieceAttack()) {
				chessMoves.add(ChessMove.SIMPLE_MOVE);
			} else if (isValidPawnInitialDouble()) {
				chessMoves.add(ChessMove.PAWN_INITIAL_DOUBLE);
			} else {
				return false;
			}
		} else if (absColDisp == 0 && absRowDisp == 1) {
			if (!isPieceAttack()) {
				chessMoves.add(ChessMove.SIMPLE_MOVE);
			} else {
				return false;
			}
		}

		return true;
	}
}
