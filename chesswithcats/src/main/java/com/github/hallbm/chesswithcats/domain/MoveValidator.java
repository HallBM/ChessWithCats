package com.github.hallbm.chesswithcats.domain;

import java.util.ArrayList;
import java.util.List;

import com.github.hallbm.chesswithcats.domain.GameEnums.ChessMove;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameColor;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameOutcome;
import com.github.hallbm.chesswithcats.domain.GameEnums.PieceNotation;
import com.github.hallbm.chesswithcats.domain.GameEnums.PieceType;
import com.github.hallbm.chesswithcats.dto.MoveDTO;
import com.github.hallbm.chesswithcats.dto.MoveResponseDTO;
import com.github.hallbm.chesswithcats.model.GamePlay;
import com.github.hallbm.chesswithcats.service.GameBoardServices;

import lombok.Getter;
import lombok.Setter;

/**
 * Class validates attempted move based on given game state (gamePlay) and
 * attempted move (moveDTO) which are passed in to the constructor.
 * 
 * Constructor expands variables inferred from moveDTO (row/col number,
 * calculated displacement), and creates deep copy of chess board from gamePlay
 * (PieceNotation [][]) to simulate game move and evaluate
 * check/checkmate/stalemate without affecting stored gameboard.
 * 
 * 'validate' method as an entry point into the validator; interprets and
 * coordinates flow through available methods based on game piece moved. Returns
 * MoveResponseDTO object populated with fields, or if move is invalid returns a
 * new MoveResponseDTO object where the 'isValidMove' boolean is set to false to
 * be interpreted by game controller.
 * 
 * MoveValidator used for classic chess game. This class extended by other
 * validators to override methods in order to implement new chess rules.
 * 
 */

@Getter
@Setter
public class MoveValidator {

	protected MoveResponseDTO moveResponseDTO = new MoveResponseDTO();
	protected GamePlay gamePlay;
	protected List<ChessMove> chessMoves = new ArrayList<>();

	protected String startPos, endPos;
	protected int startRow, endRow, startCol, endCol;
	protected int colDisplacement, rowDisplacement, absRowDisp, absColDisp;
	protected String nextEnPassantTargetSquare = null;

	protected PieceNotation[][] mockBoard;
	protected PieceNotation movedPiece;
	protected boolean isWhiteMove;

	/**
	 * Entry point into move validation and composition of MoveResponseDTO object:
	 * 1) validates move based on allowed piece movements. 2) if plausible move,
	 * simulates chess move on 'mockBoard' (deep copy of persisted game state) 3)
	 * checks if move resulted in player's king being in check 4) evaluate whether
	 * other king has been placed in check 5) if moves are not valid, return null
	 * moveResponseDTO, otherwise return populated DTO.
	 */
	public MoveResponseDTO validate(MoveDTO moveDTO, GamePlay gp) {
		gamePlay = gp;
		mockBoard = gp.getGameBoard().getBoard();
		loadMove(moveDTO);

		if (moveDTO.getPromotionPiece() != null) {
			if (!("RNBQrnbq".contains(moveDTO.getPromotionPiece())))
				return null;
			if (!(isWhiteMove ? endRow == 7 : endRow == 0))
				return null;
		}

		if (!isValidTurn() || !isValidMove())
			return null;

		generateMoveResponse();

		mockBoard = GameBoardServices.simulateMove(mockBoard, moveResponseDTO.getPieceMoves(),
				moveDTO.getPromotionPiece());

		if (movedPiece.getType() == PieceType.KING) {
			if (isKingInCheck(new int[] { endRow, endCol }, getPlayerColor()))
				return null;
		} else {
			if (isKingInCheck(getPlayerColor()))
				return null;
		}

		if (isKingInCheck(getOpponentColor())) {
			chessMoves.add(ChessMove.CHECK);
			moveResponseDTO.setGameOutcome(GameOutcome.CHECKMATE); // TODO
		}

		generateOfficialMove(moveDTO);

		// TODO check checkmate, stalemate, update DTO, update official move #; check =>
		// checkmate as POC

		return moveResponseDTO;
	}

	protected GameColor getPlayerColor() {
		return isWhiteMove ? GameColor.WHITE : GameColor.BLACK;
	}

	protected GameColor getOpponentColor() {
		return isWhiteMove ? GameColor.BLACK : GameColor.WHITE;
	}

	protected void loadMove(MoveDTO moveDTO) {
		startPos = moveDTO.getStartPos();
		endPos = moveDTO.getEndPos();
		startRow = GameBoardServices.getRow(startPos);
		endRow = GameBoardServices.getRow(endPos);
		startCol = GameBoardServices.getColumn(startPos);
		endCol = GameBoardServices.getColumn(endPos);
		colDisplacement = 1 * (endCol - startCol);
		rowDisplacement = -1 * (endRow - startRow);
		absRowDisp = Math.abs(rowDisplacement);
		absColDisp = Math.abs(colDisplacement);

		movedPiece = mockBoard[startRow][startCol];
		isWhiteMove = movedPiece.getColor() == GameColor.WHITE;

		nextEnPassantTargetSquare = null;
	}

	protected boolean isValidTurn() {
		return (gamePlay.getHalfMoves() % 2 == 1) == isWhiteMove;
	}

	protected boolean isValidMove() {
		switch (movedPiece.getType()) {
		case ROOK:
			return validateRookMove();
		case KNIGHT:
			return validateKnightMove();
		case BISHOP:
			return validateBishopMove();
		case QUEEN:
			return validateQueenMove();
		case PAWN:
			return validatePawnMove();
		case KING:
			return validateKingMove();
		default:
			return false;
		}
	}

	/**
	 * Functions for evaluating whether a moved piece along a row in the indicated
	 * direction (according to white player perspective) is attempting to pass
	 * through pieces on the board.
	 * 
	 * @param sign +1 for right, -1 for left
	 * @return true for valid move (not obstructed), false for invalid move
	 *         (obstructed).
	 */
	protected boolean isUnobstructedMove() {
		int rowSign = Integer.signum(rowDisplacement);
		int colSign = Integer.signum(colDisplacement);
		int disp = Math.max(absColDisp, absRowDisp);

		for (int i = 1; i < disp; i++) {
			if (mockBoard[startRow - i * rowSign][startCol + i * colSign] != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Logic for classic rook move along row or column.
	 */
	protected boolean validateRookMove() {
		if ((absRowDisp == 0) == (absColDisp == 0))
			return false;
		return isUnobstructedMove();
	}

	/**
	 * Logic for classic knight move in L shape.
	 */
	protected boolean validateKnightMove() {
		return (absRowDisp == 1 && absColDisp == 2) || (absRowDisp == 2 && absColDisp == 1);
	}

	/**
	 * Logic for classic bishop move along diagonal.
	 */
	protected boolean validateBishopMove() {
		if (absRowDisp != absColDisp)
			return false;
		return isUnobstructedMove();
	}

	/**
	 * Logic for classic queen move along row, column or diagonal.
	 */
	protected boolean validateQueenMove() {
		if ((absRowDisp == 0) == (absColDisp == 0) == (absRowDisp != absColDisp))
			return false;
		return isUnobstructedMove();
	}

	/**
	 * Logic for classic pawn moves. 1) Check if move is an attack or en passant
	 * capture. 2) Check whether move from starting position triggers an en passant
	 * attack on next move. 3) Check if move is simple forward move in column.
	 */
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
				moveResponseDTO.addPieceMove(new String[] { GameBoardServices.getSquare(startRow, endCol), "ep" });
			} else {
				return false;
			}
		} else if (absColDisp == 0 && absRowDisp == 2) {
			if (isValidPawnInitialDouble()) {
				chessMoves.add(ChessMove.PAWN_INITIAL_DOUBLE);
				nextEnPassantTargetSquare = GameBoardServices.getSquare(startRow + (isWhiteMove ? -1 : 1), startCol);
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

	protected boolean isValidPawnMovement() {
		return (absColDisp <= 1 && absRowDisp == 1) || (absColDisp == 0 && absRowDisp == 2);
	}

	protected boolean isValidPawnDirection() {
		return (isWhiteMove ? 1 : -1) * rowDisplacement > 0;
	}

	protected boolean isValidEnPassantAttack() {
		return gamePlay.getEnPassantTargetSquare() != null && gamePlay.getEnPassantTargetSquare().equals(endPos);
	}

	protected boolean isValidPawnInitialDouble() {
		return (startRow == 1 || startRow == 6) && !isPieceAttack()
				&& mockBoard[startRow + (isWhiteMove ? -1 : 1)][startCol] == null;
	}

	protected boolean isPieceAttack() {
		return mockBoard[endRow][endCol] != null;
	}

	protected boolean isValidKingMove() {
		return (absColDisp <= 1 && absRowDisp <= 1) || (absColDisp == 2 && absRowDisp == 0);
	}

	/**
	 * Logic for classic king move, including castling if available. Moving king
	 * directly validates whether king will be in check, and for castling, whether
	 * piece is in check at any point along castling move transition. Updates
	 * castling rules accordingly within gamePlay object.
	 */
	protected boolean validateKingMove() {

		if (!isValidKingMove())
			return false;

		if (absColDisp == 2)
			return validateCastlingMove();

		return true;
	}

	protected boolean validateCastlingMove() {

		if (gamePlay.getCastling() == null)
			return false;

		switch (startPos + endPos) {
		case "E1C1":
			if (!gamePlay.getCastling().contains("Q"))
				return false;
			chessMoves.add(ChessMove.QUEEN_SIDE_CASTLE);
			moveResponseDTO.addPieceMove(new String[] { "A1", "D1" });
			break;
		case "E1G1":
			if (!gamePlay.getCastling().contains("K"))
				return false;
			chessMoves.add(ChessMove.KING_SIDE_CASTLE);
			moveResponseDTO.addPieceMove(new String[] { "H1", "F1" });
			break;
		case "E8C8":
			if (!gamePlay.getCastling().contains("q"))
				return false;
			chessMoves.add(ChessMove.QUEEN_SIDE_CASTLE);
			moveResponseDTO.addPieceMove(new String[] { "A8", "D8" });
			break;
		case "E8G8":
			if (!gamePlay.getCastling().contains("k"))
				return false;
			chessMoves.add(ChessMove.KING_SIDE_CASTLE);
			moveResponseDTO.addPieceMove(new String[] { "H8", "F8" });
			break;
		default:
			return false;
		}

		int colDirection = Integer.signum(colDisplacement);

		if (isCastlingObstructed(colDirection))
			return false;

		if (isKingInCheckDuringCastle(colDirection))
			return false;

		return true;
	}

	protected boolean isCastlingObstructed(int colDirection) {
		if (colDirection == -1) {
			for (int i = 1; i <= 2; i++) {
				if (mockBoard[startRow][startCol - i] != null) {
					return true;
				}
			}
		} else if (colDirection == 1) {
			for (int i = 1; i <= 1; i++) {
				if (mockBoard[startRow][startCol + i] != null) {
					return true;
				}
			}
		} else {
			return true;
		}

		return false;
	}

	// Checks starting position and transition for check (not final position)
	protected boolean isKingInCheckDuringCastle(int colDirection) {
		for (int i = 0; i <= 2; i++) {
			if (isKingInCheck(new int[] { startRow, startCol + i * colDirection }, getPlayerColor())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Updates MoveResponseDTO with move validity and piece movement(s).
	 */
	protected void generateMoveResponse() {
		String[] move = { startPos, endPos };
		moveResponseDTO.addPieceMove(move);

		if (movedPiece.getType() != PieceType.PAWN) { // handled explicitly in pawn move logic
			chessMoves.add(isPieceAttack() ? ChessMove.CAPTURE : ChessMove.SIMPLE_MOVE);
		}
	}

	/**
	 * Generates String of extended chess move notation for tracking game history.
	 * MoveResponseDTO object updated with official chess move.
	 */
	protected void generateOfficialMove(MoveDTO moveDTO) {
		String move = "";

		if (gamePlay.getHalfMoves() % 2 == 1) {
			move += String.valueOf((gamePlay.getHalfMoves() - 1) / 2 + 1) + ".";
		}

		if (chessMoves.contains(ChessMove.KING_SIDE_CASTLE)) {
			move += "O-O" + (chessMoves.contains(ChessMove.CHECK) ? "+ " : " ");
		} else if (chessMoves.contains(ChessMove.QUEEN_SIDE_CASTLE)) {
			move += "O-O-O" + (chessMoves.contains(ChessMove.CHECK) ? "+ " : " ");
		} else {
			move += movedPiece.toString() + startPos.toLowerCase() + (chessMoves.contains(ChessMove.CAPTURE) ? "x" : "")
					+ endPos.toLowerCase() + (chessMoves.contains(ChessMove.EN_PASSANT_CAPTURE) ? "ep" : "")
					+ (moveDTO.getPromotionPiece() != null ? "=" + moveDTO.getPromotionPiece() : "")
					+ (chessMoves.contains(ChessMove.CHECK) ? "+ " : " ");
		}
		moveResponseDTO.setMoveNotation(move);
	}

	/**
	 * Evaluates whether the king of the indicated color is in check. Finds piece
	 * via GameBoardServices
	 */
	protected boolean isKingInCheck(GameColor kingColor) {
		PieceNotation king = PieceNotation.valueOf(kingColor == GameColor.WHITE ? "K" : "k");
		int[] kingPos = GameBoardServices.findKingPosition(mockBoard, king);

		return isKingInCheck(kingPos, kingColor);
	}

	/**
	 * Checks whether the king of the indicated color and position is under attack
	 * in any direction and within reach of knight.
	 */
	protected boolean isKingInCheck(int[] kingPos, GameColor kingColor) {

		if (isKingUnderAttackByKnight(kingPos, kingColor))
			return true;
		if (isKingUnderStraightLineAttack(kingPos, kingColor))
			return true;
		if (isKingUnderDiagonalLineAttack(kingPos, kingColor))
			return true;

		return false;
	}

	/**
	 * Knight-specific logic for evaluating whether the king is in check.
	 */
	protected boolean isKingUnderAttackByKnight(int[] kingPos, GameColor kingColor) {
		int row = kingPos[0];
		int col = kingPos[1];
		int[][] indices = { { 1, 2 }, { 2, 1 }, { -1, -2 }, { -2, -1 }, { 1, -2 }, { -2, 1 }, { -1, 2 }, { 2, -1 } };

		for (int[] i : indices) {
			if (isPositionOnBoard(row + i[0], col + i[1])) {
				PieceNotation attackPiece = mockBoard[row + i[0]][col + i[1]];
				if (attackPiece != null) {
					if (attackPiece.getColor() == kingColor)
						continue;
					if (attackPiece.getType() == PieceType.KNIGHT)
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Row/Col-specific logic for evaluating whether the king is in check. Looks for
	 * unobstructed opposite-color queen, rook, nearby king.
	 */
	protected boolean isKingUnderStraightLineAttack(int[] kingPos, GameColor kingColor) {
		int row = kingPos[0];
		int col = kingPos[1];
		int[][] indices = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

		for (int[] i : indices) {
			int count = 1;
			while (isPositionOnBoard(row + i[0] * count, col + i[1] * count)) {
				PieceNotation attackPiece = mockBoard[row + i[0] * count][col + i[1] * count];
				if (attackPiece != null) {
					if (attackPiece.getColor() == kingColor)
						break;
					if (attackPiece.getType() == PieceType.QUEEN)
						return true;
					if (attackPiece.getType() == PieceType.ROOK)
						return true;
					if (attackPiece.getType() == PieceType.KING
							&& (Math.abs(i[0] * count) == 1 || Math.abs(i[1] * count) == 1)) {
						return true;
					}
					break;
				}
				count++;
			}
		}
		return false;
	}

	/**
	 * Diagonal-specific logic for evaluating whether the king is in check. Looks
	 * for unobstructed opposite-color queen, bishop, nearby king or nearby pawn (on
	 * attacking diagonal).
	 */
	protected boolean isKingUnderDiagonalLineAttack(int[] kingPos, GameColor kingColor) {
		int row = kingPos[0];
		int col = kingPos[1];
		int[][] indices = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };

		for (int[] i : indices) {
			int count = 1;
			while (isPositionOnBoard(row + i[0] * count, col + i[1] * count)) {
				PieceNotation attackPiece = mockBoard[row + i[0] * count][col + i[1] * count];
				if (attackPiece != null) {
					if (attackPiece.getColor() == kingColor)
						break;
					if (attackPiece.getType() == PieceType.QUEEN)
						return true;
					if (attackPiece.getType() == PieceType.BISHOP)
						return true;
					if (attackPiece.getType() == PieceType.KING
							&& (Math.abs(i[0] * count) == 1 || Math.abs(i[1] * count) == 1)) {
						return true;
					}
					if (attackPiece.getType() == PieceType.PAWN
							&& (row + (kingColor == GameColor.WHITE ? -1 : 1)) == (row + i[0] * count)) {
						return true;
					}
					break;
				}
				count++;
			}
		}
		return false;
	}

	protected boolean isPositionOnBoard(int row, int col) {
		return row >= 0 && row <= 7 && col >= 0 && col <= 7;
	}

}
