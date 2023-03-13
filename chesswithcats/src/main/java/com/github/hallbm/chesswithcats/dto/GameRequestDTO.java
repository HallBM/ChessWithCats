package com.github.hallbm.chesswithcats.dto;

import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for transmitting data from front end for creation of new Game Request
 */

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
