package com.github.hallbm.chesswithcats.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;
import com.github.hallbm.chesswithcats.model.Game;
import com.github.hallbm.chesswithcats.model.GamePlay;
import com.github.hallbm.chesswithcats.model.GameRequest;
import com.github.hallbm.chesswithcats.model.Player;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class GamePlayRepositoryTest {

	@Autowired
	private GameRequestRepository gameReqRepo;
	
	@Autowired
	private PlayerRepository playerRepo;
	
	@Autowired
	private GamePlayRepository gamePlayRepo;

	@Autowired
	private GameRepository gameRepo;
	
	private Player player1, player2;
	private Game g1;
	private GamePlay gp1;
	
	@BeforeEach
	public void setUp() {
		player1 = new Player();
		player1.setId(1001L);
		player1.setUsername("player1_username");
		player1.setIconFile("mockImage.jpg");
		player1.setPassword("12#$qwER");
		player1.setFirstName("John1");
		player1.setLastName("Doe1");
		player1.setEmail("jd1@gmail.com");
		player1.setDateJoined(LocalDate.now());

		player2 = new Player();
		player2.setId(2002L);
		player2.setUsername("player2_username");
		player2.setIconFile("mockImage.jpg");
		player2.setPassword("22#$qwER");
		player2.setFirstName("John2");
		player2.setLastName("Doe2");
		player2.setEmail("jd2@gmail.com");
		player2.setDateJoined(LocalDate.now());;

		playerRepo.save(player1);
		playerRepo.save(player2);

		gp1 = new GamePlay();
		gamePlayRepo.save(gp1);
		
		g1 = new Game();
		g1.setId(0);
		g1.setOpeningFen("");
		g1.setStyle(GameStyle.CLASSIC);
		g1.setWhite(player1);
		g1.setBlack(player2);
		g1.setGamePlay(gp1);
		gp1.setGame(g1);
		gameRepo.save(g1);

	}
	
	@DisplayName("Test for findByGameId")
	@Test
	public void givenGameId_whenFindByGameId_thenReturnGamePlay() {
		GamePlay gamePlayDB1 = gamePlayRepo.findByGameId(g1.getId());
		assertThat(gamePlayDB1).isNotNull();
		assertThat(gamePlayDB1.getId()).isEqualTo(g1.getGamePlay().getId());
	}
}	