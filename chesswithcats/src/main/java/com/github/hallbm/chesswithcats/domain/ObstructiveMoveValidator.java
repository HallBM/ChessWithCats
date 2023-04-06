package com.github.hallbm.chesswithcats.domain;

import java.util.ArrayList;
import java.util.List;

import com.github.hallbm.chesswithcats.domain.GameEnums.ChessMove;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameColor;
import com.github.hallbm.chesswithcats.domain.GameEnums.PieceMovement;
import com.github.hallbm.chesswithcats.domain.GameEnums.PieceNotation;
import com.github.hallbm.chesswithcats.domain.GameEnums.PieceType;
import com.github.hallbm.chesswithcats.service.GameBoardServices;

public class ObstructiveMoveValidator extends MoveValidator {

	@Override
	protected boolean isValidPawnMove(PieceNotation[][] board, int[] start, int[] end, int rowDisp, int colDisp,
			boolean isWhiteMove, boolean isAttack, String enPassantSquare) {

		int absRowDisp = Math.abs(rowDisp);
		int absColDisp = Math.abs(colDisp);

		if (!isValidPawnDirection(rowDisp, isWhiteMove) || absRowDisp > 2 || absColDisp > 1)
			return false;

		if (isValidEnPassantCapture(absRowDisp, absColDisp, isAttack, moveDTO.getEndPos(), enPassantSquare)) {
			pieceMoves.add(new String[] { GameBoardServices.getPosition(start[0], end[1]), "ep" });
			chessMoves.add(ChessMove.EN_PASSANT_CAPTURE);
			chessMoves.add(ChessMove.CAPTURE);
			return true;
		}

		if (isValidInitialDouble(board, start, absRowDisp, absColDisp, isWhiteMove, isAttack)) {
			chessMoves.add(ChessMove.PAWN_INITIAL_DOUBLE);
			return true;
		}

		// new addition to method; ability of pawns to jump over cats
		if (absRowDisp == 2 && colDisp == 0 && board[(start[0] + end[0]) / 2][start[1]] == PieceNotation.C) {
			return true;
		}

		return absRowDisp == 1 && (absColDisp == 1 ? isAttack : !isAttack);
	}

	@Override
	protected boolean isValidInitialDouble(PieceNotation[][] board, int[] start, int absRowDisp, int absColDisp,
			boolean isWhiteMove, boolean isAttack) {

		// modified to account for initial double jumping over cats
		return absRowDisp == 2 && absColDisp == 0 && !isAttack && (start[0] == 6 || start[0] == 1)
				&& (isUnoccupied(board, isWhiteMove ? 5 : 2, start[1])
						|| board[isWhiteMove ? 5 : 2][start[1]] == PieceNotation.C);
	}

	@Override
	protected List<int[]> getAllPiecePositionsWithStraightAccessByColor(PieceNotation[][] board, int[] square,
			GameColor color, boolean isAttack) {
		List<int[]> positions = new ArrayList<>();

		for (int[] i : PieceMovement.STRAIGHT.getMoves()) {
			int count = 1;
			int testRow = square[0] + i[0] * count;
			int testCol = square[1] + i[1] * count;

			while (isPositionOnBoard(testRow, testCol)) {
				PieceNotation testSquare = board[testRow][testCol];
				if (testSquare != null) {
					if (testSquare.getColor() != color && testSquare != PieceNotation.C) {
						break;
					}

					if (testSquare.getType() == PieceType.QUEEN || testSquare.getType() == PieceType.ROOK) {
						positions.add(new int[] { testRow, testCol });
					}

					// TODO
					// if (testSquare.getType() == PieceType.KING && count == 1) {
					// log.info("king");
					// positions.add(new int [] {testRow, testCol});
					// }

					if (!isAttack && testSquare.getType() == PieceType.PAWN && i[1] == 0) {
						int rowDisp = i[0] * count;
						int absRowDisp = Math.abs(rowDisp);
						if (isValidPawnDirection(rowDisp, color == GameColor.WHITE) && absRowDisp <= 2) {
							if (absRowDisp == 1 || isValidInitialDouble(board, new int[] { testRow, testCol },
									absRowDisp, 0, color == GameColor.WHITE, isAttack)) {
								positions.add(new int[] { testRow, testCol });
							}
						}
					}

					// addition to method; if piece is cat, check one beyond to see if pawn can jump
					// over
					if (!isAttack && testSquare == PieceNotation.C && i[1] == 0) {
						int rowDisp = i[0] * (count + 1);
						int absRowDisp = Math.abs(rowDisp);
						if (absRowDisp == 2 && board[square[0] + rowDisp][testCol].getType() == PieceType.PAWN
								&& isValidPawnDirection(rowDisp, color == GameColor.WHITE)) {
							positions.add(new int[] { square[0] + rowDisp, testCol });
						}
					}

					break;
				}
				count++;
				testRow = square[0] + i[0] * count;
				testCol = square[1] + i[1] * count;
			}
		}
		return positions;
	}

	@Override
	protected boolean isStalemate(PieceNotation[][] board, GameColor color) {

		int[] kingPos = findKing(board, color == GameColor.WHITE);

		// scan board for pieces with specified color
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				PieceNotation testSq = board[row][col];
				if (testSq != null && testSq.getColor() == color) {

					int[] start = { row, col };

					switch (testSq.getType()) {
					case ROOK:
						for (int[] i : PieceMovement.STRAIGHT.getMoves()) {
							if (canPieceMove(board, start, new int[] { row + i[0], col + i[1] }, kingPos, color)) {
								return false;
							}
						}
						break;
					case BISHOP:
						for (int[] i : PieceMovement.DIAGONAL.getMoves()) {
							if (canPieceMove(board, start, new int[] { row + i[0], col + i[1] }, kingPos, color)) {
								return false;
							}
						}
						break;
					case QUEEN:
						for (int[] i : PieceMovement.OMNI.getMoves()) {
							if (canPieceMove(board, start, new int[] { row + i[0], col + i[1] }, kingPos, color)) {
								return false;
							}
						}
						break;
					case KNIGHT:
						for (int[] i : PieceMovement.KNIGHT.getMoves()) {
							if (canPieceMove(board, start, new int[] { row + i[0], col + i[1] }, kingPos, color)) {
								return false;
							}
						}
						break;
					case PAWN:
						int[][] index = color == GameColor.WHITE ? PieceMovement.WHITE_PAWN.getMoves()
								: PieceMovement.BLACK_PAWN.getMoves();
						for (int[] i : index) {
							int tempRow = row + i[0];
							int tempCol = col + i[1];

							if (i[1] == 0 && Math.abs(i[0]) != 2 && board[tempRow][col] == null
									&& canPieceMove(board, start, new int[] { tempRow, col }, kingPos, color)) {
								return false;
							}

							// added code handling pawn jumping over cat
							if (i[1] == 0 && Math.abs(i[0]) == 2 && board[tempRow][tempCol] == null
									&& board[row + i[0] / 2][tempCol] == PieceNotation.C
									&& canPieceMove(board, start, new int[] { tempRow, tempCol }, kingPos, color)) {
								return false;
							}

							if (Math.abs(i[1]) == 1) {
								if (board[tempRow][tempCol] == null
										&& GameBoardServices.getPosition(tempRow, tempCol).equals(enPassantSquare)) {
									return false;
								}
								if (board[tempRow][tempCol] != null && board[tempRow][tempCol].getColor() != color) {
									return false;
								}
							}
						}
						break;
					case KING:
						if (canKingMove(board, kingPos, color)) {
							return false;
						}
						break;

					default:
						break;
					}

				}
			}
		}
		return true;
	}

	@Override
	protected boolean canStraightCheck(PieceNotation[][] board, int[] square, GameColor color) {

		for (int[] i : PieceMovement.STRAIGHT.getMoves()) {
			int count = 1;
			int testRow = square[0] + i[0] * count;
			int testCol = square[1] + i[1] * count;

			while (isPositionOnBoard(testRow, testCol)) {

				PieceNotation testSquare = board[testRow][testCol];
				if (testSquare != null) {
					// modified to break for cats
					if (testSquare.getColor() == color || testSquare.getColor() == GameColor.NEUTRAL) {
						break;
					}
					if (testSquare.getType() == PieceType.QUEEN || testSquare.getType() == PieceType.ROOK) {
						return true;
					}
					if (testSquare.getType() == PieceType.KING && count == 1) {
						return true;
					}
					break;
				}

				count++;
				testRow = square[0] + i[0] * count;
				testCol = square[1] + i[1] * count;
			}
		}
		return false;
	}

	@Override
	protected boolean canDiagonalCheck(PieceNotation[][] board, int[] square, GameColor color) {

		for (int[] i : PieceMovement.DIAGONAL.getMoves()) {
			int count = 1;
			int testRow = square[0] + i[0] * count;
			int testCol = square[1] + i[1] * count;

			while (isPositionOnBoard(testRow, testCol)) {
				PieceNotation testSquare = board[testRow][testCol];
				if (testSquare != null) {
					// modified code to break for cats
					if (testSquare.getColor() == color || testSquare.getColor() == GameColor.NEUTRAL) {
						break;
					}
					if (testSquare.getType() == PieceType.QUEEN || testSquare.getType() == PieceType.BISHOP) {
						return true;
					}
					if (testSquare.getType() == PieceType.KING && count == 1) {
						return true;
					}
					if (testSquare.getType() == PieceType.PAWN && count == 1
							&& isValidPawnDirection(i[0], color != GameColor.WHITE)) {
						return true;
					}
					break;
				}

				count++;
				testRow = square[0] + i[0] * count;
				testCol = square[1] + i[1] * count;
			}
		}
		return false;
	}

	@Override
	protected boolean canKingMove(PieceNotation[][] board, int[] kingPos, GameColor kingColor) {
		PieceNotation king = board[kingPos[0]][kingPos[1]];
		board[kingPos[0]][kingPos[1]] = null;
		GameColor oppColor = getOppositeColor(kingColor);

		for (int[] i : PieceMovement.OMNI.getMoves()) {

			int testRow = kingPos[0] + i[0];
			int testCol = kingPos[1] + i[1];

			if (!isPositionOnBoard(testRow, testCol)) {
				continue;
			}

			// modified to account for cats
			if (board[testRow][testCol] != null && board[testRow][testCol].getColor() != oppColor) {
				continue;
			}

			if (!isKingInCheck(board, new int[] { testRow, testCol }, kingColor)) {
				board[kingPos[0]][kingPos[1]] = king;
				return true;
			}
		}

		board[kingPos[0]][kingPos[1]] = king;
		return false;
	}

}
