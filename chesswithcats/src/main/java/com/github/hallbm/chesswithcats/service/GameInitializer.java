package com.github.hallbm.chesswithcats.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GameInitializer implements CommandLineRunner {

	@Autowired
	private GameManager gameManager;
	
	@Override
	public void run(String... args) throws Exception{
		gameManager.start();
	}
	
}
