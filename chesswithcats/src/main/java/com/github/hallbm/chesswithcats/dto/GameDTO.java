package com.github.hallbm.chesswithcats.dto;

import com.github.hallbm.chesswithcats.domain.GameEnums.GameColor;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameOutcome;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameWLD;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameDTO {
	private String id;
	private String opponent;
	private GameStyle style;
	private GameColor color;
	private String turn;
	private GameOutcome outcome;
	private GameWLD winLoseDraw;
}
