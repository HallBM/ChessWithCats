package com.github.hallbm.chesswithcats.dto;

import java.util.ArrayList;
import java.util.List;

import com.github.hallbm.chesswithcats.domain.GameEnums.GameOutcome;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for generating (via MoveValidator classes) and transmitting move response for the AJAX (Fetch API) call
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoveResponseDTO {
	
	private List<String[]> pieceMoves = new ArrayList<>();
	private String moveNotation = "";
	private GameOutcome gameOutcome;
	
}
