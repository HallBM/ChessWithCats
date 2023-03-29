package com.github.hallbm.chesswithcats.domain;

/**
 * List of enum sets for standardizing options for games and game requests
 */
public class GameEnums {

	/**
	 * List of current chess variations available
	 */	
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

	/**
	 * Chess piece types
	 * 'Cat' piece included for 'Obstructive' chess style
	 */	
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
	
	/**
	 * Chess pieces denoted by chess piece notation 
	 * 'Cat' piece "C" included for 'Obstructive' chess style
	 */	
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
	
	/**
	 * Available chess moves for tracking move validation and response
	 */	
	public enum ChessMove{
		SIMPLE_MOVE, CAPTURE, 
		EN_PASSANT_CAPTURE, PAWN_INITIAL_DOUBLE, 
		KING_SIDE_CASTLE, QUEEN_SIDE_CASTLE,
		PROMOTE_QUEEN, PROMOTE_KNIGHT, PROMOTE_ROOK, PROMOTE_BISHOP,
		CHECK, CHECKMATE; 
	}
	
	/**
	 * All possible game outcomes for the Game class
	 * Checkmate and Timeout are WIN/LOSE scenario.
	 * Accepted (moves haven't been made) and Incomplete (unfinished games) included
	 * to track game progress.
	 * All remaining are DRAW scenarios. 
	 */	
	public enum GameOutcome {
		CHECKMATE, TIMEOUT,
		RESIGNATION, STALEMATE, INSUFFICIENT_MATERIAL, EXCESSIVE_MOVE_RULE, REPETITION, AGREEMENT, 
		ACCEPTED, INCOMPLETE;
	}
	
	/**
	 * Game outcomes with respect to win, lose and draw (WLD) 
	 */	
	public enum GameWLD{
		WIN, LOSE, DRAW;
	}
	
	/**
	 * Game colors used in game (neutral added for 'cat' pieces) 
	 */	
	public enum GameColor {
		WHITE, BLACK, NEUTRAL;
	}
}
