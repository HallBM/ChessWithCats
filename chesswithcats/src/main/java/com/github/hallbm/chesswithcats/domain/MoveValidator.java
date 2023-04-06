package com.github.hallbm.chesswithcats.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.hallbm.chesswithcats.domain.GameEnums.ChessMove;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameColor;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameOutcome;
import com.github.hallbm.chesswithcats.domain.GameEnums.PieceMovement;
import com.github.hallbm.chesswithcats.domain.GameEnums.PieceNotation;
import com.github.hallbm.chesswithcats.domain.GameEnums.PieceType;
import com.github.hallbm.chesswithcats.dto.MoveDTO;
import com.github.hallbm.chesswithcats.model.GamePlay;
import com.github.hallbm.chesswithcats.service.GameBoardServices;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoveValidator {

	protected List<ChessMove> chessMoves = new ArrayList<>();
	protected List<String[]> pieceMoves = new ArrayList<>();
	protected String enPassantSquare;

	protected PieceNotation[][] mockBoard;
	protected PieceNotation movedPiece;
	protected MoveDTO moveDTO;
	protected GamePlay gamePlay;
	protected boolean isWhiteMove;

	public void setup(MoveDTO moveDTO, GamePlay gamePlay) {
		this.moveDTO = moveDTO;
		this.gamePlay = gamePlay;

		chessMoves.clear();
		pieceMoves.clear();
		movedPiece = null;

		enPassantSquare = gamePlay.getEnPassantTargetSquare();
		mockBoard = gamePlay.getGameBoard().getBoard();
		isWhiteMove = gamePlay.getHalfMoves() % 2 == 1;

	}

	/**
	 * Entry point into move validation and composition of MoveResponseDTO object:
	 * 1) validates move based on allowed piece movements. 2) if plausible move,
	 * simulates chess move on 'mockBoard' (deep copy of persisted game state) 3)
	 * checks if move resulted in player's king being in check 4) evaluate whether
	 * other king has been placed in check 5) if moves are not valid, return null
	 * moveResponseDTO, otherwise return populated DTO.
	 */
	public boolean validateMove(MoveDTO moveDTO, GamePlay gamePlay) {
		log.info("validating move");
		setup(moveDTO, gamePlay);

		// unpack fields and derive relevant info
		int[] start = GameBoardServices.getCoordinates(moveDTO.getStartPos());
		int[] end = GameBoardServices.getCoordinates(moveDTO.getEndPos());
		int rowDisp = -1 * (end[0] - start[0]);
		int colDisp = 1 * (end[1] - start[1]);

		movedPiece = mockBoard[start[0]][start[1]];
		PieceNotation occupyingPiece = mockBoard[end[0]][end[1]];
		boolean isAttack = isAttack(movedPiece, occupyingPiece);

		// validation of MoveDTO
		if (Arrays.equals(start, end))
			return false;

		if (!isValidTurn(isWhiteMove, movedPiece))
			return false;

		if (!isValidEndSquare(movedPiece, occupyingPiece))
			return false;

		if (moveDTO.getPromotionPiece() != null) {
			if (!("RNBQrnbq".contains(moveDTO.getPromotionPiece())))
				return false;

			if (!(isWhiteMove ? end[0] == 0 : end[0] == 7))
				return false;
		}

		// validation of piece movement
		switch (movedPiece.getType()) {
		case ROOK:
			if (!isValidRookMove(mockBoard, start, rowDisp, colDisp))
				return false;
			break;
		case BISHOP:
			if (!isValidBishopMove(mockBoard, start, rowDisp, colDisp))
				return false;
			break;
		case QUEEN:
			if (!(isValidQueenMove(mockBoard, start, rowDisp, colDisp)))
				return false;
			break;
		case KNIGHT:
			if (!isValidKnightMove(rowDisp, colDisp))
				return false;
			break;
		case PAWN:
			if (!isValidPawnMove(mockBoard, start, end, rowDisp, colDisp, isWhiteMove, isAttack, enPassantSquare))
				return false;
			break;
		case KING:
			if (!isValidKingMove(mockBoard, moveDTO.getStartPos(), moveDTO.getEndPos(), start, rowDisp, colDisp,
					isAttack, isWhiteMove, gamePlay.getCastling()))
				return false;
			break;
		default:
			return false;
		}

		log.info("was valid movement");

		// update fields for response
		setEnPassantSquare(chessMoves, start, isWhiteMove);
		chessMoves.add(isAttack ? ChessMove.CAPTURE : ChessMove.SIMPLE_MOVE);
		pieceMoves.add(new String[] { moveDTO.getStartPos(), moveDTO.getEndPos() });

		// clone board and simulate move to evaluate check
		mockBoard = GameBoardServices.simulateMove(mockBoard, pieceMoves, moveDTO.getPromotionPiece());

		log.info("checking if valid movement puts king in check");
		if (movedPiece.getType() == PieceType.KING) {
			if (isKingInCheck(mockBoard, end, movedPiece.getColor()))
				return false;
		} else {
			if (isKingInCheck(mockBoard, findKing(mockBoard, isWhiteMove), movedPiece.getColor()))
				return false;
		}

		return true;
	}

	/**
	 * Helper functions associated with loading and deriving data from MoveDTO and
	 * GamePlay
	 */

	protected boolean isValidTurn(boolean isWhiteMove, PieceNotation movedPiece) {
		return isWhiteMove ? movedPiece.getColor() == GameColor.WHITE : movedPiece.getColor() == GameColor.BLACK;
	}

	protected boolean isWhitePiece(PieceNotation piece) {
		return piece.getColor() == GameColor.WHITE;
	}

	protected GameColor getOppositeColor(GameColor color) {
		return color == GameColor.WHITE ? GameColor.BLACK : GameColor.WHITE;
	}

	/**
	 * Helper functions associated with generating final move response
	 */

	public void setEnPassantSquare(List<ChessMove> chessMoves, int[] start, boolean isWhiteMove) {
		enPassantSquare = chessMoves.contains(ChessMove.PAWN_INITIAL_DOUBLE)
				? GameBoardServices.getPosition(isWhiteMove ? 5 : 2, start[1])
				: "";
	}

	/**
	 * Generates String of extended chess move notation for tracking game history.
	 * MoveResponseDTO object updated with official chess move.
	 */
	public String generateOfficialMove() {
		String move = "";

		if (gamePlay.getHalfMoves() % 2 == 1) {
			move += String.valueOf((gamePlay.getHalfMoves() - 1) / 2 + 1) + ".";
		}

		if (chessMoves.contains(ChessMove.KING_SIDE_CASTLE)) {
			move += "O-O" + (chessMoves.contains(ChessMove.CHECK) ? "+" : " ");
		} else if (chessMoves.contains(ChessMove.QUEEN_SIDE_CASTLE)) {
			move += "O-O-O" + (chessMoves.contains(ChessMove.CHECK) ? "+" : " ");
		} else {
			move += movedPiece.toString() + moveDTO.getStartPos().toLowerCase()
					+ (chessMoves.contains(ChessMove.CAPTURE) ? "x" : "") + moveDTO.getEndPos().toLowerCase()
					+ (chessMoves.contains(ChessMove.EN_PASSANT_CAPTURE) ? "ep" : "")
					+ (moveDTO.getPromotionPiece() != null ? "=" + moveDTO.getPromotionPiece() : "")
					+ (chessMoves.contains(ChessMove.CHECK) ? "+" : "")
					+ (chessMoves.contains(ChessMove.CHECKMATE) ? "#" : "") + " ";
		}
		return move;
	}

	/**
	 * Helper functions associated with piece movements
	 */
	protected boolean isPositionOnBoard(int row, int col) {
		return row >= 0 && row <= 7 && col >= 0 && col <= 7;
	}

	protected boolean isValidEndSquare(PieceNotation movedPiece, PieceNotation occupyingPiece) {
		return occupyingPiece == null || occupyingPiece.getColor() != movedPiece.getColor();
	}

	protected boolean isAttack(PieceNotation movedPiece, PieceNotation occupyingPiece) {
		return occupyingPiece != null && occupyingPiece.getColor() != movedPiece.getColor();
	}

	protected PieceNotation getKingNotation(boolean isWhite) {
		return PieceNotation.valueOf(isWhite ? "K" : "k");
	}

	protected int[] findKing(PieceNotation[][] board, boolean isWhite) {
		PieceNotation king = getKingNotation(isWhite);
		return GameBoardServices.findKingPosition(board, king);
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
	protected boolean isUnobstructed(PieceNotation[][] board, int[] start, int rowDisp, int colDisp) {

		int maxDisp = Math.max(Math.abs(rowDisp), Math.abs(colDisp));
		int rowSign = Integer.signum(rowDisp);
		int colSign = Integer.signum(colDisp);

		for (int i = 1; i < maxDisp; i++) {
			int testRow = start[0] - i * rowSign;
			int testCol = start[1] + i * colSign;

			if (board[testRow][testCol] != null) {
				return false;
			}

		}

		return true;
	}

	protected boolean isUnoccupied(PieceNotation[][] board, int row, int col) {
		return board[row][col] == null;
	}

	protected boolean isValidRookMove(PieceNotation[][] board, int[] start, int rowDisp, int colDisp) {
		boolean isValid = (rowDisp == 0) != (colDisp == 0);
		return isValid && isUnobstructed(board, start, rowDisp, colDisp);
	}

	protected boolean isValidKnightMove(int rowDisp, int colDisp) {
		return (Math.abs(rowDisp) == 1 && Math.abs(colDisp) == 2) || (Math.abs(rowDisp) == 2 && Math.abs(colDisp) == 1);
	}

	protected boolean isValidBishopMove(PieceNotation[][] board, int[] start, int rowDisp, int colDisp) {
		boolean isValid = Math.abs(rowDisp) == Math.abs(colDisp);
		return isValid && isUnobstructed(board, start, rowDisp, colDisp);
	}

	protected boolean isValidQueenMove(PieceNotation[][] board, int[] start, int rowDisp, int colDisp) {
		boolean isValid = ((rowDisp == 0) != (colDisp == 0)) || (Math.abs(rowDisp) == Math.abs(colDisp));
		return isValid && isUnobstructed(board, start, rowDisp, colDisp);
	}

	/**
	 * Logic for classic pawn moves. 1) Check if move is an attack or en passant
	 * capture. 2) Check whether move from starting position triggers an en passant
	 * attack on next move. 3) Check if move is simple forward move in column.
	 */
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

		log.info("not valid enpassant capture or initial double");
		return absRowDisp == 1 && (absColDisp == 1 ? isAttack : !isAttack);
	}

	protected boolean isValidPawnDirection(int rowDisp, boolean isWhiteMove) {
		return (isWhiteMove ? 1 : -1) * rowDisp > 0;
	}

	protected boolean isValidInitialDouble(PieceNotation[][] board, int[] start, int absRowDisp, int absColDisp,
			boolean isWhiteMove, boolean isAttack) {
		log.info("checking is valid initial double?");
		return absRowDisp == 2 && absColDisp == 0 && !isAttack && (start[0] == 6 || start[0] == 1)
				&& isUnoccupied(board, isWhiteMove ? 5 : 2, start[1]);
	}

	protected boolean isValidEnPassantCapture(int absRowDisp, int absColDisp, boolean isAttack, String endPos,
			String enPassantSquare) {
		return absRowDisp == 1 && absColDisp == 1 && !isAttack && enPassantSquare.equals(endPos);
	}

	/**
	 * Logic for classic king move, including castling if available. Moving king
	 * directly validates whether king will be in check, and for castling, whether
	 * piece is in check at any point along castling move transition. Updates
	 * castling rules accordingly within gamePlay object.
	 */
	protected boolean isValidKingMove(PieceNotation[][] board, String startPos, String endPos, int[] start, int rowDisp,
			int colDisp, boolean isAttack, boolean isWhiteMove, String castling) {

		int absRowDisp = Math.abs(rowDisp);
		int absColDisp = Math.abs(colDisp);

		boolean isValid = (absColDisp == 1 || absRowDisp == 1 || (absColDisp == 2 && absRowDisp == 0));

		if (!isValid) {
			return false;
		}

		if (absColDisp == 2) {
			return isValidCastling(board, start, startPos, endPos, isAttack, isWhiteMove, castling);
		}

		return true;
	}

	protected boolean isValidCastling(PieceNotation[][] board, int[] start, String startPos, String endPos,
			boolean isAttack, boolean isWhiteMove, String castling) {

		log.info("checking for valid castle");
		boolean isQueenSide;

		if (castling == null || isAttack) {
			return false;
		} else if (startPos.equals("E1") && endPos.equals("C1") && castling.contains("Q")) {
			chessMoves.add(ChessMove.QUEEN_SIDE_CASTLE);
			pieceMoves.add(new String[] { "A1", "D1" });
			isQueenSide = true;
			log.info("Q");
		} else if (startPos.equals("E1") && endPos.equals("G1") && castling.contains("K")) {
			chessMoves.add(ChessMove.KING_SIDE_CASTLE);
			pieceMoves.add(new String[] { "H1", "F1" });
			isQueenSide = false;
			log.info("K");
		} else if (startPos.equals("E8") && endPos.equals("C8") && castling.contains("q")) {
			chessMoves.add(ChessMove.QUEEN_SIDE_CASTLE);
			pieceMoves.add(new String[] { "A8", "D8" });
			isQueenSide = true;
			log.info("q");
		} else if (startPos.equals("E8") && endPos.equals("G8") && castling.contains("k")) {
			chessMoves.add(ChessMove.KING_SIDE_CASTLE);
			pieceMoves.add(new String[] { "H8", "F8" });
			isQueenSide = false;
			log.info("k");
		} else {
			return false;
		}

		log.info("now evaluating whether king can transition through squares without being blocked or in check");
		for (int i = 0; i <= (isQueenSide ? 2 : 1); i++) {
			int[] testSquare = { start[0], start[1] + i * (isQueenSide ? -1 : 1) };

			if (i != 0 && board[testSquare[0]][testSquare[1]] != null) {
				log.info("blocked");
				return false;
			}

			if (isKingInCheck(board, testSquare, isWhiteMove ? GameColor.WHITE : GameColor.BLACK)) {
				log.info("king is in check during transition");
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks whether the king of the indicated color and position is under attack
	 * in any direction and within reach of knight.
	 */
	protected boolean isKingInCheck(PieceNotation[][] board, int[] kingPos, GameColor kingColor) {
		log.info("checking if king in check function");
		return canKnightCheck(board, kingPos, kingColor) || canStraightCheck(board, kingPos, kingColor)
				|| canDiagonalCheck(board, kingPos, kingColor);
	}

	/**
	 * Knight-specific logic for evaluating whether the king is in check.
	 */
	protected boolean canKnightCheck(PieceNotation[][] board, int[] square, GameColor color) {
		log.info("can knight check");

		for (int[] i : PieceMovement.KNIGHT.getMoves()) {
			int testRow = square[0] + i[0];
			int testCol = square[1] + i[1];

			if (isPositionOnBoard(testRow, testCol)) {
				PieceNotation testSquare = board[testRow][testCol];
				if (testSquare != null && testSquare.getType() == PieceType.KNIGHT && testSquare.getColor() != color) {
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
	protected boolean canStraightCheck(PieceNotation[][] board, int[] square, GameColor color) {
		log.info("can straight check");

		for (int[] i : PieceMovement.STRAIGHT.getMoves()) {
			int count = 1;
			int testRow = square[0] + i[0] * count;
			int testCol = square[1] + i[1] * count;

			while (isPositionOnBoard(testRow, testCol)) {

				PieceNotation testSquare = board[testRow][testCol];
				if (testSquare != null) {
					if (testSquare.getColor() == color) {
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

	/**
	 * Diagonal-specific logic for evaluating whether the king is in check. Looks
	 * for unobstructed opposite-color queen, bishop, nearby king or nearby pawn (on
	 * attacking diagonal).
	 */
	protected boolean canDiagonalCheck(PieceNotation[][] board, int[] square, GameColor color) {
		log.info("can diag check");

		for (int[] i : PieceMovement.DIAGONAL.getMoves()) {
			int count = 1;
			int testRow = square[0] + i[0] * count;
			int testCol = square[1] + i[1] * count;

			while (isPositionOnBoard(testRow, testCol)) {
				PieceNotation testSquare = board[testRow][testCol];
				if (testSquare != null) {
					if (testSquare.getColor() == color) {
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

	/**
	 * Methods for evaluating check, checkmate and stalemate
	 */

	public GameOutcome evaluateGameStatus() {
		log.info("eval game status");
		// TODO make sure to chessmove check if check but not checkmate

		int[] oppKingPos = findKing(mockBoard, !isWhiteMove);

		if (isCheckmate(mockBoard, oppKingPos, !isWhiteMove)) {
			return GameOutcome.CHECKMATE;
		}

		if (isStalemate(mockBoard, !isWhiteMove ? GameColor.WHITE : GameColor.BLACK)) {
			return GameOutcome.STALEMATE;
		}
		return null;
	}

	protected boolean isCheckmate(PieceNotation[][] board, int[] kingPos, boolean isWhiteKing) {
		log.info("check for checkmate");
		List<int[]> attackPositions = getAllAccessingPiecePositionsByColor(board, kingPos,
				!isWhiteKing ? GameColor.WHITE : GameColor.BLACK, true, enPassantSquare);

		log.info("no atk pieces: {}", attackPositions.size());

		if (attackPositions.size() == 0) {
			return false;
		}

		chessMoves.add(ChessMove.CHECK);

		if (canBlockCheck(board, attackPositions, kingPos, isWhiteKing ? GameColor.WHITE : GameColor.BLACK,
				enPassantSquare)) {
			return false;
		}

		chessMoves.remove(ChessMove.CHECK);
		chessMoves.add(ChessMove.CHECKMATE);

		return true;
	}

	/**
	 * Checks whether the king of the indicated color and position is under attack
	 * in any direction and within reach of knight.
	 */
	protected List<int[]> getAllAccessingPiecePositionsByColor(PieceNotation[][] board, int[] square, GameColor color,
			boolean isAttack, String enPassantSquare) {

		List<int[]> positions = new ArrayList<>();

		positions.addAll(getAllPiecePositionsWithKnightAccessByColor(board, square, color));
		positions.addAll(getAllPiecePositionsWithStraightAccessByColor(board, square, color, isAttack));
		positions
				.addAll(getAllPiecePositionsWithDiagonalAccessByColor(board, square, color, isAttack, enPassantSquare));

		return positions;
	}

	/**
	 * Knight-specific logic for evaluating whether the king is in check.
	 */
	protected List<int[]> getAllPiecePositionsWithKnightAccessByColor(PieceNotation[][] board, int[] square,
			GameColor color) {
		log.info("AllKnightPiecesAccessingSquare");
		List<int[]> positions = new ArrayList<>();

		for (int[] i : PieceMovement.KNIGHT.getMoves()) {
			int testRow = square[0] + i[0];
			int testCol = square[1] + i[1];

			if (isPositionOnBoard(testRow, testCol)) {
				PieceNotation testSquare = board[testRow][testCol];
				if (testSquare != null && testSquare.getColor() == color && testSquare.getType() == PieceType.KNIGHT) {
					positions.add(new int[] { testRow, testCol });
				}
			}
		}
		return positions;
	}

	/**
	 * Row/Col-specific logic for evaluating whether the king is in check. Looks for
	 * unobstructed opposite-color queen, rook, nearby king.
	 */
	protected List<int[]> getAllPiecePositionsWithStraightAccessByColor(PieceNotation[][] board, int[] square,
			GameColor color, boolean isAttack) {
		List<int[]> positions = new ArrayList<>();
		GameColor oppColor = getOppositeColor(color);

		log.info("AllStraightPiecesAccessingSquare");
		for (int[] i : PieceMovement.STRAIGHT.getMoves()) {
			int count = 1;
			int testRow = square[0] + i[0] * count;
			int testCol = square[1] + i[1] * count;

			while (isPositionOnBoard(testRow, testCol)) {
				PieceNotation testSquare = board[testRow][testCol];
				if (testSquare != null) {
					if (testSquare.getColor() == oppColor) {
						log.info("break; wrong color");
						break;
					}

					if (testSquare.getType() == PieceType.QUEEN || testSquare.getType() == PieceType.ROOK) {
						log.info("queen/rook");
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
						log.info("pawn");
						if (isValidPawnDirection(rowDisp, color == GameColor.WHITE) && absRowDisp <= 2) {
							if (absRowDisp == 1 || isValidInitialDouble(board, new int[] { testRow, testCol },
									absRowDisp, 0, color == GameColor.WHITE, isAttack)) {
								log.info("valid pawn");
								positions.add(new int[] { testRow, testCol });
							}
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

	/**
	 * Diagonal-specific logic for evaluating whether the king is in check. Looks
	 * for unobstructed opposite-color queen, bishop, nearby king or nearby pawn (on
	 * attacking diagonal).
	 */
	protected List<int[]> getAllPiecePositionsWithDiagonalAccessByColor(PieceNotation[][] board, int[] square,
			GameColor color, boolean isAttack, String enPassantSquare) {
		List<int[]> positions = new ArrayList<>();
		GameColor oppColor = getOppositeColor(color);

		log.info("AllDiagonalPiecesAccessingSquare");
		for (int[] i : PieceMovement.DIAGONAL.getMoves()) {
			int count = 1;
			int testRow = square[0] + i[0] * count;
			int testCol = square[1] + i[1] * count;

			while (isPositionOnBoard(testRow, testCol)) {
				PieceNotation testSquare = board[testRow][testCol];
				if (testSquare != null) {
					if (testSquare.getColor() == oppColor) {
						log.info("wrong color; doesn't match");
						break;
					}
					if (testSquare.getType() == PieceType.QUEEN || testSquare.getType() == PieceType.BISHOP) {
						log.info("queen/bish");
						positions.add(new int[] { testRow, testCol });
					}

					// TODO
					// if (testSquare.getType() == PieceType.KING && count == 1) {
					// log.info("king");
					// positions.add(new int [] {testRow, testCol});
					// }

					if (testSquare.getType() == PieceType.PAWN && count == 1) {
						int rowDisp = i[0] * count;
						log.info("pawn");
						if (isValidPawnDirection(rowDisp, color == GameColor.WHITE)) {
							if (isAttack) {
								log.info("valid pawn");
								positions.add(new int[] { testRow, testCol });
							} else if (GameBoardServices.getPosition(testRow, testCol).equals(enPassantSquare)) {
								log.info("valid en passant");
							}
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

	protected boolean canBlockCheck(PieceNotation[][] board, List<int[]> attackPositions, int[] kingPos,
			GameColor kingColor, String enPassantSquare) {
		log.info("testing if opponent's king is in checkmate");

		if (canKingMove(board, kingPos, kingColor)) {
			log.info("opponent's king can move out of check");
			return true;
		} else if (canDisruptCheck(board, attackPositions, kingPos, kingColor)) {
			log.info("opponent's king can be saved via capture or blocking");
			return true;
		} else {
			return false;
		}
	}

	protected boolean canDisruptCheck(PieceNotation[][] board, List<int[]> attackPositions, int[] kingPos,
			GameColor kingColor) {

		log.info("testing whether can any of the opponent's pieces can disrupt checkmate");
		log.info("identifying pieces that are putting opponent's king in check");
		log.info(Arrays.toString(attackPositions.get(0)));

		/**
		 * Ability of king to move to safe square already accounted for by the time this
		 * function is called; therefore, being in check by two pieces precludes the
		 * ability to escape checkmate by moving other pieces.
		 */

		if (attackPositions.size() > 1) {
			log.info("more than 1 attack position; king unable to move -> checkmate");
			return false;
		}

		int[] atkPos = attackPositions.get(0);

		// can attacking piece be captured?
		List<int[]> offensiveOptions = getAllAccessingPiecePositionsByColor(board, atkPos, kingColor, true,
				enPassantSquare);

		for (int[] offPos : offensiveOptions) {
			if (canPieceMove(board, offPos, attackPositions.get(0), kingPos, kingColor)) {
				return true;
			}
		}

		int attackerRow = atkPos[0];
		int attackerCol = atkPos[1];
		int atkRowDisp = attackerRow - kingPos[0];
		int atkColDisp = attackerCol - kingPos[1];
		int rowSign = Integer.signum(atkRowDisp);
		int colSign = Integer.signum(atkColDisp);
		int disp = Math.max(Math.abs(atkColDisp), Math.abs(atkRowDisp));

		List<int[]> defensiveOptions = new ArrayList<>();

		// check blocking moves

		log.info("identifying pieces that can disrupt opponent's check: by blocking");
		for (int i = 1; i < disp; i++) {
			int tempRow = attackerRow - i * rowSign;
			int tempCol = attackerCol - i * colSign;
			int[] tempPos = new int[] { tempRow, tempCol };

			defensiveOptions = getAllAccessingPiecePositionsByColor(board, tempPos, kingColor, false, enPassantSquare);

			log.info(defensiveOptions.stream().map(Arrays::toString).reduce("", (s1, s2) -> s1 + s2 + " "));

			for (int[] defPos : defensiveOptions) {
				log.info(Arrays.toString(defPos));
				if (canPieceMove(board, defPos, tempPos, kingPos, kingColor)) {
					return true;
				}
			}
		}

		return false;
	}

	protected boolean isStalemate(PieceNotation[][] board, GameColor color) {

		int[] kingPos = findKing(board, color == GameColor.WHITE);
		log.info("king for checking stalemate: {}", board[kingPos[0]][kingPos[1]].toString());

		// scan board for pieces with specified color
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				PieceNotation testSq = board[row][col];
				if (testSq != null && testSq.getColor() == color) {

					int[] start = { row, col };
					log.info("stalemate check on" + row + col + board[row][col].toString());

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
						log.info(Arrays.toString(index));
						for (int[] i : index) {
							int tempRow = row + i[0];
							int tempCol = col + i[1];

							if (!isPositionOnBoard(tempRow, tempCol)) {
								return false;
							}

							if (i[1] == 0 && Math.abs(i[0]) != 2 && board[tempRow][tempCol] == null
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

	protected boolean canKingMove(PieceNotation[][] board, int[] kingPos, GameColor kingColor) {
		log.info("checking whether opponent's king can move out of check");
		PieceNotation king = board[kingPos[0]][kingPos[1]];
		board[kingPos[0]][kingPos[1]] = null;

		for (int[] i : PieceMovement.OMNI.getMoves()) {

			int testRow = kingPos[0] + i[0];
			int testCol = kingPos[1] + i[1];

			log.info("checking whether square [" + testRow + "][" + testCol + "] is under attack");

			if (!isPositionOnBoard(testRow, testCol)) {
				continue;
			}

			if (board[testRow][testCol] != null && board[testRow][testCol].getColor() == kingColor) {
				log.info("tested position has a piece that is the same color as opponent (cannot be captured)");
				continue;
			}

			if (!isKingInCheck(board, new int[] { testRow, testCol }, kingColor)) {
				log.info("opponent's king can move out of check");
				board[kingPos[0]][kingPos[1]] = king;
				return true;
			}
		}

		board[kingPos[0]][kingPos[1]] = king;
		return false;
	}

	protected boolean canPieceMove(PieceNotation[][] board, int[] start, int[] end, int[] kingPos,
			GameColor kingColor) {

		if (!isPositionOnBoard(end[0], end[1])) {
			return false;
		}

		PieceNotation piece = board[start[0]][start[1]];
		PieceNotation temp = board[end[0]][end[1]];

		if (temp != null && piece.getColor() == temp.getColor()) {
			return false;
		}

		// move piece

		board[start[0]][start[1]] = null;
		board[end[0]][end[1]] = piece;

		// check for mate
		if (!isKingInCheck(board, kingPos, kingColor)) {
			log.info(Arrays.toString(start));
			log.info(Arrays.toString(end));
			log.info(Arrays.toString(kingPos));
			board[start[0]][start[1]] = piece;
			board[end[0]][end[1]] = temp;
			return true;
		}

		board[start[0]][start[1]] = piece;
		board[end[0]][end[1]] = temp;
		return false;
	}

}
