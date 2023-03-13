package com.github.hallbm.chesswithcats.domain;

import com.github.hallbm.chesswithcats.dto.MoveDTO;
import com.github.hallbm.chesswithcats.model.GamePlay;

public class AmbiguousMoveValidator extends MoveValidator{

	public AmbiguousMoveValidator(GamePlay gamePlay, MoveDTO moveDTO) {
		super(gamePlay, moveDTO);
	}

	
	
	
}
