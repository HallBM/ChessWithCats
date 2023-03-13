package com.github.hallbm.chesswithcats.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import com.github.hallbm.chesswithcats.domain.AmbiguousMoveValidator;
import com.github.hallbm.chesswithcats.domain.DefiantMoveValidator;
import com.github.hallbm.chesswithcats.domain.GameEnums.ChessMove;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameOutcome;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;
import com.github.hallbm.chesswithcats.domain.MoveValidator;
import com.github.hallbm.chesswithcats.domain.ObstructiveMoveValidator;
import com.github.hallbm.chesswithcats.dto.MoveDTO;
import com.github.hallbm.chesswithcats.dto.MoveResponseDTO;
import com.github.hallbm.chesswithcats.model.GamePlay;
import com.github.hallbm.chesswithcats.repository.GamePlayRepository;
import com.github.hallbm.chesswithcats.repository.GameRepository;

import jakarta.transaction.Transactional;

@Service
public class GamePlayServices {

	@Autowired
	GamePlayRepository gamePlayRepo;

	@Autowired
	GameRepository gameRepo;
	
	public Optional<MoveResponseDTO> validateMove(MoveDTO moveDTO, GamePlay gamePlay) {

		MoveValidator moveValidator;
		switch(GameStyle.valueOf(moveDTO.getGameStyle().toUpperCase())) {
			case OBSTRUCTIVE: 
				moveValidator = new ObstructiveMoveValidator(gamePlay, moveDTO);
				break;
			case DEFIANT:
				moveValidator = new DefiantMoveValidator(gamePlay, moveDTO);
				break;
			case AMBIGUOUS:
				moveValidator = new AmbiguousMoveValidator(gamePlay, moveDTO);
				break;
			case CLASSIC:
			default:
				moveValidator = new MoveValidator(gamePlay, moveDTO);
		}
		return Optional.ofNullable(moveValidator.validate());
	}
	
	@Modifying
	@Transactional
	public void finalizeAndSaveGameState(MoveDTO moveDTO, MoveResponseDTO moveResponseDTO, GamePlay gamePlay) {
		
		GameBoardServices.movePiece(gamePlay.getGameBoard(), moveResponseDTO.getPieceMoves(), moveDTO.getPromotionPiece());
		
		if (gamePlay.getHalfMoves() == 1) {
			gamePlay.getGame().setOutcome(GameOutcome.INCOMPLETE);
		}
		
		boolean isCaptureMove = moveResponseDTO.getChessMoves().contains(ChessMove.CAPTURE);
		boolean isPawnMove = moveResponseDTO.getOfficialChessMove().toLowerCase().startsWith("p");
		
		if (isCaptureMove || isPawnMove) {
			gamePlay.resetFiftyMoveClock();
		} else {	
			gamePlay.incrementFiftyMoveClock();
		}
		
		gamePlay.addMove(moveResponseDTO.getOfficialChessMove()); // updates move for both String and StringBuffer
		gamePlay.incrementHalfMoves(); 
		gamePlay.updateFenSet();
	
		gamePlayRepo.save(gamePlay);
	}
	
}