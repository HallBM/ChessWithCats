package com.github.hallbm.chesswithcats.dto;

import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameRequestDTO {
	String id;
	String time;
	String opponent;
	GameStyle style;
}
