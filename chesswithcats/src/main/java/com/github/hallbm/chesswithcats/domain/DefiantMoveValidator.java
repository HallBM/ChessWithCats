package com.github.hallbm.chesswithcats.domain;

import com.github.hallbm.chesswithcats.dto.MoveDTO;
import com.github.hallbm.chesswithcats.model.GamePlay;

public class DefiantMoveValidator extends MoveValidator{

	public DefiantMoveValidator(GamePlay gamePlay, MoveDTO moveDTO) {
		super(gamePlay, moveDTO);
	}

}
