package com.github.hallbm.chesswithcats.service;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.hallbm.chesswithcats.domain.GameEnums.PieceNotation;

public class GameBoardServicesTest {

	private PieceNotation [][] board;
	
	
	@ParameterizedTest
	@MethodSource("kingPositions")
	public void testFindKingPosition(int[] expected, PieceNotation king) {
	
		board = new PieceNotation[8][8];
		board[expected[0]][expected[1]] = king;
		
		int[] actual = GameBoardServices.findKingPosition(board, king);
	
		if(king == PieceNotation.K || king == PieceNotation.k) {
			Assertions.assertArrayEquals(expected,actual);
		} else {
			expected = new int [] {-1,-1};
			Assertions.assertArrayEquals(expected, actual); 
		}
	}
	
    private static Stream<Arguments> kingPositions() {
        return Stream.of(
                Arguments.of(new int[]{3, 2}, PieceNotation.K),
                Arguments.of(new int[]{5, 7}, PieceNotation.K),
                Arguments.of(new int[]{3, 2}, PieceNotation.k),
                Arguments.of(new int[]{5, 7}, PieceNotation.k),
                Arguments.of(new int[]{3, 2}, PieceNotation.n),
                Arguments.of(new int[]{5, 7}, PieceNotation.N)
        );
    }
}
