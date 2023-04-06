package com.github.hallbm.chesswithcats.model;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.github.hallbm.chesswithcats.domain.GameEnums.PieceNotation;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyClass;
import jakarta.persistence.MapKeyColumn;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Short-term persistence of active game states, including 1) board state
 * comprised of a 8x8 matrix of piece enums: PieceNotation[][] 2) hashmap of
 * current square mapping associated PieceNotation enum, e.g., "A5":"Q"
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class GameBoard {

	@Column(length = 1024)
	@Size(max = 1024)
	private PieceNotation[][] board = new PieceNotation[8][8];

	@ElementCollection
	@CollectionTable(name = "piece_map")
	@MapKeyColumn(name = "position", columnDefinition = "CHAR(2)")
	@MapKeyClass(String.class)
	@Column(name = "piece_type", columnDefinition = "VARCHAR(2)")
	@Enumerated(EnumType.STRING)
	@JoinColumn(name = "game_plays_id", referencedColumnName = "id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Size(max = 36)
	private Map<String, PieceNotation> pieceMap = new HashMap<>();;

}
