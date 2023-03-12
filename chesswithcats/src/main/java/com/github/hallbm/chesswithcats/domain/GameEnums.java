package com.github.hallbm.chesswithcats.domain;

public class GameEnums {

	public enum GameStyle {
		CLASSIC("Classic Chess"),
		OBSTRUCTIVE("Obstructive Kitties"),
		AMBIGUOUS("Ambiguous Kitties"),
		DEFIANT("Defiant Kitties");

		private final String description;

		private GameStyle(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}

	public enum PieceType {
		KING("K"),
		QUEEN("Q"),
		BISHOP("B"),
		KNIGHT("N"),
		ROOK("R"),
		PAWN("P"),
		CAT("C");

		private final String notation;

		private PieceType(String notation) {
			this.notation = notation;
		}

		public String getCatNotation() {
			return PieceType.CAT.notation;
		}
		
		public String getNotation(GameColor color) {
			return color == GameColor.BLACK ? notation.toLowerCase() : notation;
		}
	}
	
	public enum PieceNotation{
		R(GameColor.WHITE, PieceType.ROOK),
		N(GameColor.WHITE, PieceType.KNIGHT),
		B(GameColor.WHITE, PieceType.BISHOP),
		Q(GameColor.WHITE, PieceType.QUEEN),
		K(GameColor.WHITE, PieceType.KING),
		P(GameColor.WHITE, PieceType.PAWN),
		r(GameColor.BLACK, PieceType.ROOK),
		n(GameColor.BLACK, PieceType.KNIGHT),
		b(GameColor.BLACK, PieceType.BISHOP),
		q(GameColor.BLACK, PieceType.QUEEN),
		k(GameColor.BLACK, PieceType.KING),
		p(GameColor.BLACK, PieceType.PAWN),
		
		C(GameColor.NEUTRAL, PieceType.CAT);
		
		private final GameColor color;
	    private final PieceType type;
	    
	    PieceNotation(GameColor color, PieceType type) {
	        this.color = color;
	        this.type = type;
	    }
	    
	    public GameColor getColor() {
	        return color;
	    }
	    
	    public PieceType getType() {
	        return type;
	    }
	}
	
	public enum ChessMove{
		SIMPLE_MOVE, CAPTURE, 
		EN_PASSANT, 
		KING_SIDE_CASTLE, QUEEN_SIDE_CASTLE,
		PROMOTE_QUEEN, PROMOTE_KNIGHT, PROMOTE_ROOK, PROMOTE_BISHOP,
		CHECK; 
	}
	
	public enum GameOutcome {
		CHECKMATE, TIMEOUT,
		RESIGNATION, STALEMATE, INSUFFICIENT_MATERIAL, EXCESSIVE_MOVE_RULE, REPETITION, AGREEMENT, 
		ACCEPTED, INCOMPLETE;
	}

	public enum GameWLD{
		WIN, LOSE, DRAW;
	}

	public enum GameColor {
		WHITE, BLACK, NEUTRAL;
	}
}
