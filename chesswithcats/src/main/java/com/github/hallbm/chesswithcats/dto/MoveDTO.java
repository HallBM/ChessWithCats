package com.github.hallbm.chesswithcats.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for transmitting move data from front end (AJAX via Fetch API) for validation
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoveDTO {

	private String gameId;
	private String gameStyle;
	private String isChecked;
	private String promotionPiece;
	private String startPos;
	private String endPos;
	
}
