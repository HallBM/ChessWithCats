package com.github.hallbm.chesswithcats.domain;

import com.github.hallbm.chesswithcats.domain.GameEnums.ChessMove;
import com.github.hallbm.chesswithcats.domain.GameEnums.PieceNotation;
import com.github.hallbm.chesswithcats.dto.MoveDTO;
import com.github.hallbm.chesswithcats.model.GamePlay;
import com.github.hallbm.chesswithcats.service.GameBoardServices;

public class ObstructiveMoveValidator extends MoveValidator{

	public ObstructiveMoveValidator(GamePlay gamePlay, MoveDTO moveDTO) {
		super(gamePlay, moveDTO);
	}

	@Override	
	protected boolean checkPawnMove() {
		boolean validMove = (absColDisp <= 1 && absRowDisp == 1);
		validMove |= (absColDisp == 0 && absRowDisp == 2);
		validMove &= (isWhite ? 1 : -1) * rowDisplacement > 0;

		if (!validMove) {
			return false;
		}

		if (absColDisp == 1 && absRowDisp == 1) {
			if (gamePlay.getEnPassantTargetSquare() != null && gamePlay.getEnPassantTargetSquare().equals(endPos)) {
				moveResponseDTO.addChessMove(ChessMove.EN_PASSANT);
				moveResponseDTO.addChessMove(ChessMove.CAPTURE);
				String[] extraMove = { GameBoardServices.getSquare(startRow, endCol), null };
				moveResponseDTO.addPieceMove(extraMove);
			} else if (attackedPiece != null) {
				moveResponseDTO.addChessMove(ChessMove.CAPTURE);
			} else {
				return false;
			}
		}

		if (moveDTO.getPromotionPiece() != null) {
			evaluatePawnPromotion();
		}

		gamePlay.setEnPassantTargetSquare(null);

		// modified rules allowing only pawns to jump forward over obstructive cats 
		if (absColDisp == 0 && absRowDisp == 2) {
			if (mockBoard[startRow+(isWhite ? -1 : 1)][startCol] == PieceNotation.C && attackedPiece == null) {
				moveResponseDTO.addChessMove(ChessMove.SIMPLE_MOVE);
			} else if ((startRow == 1 || startRow == 6) && attackedPiece == null) {
				moveResponseDTO.addChessMove(ChessMove.SIMPLE_MOVE);
				gamePlay.setEnPassantTargetSquare(GameBoardServices.getSquare(startRow + (isWhite ? -1 : 1), startCol));
			} else {
				return false;
			}
		}

		if (absColDisp == 0 && absRowDisp == 1) {
			if (attackedPiece == null) {
				moveResponseDTO.addChessMove(ChessMove.SIMPLE_MOVE);
			} else {
				return false;
			}
		}

		return true;
	}
	
}
