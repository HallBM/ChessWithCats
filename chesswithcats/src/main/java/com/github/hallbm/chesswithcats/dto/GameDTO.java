package com.github.hallbm.chesswithcats.dto;

import com.github.hallbm.chesswithcats.model.Game.GameColor;
import com.github.hallbm.chesswithcats.model.Game.GameOutcome;
import com.github.hallbm.chesswithcats.model.Game.GameStyle;
import com.github.hallbm.chesswithcats.model.Game.GameWLD;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameDTO {
	String gameId;
	String opponent;
	GameStyle style;
	GameColor color;
	String turn;
	GameOutcome outcome;
	GameWLD winLoseDraw;
}
