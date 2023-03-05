package com.github.hallbm.chesswithcats.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.hallbm.chesswithcats.repository.GamePlayRepository;
import com.github.hallbm.chesswithcats.repository.GameRepository;

@Service
public class GamePlayServices {

	@Autowired
	GamePlayRepository gamPlayRepo;

	@Autowired
	GameRepository gameRepo;

	/*
	public static final String [] columnLetters = new String [] {"A","B","C","D","E","F","G","H"};

	public GameBoard(String gameFen) {
		this.gameFen = gameFen;
		String [] splitFen = gameFen.split(" ");
		loadPieces(splitFen[0]);

	}

	private static void loadPieces(String boardPositions) {
		String [] row = positions;


	}

	public GameBoard(String gameFen) {
		this.gameFen = gameFen;
		String [] splitFen = gameFen.split(" ");
		loadPieces(splitFen[0]);



	}

	private static void loadPieces(String boardPositions) {
		String [] row = positions



	}
	*/




	//check50clock; reset if pawn moves or if capture (50 moves without pawn move or capture)




}