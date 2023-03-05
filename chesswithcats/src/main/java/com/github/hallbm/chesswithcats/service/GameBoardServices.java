package com.github.hallbm.chesswithcats.service;

import java.util.Random;

import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;
import com.github.hallbm.chesswithcats.domain.GameEnums.PieceNotation;
import com.github.hallbm.chesswithcats.model.Game;

public final class GameBoardServices {
	
	private final static String [][] squares = {
			{"A8","B8","C8","D8","E8","F8","G8","H8"},
			{"A7","B7","C7","D7","E7","F7","G7","H7"},
			{"A6","B6","C6","D6","E6","F6","G6","H6"},
			{"A5","B5","C5","D5","E5","F5","G5","H5"}, 
			{"A4","B4","C4","D4","E4","F4","G4","H4"},
			{"A3","B3","C3","D3","E3","F3","G3","H3"},
			{"A2","B2","C2","D2","E2","F2","G2","H2"},
			{"A1","B1","C1","D1","E1","F1","G1","H1"}};
	
	public static String getSquare(int row, int col) {
		return squares[row][col];
	}
	
	
	public static void setupGameBoard(Game game) {

		String [] pieces = "rnbqkbnrppppppppPPPPPPPPRNBQKBNR".split("");
		int index = 0;

		for (int row : new int[] {0,1,6,7}) {
			for (int col = 0; col < 8; col++) {
				game.getGamePlay().getGameBoard().getBoard()[row][col] = PieceNotation.valueOf(pieces[index]);
				game.getGamePlay().getGameBoard().getPieceMap().put(GameBoardServices.getSquare(row, col), PieceNotation.valueOf(pieces[index++]));
			}
		}
		
		if(game.getStyle() == GameStyle.OBSTRUCTIVE) {
			addFourCats(game);
		} 
	}
	
	public static void addFourCats(Game game) {

		int[] quad1 = new int[2];
		int[] quad2 = new int[2];
		int[] quad3 = new int[2];
		int[] quad4 = new int[2];
		
		if(Math.random()<0.5) {
			quad1[0] = 2;
			quad2[0] = 3;
		} else {
			quad1[0] = 3;
			quad2[0] = 2;
		}
		
		if(Math.random()<0.5) {
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
			quad2[1] = temp1+4;
			quad3[1] = temp2+4;
		} else {
			quad2[1] = temp1+4;
			quad4[1] = temp2+4;
		}

		int[][] quads = {quad1, quad2, quad3, quad4};
		
		for (int[] q : quads) {
			game.getGamePlay().getGameBoard().getBoard()[q[0]][q[1]] = PieceNotation.C;
			game.getGamePlay().getGameBoard().getPieceMap().put(GameBoardServices.getSquare(q[0], q[1]), PieceNotation.C);
		}
	}

}
