package com.github.hallbm.chesswithcats.model;

import java.io.Serializable;
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
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class GameBoard implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Column(length = 1024)
	@Size(max = 1024)
	private PieceNotation [][] board = new PieceNotation[8][8];
	
	@ElementCollection
	@CollectionTable(name="piece_map")
	@MapKeyColumn(name = "position", columnDefinition="CHAR(2)")
	@MapKeyClass(String.class)
	@Column(name = "piece_type", columnDefinition="VARCHAR(2)")
	@Enumerated(EnumType.STRING)
	@JoinColumn(name = "game_plays_id", referencedColumnName = "id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Size(max = 36)
	private Map<String, PieceNotation> pieceMap = new HashMap<>();;
	
	public StringBuilder getFenPositions() {
		StringBuilder fenPos = new StringBuilder(100);
		int emptyCount = 0;

		for (int row = 0; row <8; row++) {
			emptyCount = 0;
			for (int col = 0; col < 8; col++) {
				if(board[row][col] == null) {
					emptyCount++;
				} else {
					if (emptyCount != 0) {
						fenPos.append(String.valueOf(emptyCount));
						emptyCount = 0;
					}
					fenPos.append(board[row][col].toString());
				}

				if (col == 7 && emptyCount !=0) {
					fenPos.append(String.valueOf(emptyCount));
				}
			}

			if (row > 0) {
				fenPos.append("/");
			}
		}
		return fenPos;
	}

}
