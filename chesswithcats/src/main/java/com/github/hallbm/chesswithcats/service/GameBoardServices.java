package com.github.hallbm.chesswithcats.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;
import com.github.hallbm.chesswithcats.domain.GameEnums.PieceNotation;
import com.github.hallbm.chesswithcats.model.Game;
import com.github.hallbm.chesswithcats.model.GameBoard;

/**
 * Services associated with maintenance and properties of the actual chess board state.
 */

public final class GameBoardServices {
	
	/**
	 * Square positions of the corresponding PieceNotation[][] board: e.g., [0][0] = "A8".
	 */
	private final static String[][] squares = { 
			{ "A8", "B8", "C8", "D8", "E8", "F8", "G8", "H8" },
			{ "A7", "B7", "C7", "D7", "E7", "F7", "G7", "H7" }, 
			{ "A6", "B6", "C6", "D6", "E6", "F6", "G6", "H6" },
			{ "A5", "B5", "C5", "D5", "E5", "F5", "G5", "H5" }, 
			{ "A4", "B4", "C4", "D4", "E4", "F4", "G4", "H4" },
			{ "A3", "B3", "C3", "D3", "E3", "F3", "G3", "H3" }, 
			{ "A2", "B2", "C2", "D2", "E2", "F2", "G2", "H2" },
			{ "A1", "B1", "C1", "D1", "E1", "F1", "G1", "H1" }};

	/**
	 * Mapping of column letter to PieceNotation[][] board index.
	 */
	private final static Map<Character, Integer> columnMap = new HashMap<>() {
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

	public static String getSquare(int row, int col) {
		return squares[row][col];
	}

	public static int getColumn(String location) {
		return columnMap.get(location.charAt(0));
	}

	public static int getRow(String location) {
		return rowMap.get(location.charAt(1));
	}

	/**
	 * Initial game setup of classic chess game.
	 * String[] pieces converted to PieceNotation. 
	 * PieceNotation [][] board and HashMap<String,PieceNotation> pieceMap assigned pieces.
	 */
	public static void setupGameBoard(Game game) {

		String[] pieces = "rnbqkbnrppppppppPPPPPPPPRNBQKBNR".split("");
		int index = 0;

		for (int row : new int[] { 0, 1, 6, 7 }) {
			for (int col = 0; col < 8; col++) {
				game.getGamePlay().getGameBoard().getBoard()[row][col] = PieceNotation.valueOf(pieces[index]);
				game.getGamePlay().getGameBoard().getPieceMap().put(GameBoardServices.getSquare(row, col),
						PieceNotation.valueOf(pieces[index++]));
			}
		}

		if (game.getStyle() == GameStyle.OBSTRUCTIVE) {
			addFourCats(game);
		}
	}

	/**
	 * For 'obstructive' game, 4 cats added to the game board (1 per quadrant)
	 * PieceNotation [][] board and HashMap<String,PieceNotation> pieceMap assigned cats.
	 */
	public static void addFourCats(Game game) {
		int[] quad1 = new int[2];
		int[] quad2 = new int[2];
		int[] quad3 = new int[2];
		int[] quad4 = new int[2];

		if (Math.random() < 0.5) {
			quad1[0] = 2;
			quad2[0] = 3;
		} else {
			quad1[0] = 3;
			quad2[0] = 2;
		}

		if (Math.random() < 0.5) {
			quad3[0] = 4;
			quad4[0] = 5;
		} else {
			quad3[0] = 5;
			quad4[0] = 4;
		}

		Random random = new Random();

		int temp1 = random.nextInt(4);
		int temp2 = random.nextInt(4);
		while (temp1 == temp2) {
			temp2 = random.nextInt(4);
		}

		if (quad1[0] == quad3[0]) {
			quad1[1] = temp1;
			quad3[1] = temp2;
		} else {
			quad1[1] = temp1;
			quad4[1] = temp2;
		}

		temp1 = random.nextInt(4);
		temp2 = random.nextInt(4);
		while (temp1 == temp2) {
			temp2 = random.nextInt(4);
		}

		if (quad2[0] == quad3[0]) {
			quad2[1] = temp1 + 4;
			quad3[1] = temp2 + 4;
		} else {
			quad2[1] = temp1 + 4;
			quad4[1] = temp2 + 4;
		}

		int[][] quads = { quad1, quad2, quad3, quad4 };

		for (int[] q : quads) {
			game.getGamePlay().getGameBoard().getBoard()[q[0]][q[1]] = PieceNotation.C;
			game.getGamePlay().getGameBoard().getPieceMap().put(GameBoardServices.getSquare(q[0], q[1]),
					PieceNotation.C);
		}
	}
	
	/**
	 * Movement of chess pieces on the board
	 * @Param PieceNotation[][] board;
	 * @Param List<String[]> moves from moveResponseDTO; String[2] represents [startPos, endPos], e.g., ["A3","B4"]
	 * @Param String promotionPiece from moveDTO to indicate what piece a pawn was promoted to
	 * 
	 * PieceNotation [][] board and HashMap<String,PieceNotation> pieceMap updated.
	 */
	public static void movePiece(PieceNotation[][] board, List<String[]> moves, String promotionPiece) {
		int start_row;
		int start_col;
		int end_row;
		int end_col;

		for (String[] move : moves) {
			start_row = getRow(move[0]);
			start_col = getColumn(move[0]);

			if (move[1] == null) { // en passant captured piece
				board[start_row][start_col] = null;
				continue;
			}

			end_row = getRow(move[1]);
			end_col = getColumn(move[1]);

			if (promotionPiece != null) {
				board[end_row][end_col] = null;
				board[end_row][end_col] = PieceNotation.valueOf(promotionPiece);
				board[start_row][start_col] = null;

			} else {
				board[end_row][end_col] = null;
				board[end_row][end_col] = board[start_row][start_col];
				board[start_row][start_col] = null;
			}
		}
	}
	
	/**
	 * Movement of chess pieces on the board
	 * @Param GameBoard
	 * @Param List<String[]> moves from moveResponseDTO; String[2] represents [startPos, endPos], e.g., ["A3","B4"]
	 * @Param String promotionPiece from moveDTO to indicate what piece a pawn was promoted to
	 * 
	 * PieceNotation [][] board and HashMap<String,PieceNotation> pieceMap updated.
	 */
	public static void movePiece(GameBoard board, List<String[]> moves, String promotionPiece) {
		int start_row;
		int start_col;
		int end_row;
		int end_col;

		for (String[] move : moves) {
			start_row = getRow(move[0]);
			start_col = getColumn(move[0]);

			if (move[1] == null) { // en passant captured piece
				board.getBoard()[start_row][start_col] = null;
				board.getPieceMap().remove(move[0]);
				continue;
			}

			end_row = getRow(move[1]);
			end_col = getColumn(move[1]);

			if (promotionPiece != null) {
				board.getBoard()[end_row][end_col] = null;
				board.getBoard()[end_row][end_col] = PieceNotation.valueOf(promotionPiece);
				board.getBoard()[start_row][start_col] = null;

				board.getPieceMap().remove(move[0]);
				board.getPieceMap().put(move[1], board.getBoard()[end_row][end_col]);
			} else {
				board.getBoard()[end_row][end_col] = null;
				board.getBoard()[end_row][end_col] = board.getBoard()[start_row][start_col];
				board.getBoard()[start_row][start_col] = null;

				board.getPieceMap().remove(move[0]);
				board.getPieceMap().put(move[1], board.getBoard()[end_row][end_col]);
			}
		}
	}

	/**
	 * Iterates over PieceNotation[][] board to find king of an indicated color ("K" = white king, "k" = black king)
	 * Returns array of row index and column index of indicated king.
	 */
	public static int[] findKingPosition(PieceNotation[][] board, PieceNotation Kk) {
		
		if( Kk == PieceNotation.K || Kk == PieceNotation.k) {
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					if (board[i][j] == Kk) {
						return new int[]{i,j};
					}
				}
			}
		} 
		
		return new int[]{-1, -1};
	}

	/**
	 * Generates and returns a deep copy of a chess board PieceNotation[8][8].
	 */
	public static PieceNotation[][] copyBoard(PieceNotation[][] board){
		PieceNotation[][] newBoard = new PieceNotation[8][];
		
		for (int i =0; i<8; i++) {
			newBoard[i] = Arrays.copyOf(board[i], 8);
		}
		
		for (int i =0; i<8; i++) {
			for (int j=0; j<8; j++) {
				System.out.println(board[i][j] != null ? (board[i][j].toString() + newBoard[i][j].toString()) : "empty");
			}
		}
		
		return newBoard;
	}
}
