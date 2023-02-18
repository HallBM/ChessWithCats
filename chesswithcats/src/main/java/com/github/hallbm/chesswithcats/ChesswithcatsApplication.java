package com.github.hallbm.chesswithcats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.github.hallbm.*"})
public class ChesswithcatsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChesswithcatsApplication.class, args);
	}

}
