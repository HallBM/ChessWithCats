package com.github.hallbm.chesswithcats.domain;

import com.github.hallbm.chesswithcats.domain.GameEnums.ChessMove;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameColor;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameOutcome;
import com.github.hallbm.chesswithcats.domain.GameEnums.PieceNotation;
import com.github.hallbm.chesswithcats.domain.GameEnums.PieceType;
import com.github.hallbm.chesswithcats.dto.MoveDTO;
import com.github.hallbm.chesswithcats.dto.MoveResponseDTO;
import com.github.hallbm.chesswithcats.model.GamePlay;
import com.github.hallbm.chesswithcats.service.GameBoardServices;

/**
 * Class validates attempted move based on given game state (gamePlay) and attempted move (moveDTO)
 * which are passed in to the constructor.
 *  
 * Constructor expands variables inferred from moveDTO (row/col number, calculated displacement),
 * and creates deep copy of chess board from gamePlay (PieceNotation [][]) to 
 * simulate game move and evaluate check/checkmate/stalemate without affecting stored gameboard.
 * 
 * 'validate' method as an entry point into the validator; interprets and coordinates flow
 * through available methods based on game piece moved. Returns MoveResponseDTO object populated with fields, 
 * or if move is invalid returns a new MoveResponseDTO object where the 'isValidMove' boolean is set to false
 * to be interpreted by game controller.
 * 
 * MoveValidator used for classic chess game. This class extended by other validators to
 * override methods in order to implement new chess rules.
 * 
 */

public class MoveValidator {

	protected GamePlay gamePlay;
	protected MoveDTO moveDTO;
	protected MoveResponseDTO moveResponseDTO = new MoveResponseDTO();

	protected String startPos, endPos;
	protected int startRow, endRow, startCol, endCol;
	protected int colDisplacement, rowDisplacement, absRowDisp, absColDisp;

	protected PieceNotation[][] mockBoard;
	protected PieceNotation movedPiece, attackedPiece;
	protected boolean isWhite;

	/**
	 * Constructor with gamePlay and moveDTO parameters.
	 * All other fields generated from these objects.
	 */  
	
	public MoveValidator(GamePlay gamePlay, MoveDTO moveDTO) {
		this.gamePlay = gamePlay;
		this.moveDTO = moveDTO;

		startPos = moveDTO.getStartPos();
		endPos = moveDTO.getEndPos();
		startRow = GameBoardServices.getRow(startPos);
		endRow = GameBoardServices.getRow(endPos);
		startCol = GameBoardServices.getColumn(startPos);
		endCol = GameBoardServices.getColumn(endPos);

		mockBoard = GameBoardServices.copyBoard(gamePlay.getGameBoard().getBoard());
		movedPiece = mockBoard[startRow][startCol];
		attackedPiece = mockBoard[endRow][endCol];
		isWhite = movedPiece.getColor() == GameColor.WHITE;
	
		colDisplacement = 1 * (endCol - startCol);
		rowDisplacement = -1 * (endRow - startRow);
		absRowDisp = Math.abs(rowDisplacement);
		absColDisp = Math.abs(colDisplacement);
	}

	/**
	 * Entry point into move validation and composition of MoveResponseDTO object:
	 * 1) validates move based on allowed piece movements.
	 * 2) if plausible move, simulates chess move on 'mockBoard' (deep copy of persisted game state)
	 * 3) checks if move resulted in player's king being in check
	 * 4) evaluate whether other king has been placed in check
	 * 5) if moves are not valid, return null moveResponseDTO, otherwise return populated DTO.
	 */  

	public MoveResponseDTO validate() {
		boolean isPlausibleMove = false;

		switch (movedPiece.getType()) {
		case ROOK:
			isPlausibleMove = checkRookMove();
			break;
		case KNIGHT:
			isPlausibleMove = checkKnightMove();
			break;
		case BISHOP:
			isPlausibleMove = checkBishopMove();
			break;
		case QUEEN:
			isPlausibleMove = checkQueenMove();
			break;
		case PAWN:
			isPlausibleMove = checkPawnMove();
			break;
		case KING:
			isPlausibleMove = checkKingMove();
			break;
		case CAT:
		default:
			break;
		}
		if (!isPlausibleMove) {
			return null;
		}
		
		generateMoveResponse();

		GameBoardServices.movePiece(mockBoard, moveResponseDTO.getPieceMoves(), moveDTO.getPromotionPiece());

		if (!checkNotInCheck(isWhite ? GameColor.WHITE : GameColor.BLACK)) {
			return null;
		}

		moveResponseDTO.setChecked(!checkNotInCheck(isWhite ? GameColor.BLACK : GameColor.WHITE));
		generateOfficialMove();

		//TODO check checkmate, stalemate, update DTO, update official move #; check => checkmate as POC
		if (moveResponseDTO.isChecked()) {
			moveResponseDTO.setGameOutcome(GameOutcome.CHECKMATE);
		}
		
		return moveResponseDTO;
	}

	/**
	 * Navigation functions for evaluating whether a moved piece
	 * in the indicated direction (according to white player perspective) 
	 * is attempting to pass through pieces on the board.
	 * 
	 * Returns true if move is okay, false if not valid (obstructed).
	 */  
	protected boolean navigateRight() {
		for (int i = 1; i < absColDisp; i++) {
			if (mockBoard[startRow][startCol + i] != null) {
				return false;
			}
		}
		return true;
	}

	protected boolean navigateLeft() {
		for (int i = 1; i < absColDisp; i++) {
			if (mockBoard[startRow][startCol - i] != null) {
				return false;
			}
		}
		return true;
	}

	protected boolean navigateUp() {
		for (int i = 1; i < absRowDisp; i++) {
			if (mockBoard[startRow - i][startCol] != null) {
				return false;
			}
		}
		return true;
	}

	protected boolean navigateDown() {
		for (int i = 1; i < absRowDisp; i++) {
			if (mockBoard[startRow + i][startCol] != null) {
				return false;
			}
		}
		return true;
	}

	protected boolean navigateUpperRight() {
		for (int i = 1; i < absRowDisp; i++) {
			if (mockBoard[startRow - i][startCol + i] != null) {
				return false;
			}
		}
		return true;
	}

	protected boolean navigateLowerRight() {
		for (int i = 1; i < absRowDisp; i++) {
			if (mockBoard[startRow + i][startCol + i] != null) {
				return false;
			}
		}
		return true;
	}

	protected boolean navigateUpperLeft() {
		for (int i = 1; i < absRowDisp; i++) {
			if (mockBoard[startRow - i][startCol - i] != null) {
				return false;
			}
		}
		return true;
	}

	protected boolean navigateLowerLeft() {
		for (int i = 1; i < absRowDisp; i++) {
			if (mockBoard[startRow + i][startCol - i] != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Logic for classic rook move along row or column.
	 */  
	protected boolean checkRookMove() {
		boolean validMove = (absRowDisp == 0) != (absColDisp == 0);
		if (!validMove) {
			return false;
		}

		if (rowDisplacement == 0) {
			validMove = colDisplacement > 0 ? navigateRight() : navigateLeft();
		} else {
			validMove = rowDisplacement > 0 ? navigateUp() : navigateDown();
		}

		if (!validMove) {
			return false;
		}

		switch (startPos) {
		case "A1":
			gamePlay.removeCastling("Q");
			break;
		case "H1":
			gamePlay.removeCastling("K");
			break;
		case "A8":
			gamePlay.removeCastling("q");
			break;
		case "H8":
			gamePlay.removeCastling("k");
			break;
		default:
			break;
		}

		return true;
	}

	/**
	 * Logic for classic knight move in L shape.
	 */ 
	protected boolean checkKnightMove() {
		boolean validMove = (absRowDisp == 1 && absColDisp == 2);
		validMove |= (absRowDisp == 2 && absColDisp == 1);

		if (!validMove) {
			return false;
		}

		return true;
	}

	/**
	 * Logic for classic bishop move along diagonal.
	 */ 
	protected boolean checkBishopMove() {
		boolean validMove = absRowDisp == absColDisp;
		if (!validMove) {
			return false;
		}

		if (rowDisplacement > 0) {
			validMove = colDisplacement > 0 ? navigateUpperRight() : navigateUpperLeft();
		} else {
			validMove = colDisplacement > 0 ? navigateLowerRight() : navigateLowerLeft();
		}

		if (!validMove) {
			return false;
		}

		return true;
	}

	/**
	 * Logic for classic queen move along row, column or diagonal.
	 */ 
	protected boolean checkQueenMove() {
		boolean validMove = ((absRowDisp == 0) != (absColDisp == 0)) || (absRowDisp == absColDisp);
		if (!validMove) {
			return false;
		}

		if ((absRowDisp == 0) != (absColDisp == 0)) {
			if (rowDisplacement == 0) {
				validMove = colDisplacement > 0 ? navigateRight() : navigateLeft();
			} else {
				validMove = rowDisplacement > 0 ? navigateUp() : navigateDown();
			}
		} else {
			if (rowDisplacement > 0) {
				validMove = colDisplacement > 0 ? navigateUpperRight() : navigateUpperLeft();
			} else {
				validMove = colDisplacement > 0 ? navigateLowerRight() : navigateLowerLeft();
			}
		}

		if (!validMove) {
			return false;
		}

		return true;
	}
	
	/**
	 * Logic for classic pawn moves.
	 * 1) Check if move is an attack or en passant capture.
	 * 2) Check if piece is promoted to a queen, knight, rook, bishop. TODO: logic not fully implemented.
	 * 3) Check whether move from starting position triggers an en passant attack on next move.
	 * 4) Check if move is simple forward move in column
	 */ 
	protected boolean checkPawnMove() {
		boolean validMove = (absColDisp <= 1 && absRowDisp == 1); 	//forward move +/- attack
		validMove |= (absColDisp == 0 && absRowDisp == 2); 			// opening pawn move
		validMove &= (isWhite ? 1 : -1) * rowDisplacement > 0; 		// move is 'forward' (towards opposing side)

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

		if (absColDisp == 0 && absRowDisp == 2) {
			if ((startRow == 1 || startRow == 6) && attackedPiece == null) {
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

	/**
	 * Pawn promotion update of MoveResponseDTO.
	 */ 
	protected void evaluatePawnPromotion() {
		switch (moveDTO.getPromotionPiece().toString().toLowerCase()) {
		case "r":
			moveResponseDTO.addChessMove(ChessMove.PROMOTE_ROOK);
			break;
		case "b":
			moveResponseDTO.addChessMove(ChessMove.PROMOTE_BISHOP);
			break;
		case "n":
			moveResponseDTO.addChessMove(ChessMove.PROMOTE_KNIGHT);
			break;
		case "q":
			moveResponseDTO.addChessMove(ChessMove.PROMOTE_QUEEN);
			break;
		}
	}

	/**
	 * Logic for classic king move, including castling if available.
	 * Moving king directly validates whether king will be in check,
	 * and for castling, whether piece is in check at any point along
	 * castling move transition.
	 * Updates castling rules accordingly within gamePlay object.
	 */ 
	protected boolean checkKingMove() {
		boolean validMove = (absColDisp == 1 || absRowDisp == 1) || (absColDisp == 2 && absRowDisp == 0);

		if (!validMove) {
			return false;
		}

		if (absColDisp == 2 && !gamePlay.getCastling().equals("")) {
			String castling = gamePlay.getCastling();
			int colDirection = 0;
			String[] extraMove;
			switch (startPos + endPos) {
			case "E1C1":
				colDirection = castling.contains("Q") ? -1 : 0;
				moveResponseDTO.addChessMove(ChessMove.QUEEN_SIDE_CASTLE);
				extraMove = new String[] { "A1", "D1" };
				moveResponseDTO.addPieceMove(extraMove);
				break;
			case "E1G1":
				colDirection = castling.contains("K") ? 1 : 0;
				moveResponseDTO.addChessMove(ChessMove.KING_SIDE_CASTLE);
				extraMove = new String[] { "H1", "F1" };
				moveResponseDTO.addPieceMove(extraMove);
				break;
			case "E8C8":
				colDirection = castling.contains("q") ? -1 : 0;
				moveResponseDTO.addChessMove(ChessMove.QUEEN_SIDE_CASTLE);
				extraMove = new String[] { "A8", "D8" };
				moveResponseDTO.addPieceMove(extraMove);
				break;
			case "E8G8":
				colDirection = castling.contains("k") ? 1 : 0;
				moveResponseDTO.addChessMove(ChessMove.KING_SIDE_CASTLE);
				extraMove = new String[] { "H8", "F8" };
				moveResponseDTO.addPieceMove(extraMove);
				break;
			default:
				return false;
			}
			if (colDirection == -1) {
				for (int i = 1; i <= 3; i++) {
					if (mockBoard[startRow][startCol - i] != null) {
						return false;
					}
				}
			} else if (colDirection == 1) {
				for (int i = 1; i <= 2; i++) {
					if (mockBoard[startRow][startCol + i] != null) {
						return false;
					}
				}
			} else {
				return false;
			}
			for (int i = 0; i <= 2; i++) {
				if (!checkNotInCheck(new int[] { startRow, startCol + i * colDirection },
						isWhite ? GameColor.WHITE : GameColor.BLACK)) {
					return false;
				}
			}
		}

		if (isWhite) {
			gamePlay.removeCastling("K");
			gamePlay.removeCastling("Q");
		} else {
			gamePlay.removeCastling("k");
			gamePlay.removeCastling("q");
		}

		return true;
	}
	
	/**
	 * Updates MoveResponseDTO with move validity and piece movement(s).
	 */ 
	protected void generateMoveResponse() {
		moveResponseDTO.setValid(true);
		String[] move = { startPos, endPos };
		moveResponseDTO.addPieceMove(move);

		if (movedPiece.getType() != PieceType.PAWN) { // handled explicitly in pawn move logic
			moveResponseDTO.addChessMove(attackedPiece == null ? ChessMove.SIMPLE_MOVE : ChessMove.CAPTURE);
			gamePlay.setEnPassantTargetSquare(null);
		}
	}

	/**
	 * Generates String of extended chess move notation for tracking game history.
	 * MoveResponseDTO object updated with official chess move.
	 */ 
	protected void generateOfficialMove() {
		String move = ""; 
				
		if (gamePlay.getHalfMoves() % 2 == 1) {
			move += String.valueOf((gamePlay.getHalfMoves() - 1) / 2 + 1) + ".";
		}
		
		if (moveResponseDTO.getChessMoves().contains(ChessMove.KING_SIDE_CASTLE)) {
			move += "O-O" + (moveResponseDTO.isChecked() ? "+ " : " ");
		} else if (moveResponseDTO.getChessMoves().contains(ChessMove.QUEEN_SIDE_CASTLE)) {
			move += "O-O-O" + (moveResponseDTO.isChecked() ? "+ " : " ");
		} else {
			move += movedPiece.toString() + startPos.toLowerCase()
					+ (moveResponseDTO.getChessMoves().contains(ChessMove.CAPTURE) ? "x" : "") + endPos.toLowerCase()
					+ (moveResponseDTO.getChessMoves().contains(ChessMove.EN_PASSANT) ? "ep" : "")
					+ (moveDTO.getPromotionPiece() != null ? "=" + moveDTO.getPromotionPiece().toString() : "")
					+ (moveResponseDTO.isChecked() ? "+ " : " ");
		}
		
		moveResponseDTO.setOfficialChessMove(move);
	}

	/**
	 * Evaluates whether the king of the indicated color is in check.
	 * Finds piece via GameBoardServices
	 */ 
	protected boolean checkNotInCheck(GameColor kingColor) {
		PieceNotation king = PieceNotation.valueOf(kingColor == GameColor.WHITE ? "K" : "k");
		int[] kingPos = GameBoardServices.findKingPosition(mockBoard, king);

		return checkNotInCheck(kingPos, kingColor);
	}
	
	/**
	 * Checks whether the king of the indicated color and position is under attack
	 * in any direction and within reach of knight.
	 */ 
	protected boolean checkNotInCheck(int[] kingPos, GameColor kingColor) {

		if (!checkNoKnightAttack(kingPos, kingColor)) {
			return false;
		}
		if (!checkNoFileAttack(kingPos, kingColor)) {
			return false;
		}
		if (!checkNoDiagonalAttack(kingPos, kingColor)) {
			return false;
		}
		return true;
	}

	/**
	 * Knight-specific logic for evaluating whether the king is in check.
	 */ 
	protected boolean checkNoKnightAttack(int[] kingPos, GameColor kingColor) {

		int r = kingPos[0];
		int c = kingPos[1];
		int[][] index = { { 1, 2 }, { 2, 1 }, { -1, -2 }, { -2, -1 }, { 1, -2 }, { -2, 1 }, { -1, 2 }, { 2, -1 } };

		for (int[] i : index) {
			if ((r + i[0] >= 0) && (r + i[0] <= 7) && (c + i[1] >= 0) && (c + i[1] <= 7)) {
				PieceNotation attackPiece = mockBoard[r + i[0]][c + i[1]];
				if (attackPiece != null && attackPiece.getColor() != kingColor
						&& attackPiece.getType() == PieceType.KNIGHT) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Row/Col-specific logic for evaluating whether the king is in check.
	 * Looks for un-obstructed opposite-color queen, rook, nearby king.
	 */ 
	protected boolean checkNoFileAttack(int[] kingPos, GameColor kingColor) {
		int r = kingPos[0];
		int c = kingPos[1];
		int[][] index = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

		for (int[] i : index) {
			int count = 1;
			while ((r + i[0] * count >= 0) && (r + i[0] * count <= 7) && (c + i[1] * count >= 0)
					&& (c + i[1] * count <= 7)) {
				PieceNotation attackPiece = mockBoard[r + i[0] * count][c + i[1] * count];

				if (attackPiece != null) {
					boolean isNotSameColor = attackPiece.getColor() != kingColor;
					boolean isQueenOrRook = attackPiece.getType() == PieceType.QUEEN
							|| attackPiece.getType() == PieceType.ROOK;
					boolean isCloseKing = (attackPiece.getType() == PieceType.KING)
							&& ((Math.abs(i[0]*count) == 1) || (Math.abs(i[1] * count) == 1));

					if (isNotSameColor && (isQueenOrRook || isCloseKing)) {
						return false;
					} else {
						break;
					}
				}
				count++;
			}
		}
		return true;
	}
	
	/**
	 * Diagonal-specific logic for evaluating whether the king is in check.
	 * Looks for un-obstructed opposite-color queen, bishop, nearby king or nearby pawn (on attacking diagonal).
	 */ 
	protected boolean checkNoDiagonalAttack(int[] kingPos, GameColor kingColor) {
		int r = kingPos[0];
		int c = kingPos[1];
		int[][] index = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };

		for (int[] i : index) {
			int count = 1;
			while ((r + i[0] * count >= 0) && (r + i[0] * count <= 7) && (c + i[1] * count >= 0)
					&& (c + i[1] * count <= 7)) {
				PieceNotation attackPiece = mockBoard[r + i[0] * count][c + i[1] * count];
				if (attackPiece != null) {
					boolean isNotSameColor = attackPiece.getColor() != kingColor;
					boolean isQueenOrBishop = attackPiece.getType() == PieceType.QUEEN
							|| attackPiece.getType() == PieceType.BISHOP;
					boolean isCloseKing = (attackPiece.getType() == PieceType.KING)
							&& ((Math.abs(i[0]*count) == 1) || (Math.abs(i[1] * count) == 1));
					boolean isClosePawn = (attackPiece.getType() == PieceType.PAWN)
							&& (r + (kingColor == GameColor.WHITE ? -1 : 1)) == (r + i[0] * count);

					if (isNotSameColor && (isQueenOrBishop || isCloseKing || isClosePawn)) {
						return false;
					} else {
						break;
					}
				}
				count++;
			}
		}
		return true;
	}
}
