package com.github.hallbm.chesswithcats.enums;

public enum GameStyles {
	GAME0("Classic Chess"), 
	GAME1("Obstructive Kitties"), 
	GAME2("Ambiguous Kitties"), 
	GAME3("Defiant Kitties");
	
	private final String description;

	private GameStyles(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
