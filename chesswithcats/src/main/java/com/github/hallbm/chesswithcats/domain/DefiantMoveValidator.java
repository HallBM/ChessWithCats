package com.github.hallbm.chesswithcats.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.github.hallbm.chesswithcats.domain.GameEnums.ChessMove;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameColor;
import com.github.hallbm.chesswithcats.domain.GameEnums.PieceNotation;
import com.github.hallbm.chesswithcats.domain.GameEnums.PieceType;
import com.github.hallbm.chesswithcats.dto.MoveDTO;
import com.github.hallbm.chesswithcats.model.GamePlay;
import com.github.hallbm.chesswithcats.service.GameBoardServices;

public class DefiantMoveValidator extends MoveValidator {

	private boolean isDefiant;
	private String newEndPos;
	
	@Override
	public void setup(MoveDTO moveDTO, GamePlay gamePlay) {
		this.moveDTO = moveDTO;
		this.gamePlay = gamePlay;

		chessMoves.clear();
		pieceMoves.clear();
		movedPiece = null;

		enPassantSquare = gamePlay.getEnPassantTargetSquare();
		mockBoard = gamePlay.getGameBoard().getBoard();
		isWhiteMove = gamePlay.getHalfMoves() % 2 == 1;

		// additional code
		isDefiant = false;
		newEndPos = null;
	}
	
	@Override
	public boolean validateMove(MoveDTO moveDTO, GamePlay gamePlay) {
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

		// update fields for response
		setEnPassantSquare(chessMoves, start, isWhiteMove);
		chessMoves.add(isAttack ? ChessMove.CAPTURE : ChessMove.SIMPLE_MOVE);
		pieceMoves.add(new String[] { moveDTO.getStartPos(), moveDTO.getEndPos() });

		// clone board and simulate move to evaluate check
		mockBoard = GameBoardServices.simulateMove(mockBoard, pieceMoves, moveDTO.getPromotionPiece());

		if (movedPiece.getType() == PieceType.KING) {
			if (isKingInCheck(mockBoard, end, movedPiece.getColor()))
				return false;
		} else {
			if (isKingInCheck(mockBoard, findKing(mockBoard, isWhiteMove), movedPiece.getColor()))
				return false;
		}

		// addition to method; after move is validated, determine if position shift
		if (!gamePlay.getIsInCheck() || moveDTO.getPromotionPiece() != null || chessMoves.contains(ChessMove.EN_PASSANT_CAPTURE)) {
			if (new Random().nextInt(2) == 0) {
				attemptDefiance(gamePlay.getGameBoard().getBoard(), mockBoard, movedPiece, start, end);
			}
		}

		return true;
	}

	protected void attemptDefiance(PieceNotation[][] actualBoard, PieceNotation[][] mockBoard, PieceNotation movedPiece, int[] start, int[] end) {
		Random random = new Random();
		GameColor color = movedPiece.getColor();
		int origEndRow = end[0];
		int origEndCol = end[1];
		int startRow = start[0];
		int startCol = start[1];
		
		List<int[]> offsets = new ArrayList<>(List.of(new int[] { -1, -1 }, new int[] { -1, 0 }, new int[] { -1, 1 },
				new int[] { 0, -1 }, new int[] { 0, 1 }, new int[] { 1, -1 }, new int[] { 1, 0 }, new int[] { 1, 1 }));

		// undo previous move by restoring endSquare
		mockBoard[origEndRow][origEndCol] = actualBoard[origEndRow][origEndCol];

		int newEndRow = -1;
		int newEndCol = -1;
		// randomly evaluate adjacent squares to find first valid position
		while (offsets.size() != 0) {
			int index = random.nextInt(offsets.size());
			int[] chosenOffset = offsets.get(index);
			offsets.remove(chosenOffset);
			
			newEndRow = origEndRow + chosenOffset[0];
			newEndCol = origEndCol + chosenOffset[1];

			if (!isPositionOnBoard(newEndRow, newEndCol) || (startRow == newEndRow && startCol == newEndCol)) {
				continue;
			}

			PieceNotation newEndPiece = mockBoard[newEndRow][newEndCol];

			if (newEndPiece != null && (newEndPiece.getType() == PieceType.KING || newEndPiece.getColor() == color)) {
					continue;
			}

			newEndPiece = movedPiece;

			// is king in check with attempted defiance?
			if (movedPiece.getType() == PieceType.KING) {
				if (isKingInCheck(mockBoard, end, color)) {
					// undo 
					newEndPiece = actualBoard[newEndRow][newEndCol];
					continue;
				} 
				break;
			} else {
				if (isKingInCheck(mockBoard, findKing(mockBoard, color == GameColor.WHITE), color)) {
					newEndPiece = actualBoard[newEndRow][newEndCol];
					continue;
				} 
				break;
			}
		}

		if (offsets.size() == 0) {
			// reset if no valid moves available
			mockBoard[origEndRow][origEndCol] = movedPiece;
		} else {
			isDefiant = true;
			
			String startPos = GameBoardServices.getPosition(start[0], start[1]);
			newEndPos = GameBoardServices.getPosition(newEndRow, newEndCol);
			
			pieceMoves.remove(pieceMoves.size() - 1);
			pieceMoves.add(new String[] { startPos, newEndPos });
			
			if (actualBoard[newEndRow][newEndCol] == null) {
				chessMoves.remove(ChessMove.CAPTURE);
			} else {
				chessMoves.add(ChessMove.CAPTURE);
			}
			
			if(movedPiece.getType() == PieceType.PAWN) {
				if(chessMoves.contains(ChessMove.PAWN_INITIAL_DOUBLE)) {
					chessMoves.remove(ChessMove.PAWN_INITIAL_DOUBLE);
					enPassantSquare = "";
				} else if (startCol == newEndCol && startRow == (color == GameColor.WHITE ? 6 : 1) && newEndRow == (color == GameColor.WHITE ? 4 : 3)) {
					chessMoves.add(ChessMove.PAWN_INITIAL_DOUBLE);
					enPassantSquare = GameBoardServices.getPosition(color == GameColor.WHITE ? 5 : 2, start[1]);
				}
			}
		}

	}

	@Override 
	// modified to document original move and actual move in case of defiance;
	public String generateOfficialMove() {
		String move = "";

		if (gamePlay.getHalfMoves() % 2 == 1) {
			move += String.valueOf((gamePlay.getHalfMoves() - 1) / 2 + 1) + ".";
		}

		if (chessMoves.contains(ChessMove.KING_SIDE_CASTLE)) {
			move += "O-O" 
					+(isDefiant ? "*" + movedPiece.toString() + newEndPos.toLowerCase() : "")  
					+(chessMoves.contains(ChessMove.CHECK) ? "+" : " ");
		} else if (chessMoves.contains(ChessMove.QUEEN_SIDE_CASTLE)) {
			move += "O-O-O" 
					+ (isDefiant ? "*" + movedPiece.toString() + newEndPos.toLowerCase() : "")
					+ (chessMoves.contains(ChessMove.CHECK) ? "+" : " ");
		} else {
			move += movedPiece.toString() + moveDTO.getStartPos().toLowerCase()
					+ (chessMoves.contains(ChessMove.CAPTURE) ? "x" : "") 
					+ moveDTO.getEndPos().toLowerCase()
					+ (isDefiant ? "*" + newEndPos.toLowerCase() : "")
					+ (chessMoves.contains(ChessMove.EN_PASSANT_CAPTURE) ? "ep" : "")
					+ (moveDTO.getPromotionPiece() != null ? "=" + moveDTO.getPromotionPiece() : "")
					+ (chessMoves.contains(ChessMove.CHECK) ? "+" : "")
					+ (chessMoves.contains(ChessMove.CHECKMATE) ? "#" : "") + " ";
		}
		return move;
	}
}
