package com.github.hallbm.chesswithcats.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;
import com.github.hallbm.chesswithcats.domain.GameEnums.PieceNotation;
import com.github.hallbm.chesswithcats.model.Game;
import com.github.hallbm.chesswithcats.model.GameBoard;

/**
 * Services associated with maintenance and properties of the actual chess board
 * state.
 */

public final class GameBoardServices {

	/**
	 * Square positions of the corresponding PieceNotation[][] board: e.g., [0][0] =
	 * "A8".
	 */
	private final static String[][] squares = { { "A8", "B8", "C8", "D8", "E8", "F8", "G8", "H8" },
			{ "A7", "B7", "C7", "D7", "E7", "F7", "G7", "H7" }, { "A6", "B6", "C6", "D6", "E6", "F6", "G6", "H6" },
			{ "A5", "B5", "C5", "D5", "E5", "F5", "G5", "H5" }, { "A4", "B4", "C4", "D4", "E4", "F4", "G4", "H4" },
			{ "A3", "B3", "C3", "D3", "E3", "F3", "G3", "H3" }, { "A2", "B2", "C2", "D2", "E2", "F2", "G2", "H2" },
			{ "A1", "B1", "C1", "D1", "E1", "F1", "G1", "H1" } };

	/**
	 * Mapping of column letter to PieceNotation[][] board index.
	 */
	private final static Map<Character, Integer> columnMap = new HashMap<>() {

		private static final long serialVersionUID = 1L;

		{
			put('A', 0);
			put('B', 1);
			put('C', 2);
			put('D', 3);
			put('E', 4);
			put('F', 5);
			put('G', 6);
			put('H', 7);
		}
	};

	/**
	 * Mapping of row number to PieceNotation[][] board index.
	 */
	private final static Map<Character, Integer> rowMap = new HashMap<>() {

		private static final long serialVersionUID = 1L;

		{
			put('1', 7);
			put('2', 6);
			put('3', 5);
			put('4', 4);
			put('5', 3);
			put('6', 2);
			put('7', 1);
			put('8', 0);
		}
	};

	public static String getPosition(int row, int col) {
		return squares[row][col];
	}

	public static int getColumn(String location) {
		return columnMap.get(location.charAt(0));
	}

	public static int getRow(String location) {
		return rowMap.get(location.charAt(1));
	}

	public static int[] getCoordinates (String location) {
		return new int[] {rowMap.get(location.charAt(1)), columnMap.get(location.charAt(0))};
	}
	
	/**
	 * Initial game setup of classic chess game. String[] pieces converted to
	 * PieceNotation. PieceNotation [][] board and HashMap<String,PieceNotation>
	 * pieceMap assigned pieces.
	 */
	public static void setupGameBoard(Game game) {

		String[] pieces = "rnbqkbnrppppppppPPPPPPPPRNBQKBNR".split("");
		int index = 0;

		for (int row : new int[] { 0, 1, 6, 7 }) {
			for (int col = 0; col < 8; col++) {
				game.getGamePlay().getGameBoard().getBoard()[row][col] = PieceNotation.valueOf(pieces[index]);
				game.getGamePlay().getGameBoard().getPieceMap().put(GameBoardServices.getPosition(row, col),
						PieceNotation.valueOf(pieces[index++]));
			}
		}

		if (game.getStyle() == GameStyle.OBSTRUCTIVE) {
			addFourCats(game);
		}
	}

	/**
	 * For 'obstructive' game, 4 cats added to the game board (1 per quadrant)
	 * PieceNotation [][] board and HashMap<String,PieceNotation> pieceMap assigned
	 * cats. Cats do not overlap on rank or file.
	 */
	public static void addFourCats(Game game) {
		int[][] quads = new int[4][2];
		Random random = new Random();
	    Set<Integer> rows = new HashSet<>();
	    Set<Integer> cols = new HashSet<>();

		// Get random open square (non-overlapping in row or col)
		for (int i = 0; i < 4; i++) {
			int row, col;

			do {
				row = random.nextInt(4) + 2;
			} while (rows.contains(row));

			do {
				col = random.nextInt(8);
			} while (cols.contains(col));

			rows.add(row);
			cols.add(col);
			quads[i][0] = row;
			quads[i][1] = col;
		}

		// Add cats to the board
		for (int[] q : quads) {
			game.getGamePlay().getGameBoard().getBoard()[q[0]][q[1]] = PieceNotation.C;
			game.getGamePlay().getGameBoard().getPieceMap().put(GameBoardServices.getPosition(q[0], q[1]),
					PieceNotation.C);
		}
	}

	/**
	 * Simulated movement of chess pieces on a duplicated board
	 * 
	 * @param PieceNotation[][] currentBoard;
	 * @param List<String[]>    moves from moveResponseDTO; String[2] represents
	 *                          either [startPos, endPos] for any move (e.g.,
	 *                          ["A2","A4"]) or represents [en passant capture
	 *                          position, null] for an en passant capture (e.g.,
	 *                          ["A4", null]). Normal moves (with or without
	 *                          capture) consist of 1 move. Special moves (castling,
	 *                          en passant) consist of 2 moves: 1) the side effect
	 *                          move (i.e., additional move of Rook for castling or
	 *                          removal of ep captured pawn) followed by 2) the
	 *                          original move made by the player.
	 * @param String            promotionPiece from moveDTO to indicate which piece
	 *                          to replace the promoted pawn
	 * @return PieceNotation[][] cloned board that has been updated with attempted
	 *         move.
	 */
	public static PieceNotation[][] simulateMove(PieceNotation[][] currentBoard, List<String[]> moves,
			String promotionPiece) {
		PieceNotation[][] futureBoard = copyBoard(currentBoard);
		int start_row, start_col, end_row, end_col;

		for (String[] move : moves) {
			start_row = getRow(move[0]);
			start_col = getColumn(move[0]);

			if (move[1].equals("ep")) { // en passant captured piece
				futureBoard[start_row][start_col] = null;
				continue;
			}

			end_row = getRow(move[1]);
			end_col = getColumn(move[1]);

			if (promotionPiece != null) {
				futureBoard[end_row][end_col] = PieceNotation.valueOf(promotionPiece);
				futureBoard[start_row][start_col] = null;
			} else {
				futureBoard[end_row][end_col] = futureBoard[start_row][start_col];
				futureBoard[start_row][start_col] = null;
			}
		}
		
		return futureBoard;
	}

	/**
	 * Movement of chess pieces on the persisted board
	 * 
	 * @Param GameBoard provides PieceNotation[][] board and
	 *        HashMap<String,PieceNotation> pieceMap to be updated
	 * @param List<String[]> moves from moveResponseDTO; String[2] represents either
	 *                       [startPos, endPos] for any move (e.g., ["A2","A4"]) or
	 *                       represents [en passant capture position, null] for an
	 *                       en passant capture (e.g., ["A4", null]). Normal moves
	 *                       (with or without capture) consist of 1 move. Special
	 *                       moves (castling, en passant) consist of 2 moves: 1) the
	 *                       side effect move (i.e., additional move of Rook for
	 *                       castling or removal of ep captured pawn) followed by 2)
	 *                       the original move made by the player.
	 * @param String         promotionPiece from moveDTO to indicate which piece to
	 *                       replace the promoted pawn PieceNotation [][] board and
	 *                       HashMap<String,PieceNotation> pieceMap updated.
	 */
	public static void movePiece(GameBoard board, List<String[]> moves, String promotionPiece) {
		int start_row, start_col, end_row, end_col;

		for (String[] move : moves) {
			start_row = getRow(move[0]);
			start_col = getColumn(move[0]);

			if (move[1].equals("ep")) { // en passant capture
				board.getBoard()[start_row][start_col] = null;
				board.getPieceMap().remove(move[0]);
				continue;
			}

			end_row = getRow(move[1]);
			end_col = getColumn(move[1]);

			if (promotionPiece != null) {
				board.getBoard()[end_row][end_col] = PieceNotation.valueOf(promotionPiece);
				board.getBoard()[start_row][start_col] = null;

				board.getPieceMap().remove(move[0]);
				board.getPieceMap().put(move[1], board.getBoard()[end_row][end_col]);
			} else {
				board.getBoard()[end_row][end_col] = board.getBoard()[start_row][start_col];
				board.getBoard()[start_row][start_col] = null;

				board.getPieceMap().remove(move[0]);
				board.getPieceMap().put(move[1], board.getBoard()[end_row][end_col]);
			}
		}
	}

	/**
	 * Iterates over PieceNotation[][] board to find king of an indicated color ("K"
	 * = white king, "k" = black king) Returns array of row index and column index
	 * of indicated king.
	 */
	public static int[] findKingPosition(PieceNotation[][] board, PieceNotation Kk) {

		if (Kk == PieceNotation.K || Kk == PieceNotation.k) {
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					if (board[i][j] == Kk) {
						return new int[] { i, j };
					}
				}
			}
		}

		return new int[] { -1, -1 };
	}

	/**
	 * Generates and returns a deep copy of a chess board PieceNotation[8][8].
	 */
	public static PieceNotation[][] copyBoard(PieceNotation[][] board) {
		PieceNotation[][] newBoard = new PieceNotation[8][];

		for (int i = 0; i < 8; i++) {
			newBoard[i] = Arrays.copyOf(board[i], 8);
		}

		return newBoard;
	}

	public static StringBuilder getFenPositions(PieceNotation[][] board) {
		StringBuilder fenPos = new StringBuilder(100);
		int emptyCount = 0;

		for (int row = 0; row < 8; row++) {
			emptyCount = 0;
			for (int col = 0; col < 8; col++) {
				if (board[row][col] == null) {
					emptyCount++;
				} else {
					if (emptyCount != 0) {
						fenPos.append(String.valueOf(emptyCount));
						emptyCount = 0;
					}
					fenPos.append(board[row][col].toString());
				}

				if (col == 7 && emptyCount != 0) {
					fenPos.append(String.valueOf(emptyCount));
				}
			}

			if (row > 0) {
				fenPos.append("/");
			}
		}
		return fenPos;
	}
}
