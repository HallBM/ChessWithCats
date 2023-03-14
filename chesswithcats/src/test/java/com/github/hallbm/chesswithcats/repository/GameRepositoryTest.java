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

import com.github.hallbm.chesswithcats.domain.GameEnums.GameOutcome;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;
import com.github.hallbm.chesswithcats.model.Game;
import com.github.hallbm.chesswithcats.model.GamePlay;
import com.github.hallbm.chesswithcats.model.Player;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class GameRepositoryTest {

	@Autowired
	private GameRequestRepository gameReqRepo;
	
	@Autowired
	private PlayerRepository playerRepo;
	
	@Autowired
	private GamePlayRepository gamePlayRepo;

	@Autowired
	private GameRepository gameRepo;
	
	private Player player1, player2, player3, player4;
	private Game g1, g2, g3, g4, g5, g6, g7;
	private GamePlay gp1, gp2, gp3, gp4, gp5, gp6, gp7;
	
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
		player2.setDateJoined(LocalDate.now());

		player3 = new Player();
		player3.setId(3003L);
		player3.setUsername("player3_username");
		player3.setIconFile("mockImage.jpg");
		player3.setPassword("32#$qwER");
		player3.setFirstName("John3");
		player3.setLastName("Doe3");
		player3.setEmail("jd3@gmail.com");
		player3.setDateJoined(LocalDate.now());

		player4 = new Player();
		player4.setId(4004L);
		player4.setUsername("player4_username");
		player4.setIconFile("mockImage.jpg");
		player4.setPassword("42#$qwER");
		player4.setFirstName("John4");
		player4.setLastName("Doe4");
		player4.setEmail("jd4@gmail.com");
		player4.setDateJoined(LocalDate.now());

		playerRepo.save(player1);
		playerRepo.save(player2);
		playerRepo.save(player3);
		playerRepo.save(player4);

		
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
		
		gp2 = new GamePlay();
		gamePlayRepo.save(gp2);
		
		g2 = new Game();
		g2.setId(2);
		g2.setOpeningFen("");
		g2.setStyle(GameStyle.OBSTRUCTIVE);
		g2.setWhite(player1);
		g2.setBlack(player2);
		g2.setGamePlay(gp2);
		gp2.setGame(g2);
		gameRepo.save(g2);
		
		gp3 = new GamePlay();
		gamePlayRepo.save(gp3);
		
		g3 = new Game();
		g3.setId(4);
		g3.setOpeningFen("");
		g3.setStyle(GameStyle.AMBIGUOUS);
		g3.setWhite(player2);
		g3.setBlack(player1);
		g3.setGamePlay(gp3);
		gp3.setGame(g3);
		gameRepo.save(g3);
		
		gp4 = new GamePlay();
		gamePlayRepo.save(gp4);
		
		g4 = new Game();
		g4.setId(6);
		g4.setOpeningFen("");
		g4.setStyle(GameStyle.DEFIANT);
		g4.setWhite(player2);
		g4.setBlack(player1);
		g4.setGamePlay(gp4);
		gp4.setGame(g4);
		gameRepo.save(g4);
		
		gp5 = new GamePlay();
		gamePlayRepo.save(gp5);
		
		g5 = new Game();
		g5.setId(8);
		g5.setOpeningFen("");
		g5.setStyle(GameStyle.CLASSIC);
		g5.setOutcome(GameOutcome.CHECKMATE);
		g5.setWinner(player1.getUsername());
		g5.setWhite(player1);
		g5.setBlack(player3);
		g5.setGamePlay(gp5);
		gp5.setGame(g5);
		gameRepo.save(g5);
		
		
		
		gp6 = new GamePlay();
		gamePlayRepo.save(gp6);
		
		g6 = new Game();
		g6.setId(10);
		g6.setOpeningFen("");
		g6.setStyle(GameStyle.AMBIGUOUS);
		g6.setOutcome(GameOutcome.CHECKMATE);
		g6.setWinner(player3.getUsername());
		g6.setWhite(player3);
		g6.setBlack(player4);
		g6.setGamePlay(gp6);
		gp6.setGame(g6);
		gameRepo.save(g6);
		
		gp7 = new GamePlay();
		gamePlayRepo.save(gp7);
		
		g7 = new Game();
		g7.setId(12);
		g7.setOpeningFen("");
		g7.setStyle(GameStyle.DEFIANT);
		g7.setOutcome(GameOutcome.RESIGNATION);
		g7.setWinner("Draw");
		g7.setWhite(player3);
		g7.setBlack(player4);
		g7.setGamePlay(gp7);
		gp7.setGame(g7);
		gameRepo.save(g7);
	}

	@DisplayName("Test for getActiveByUsername")
	@Test
	public void givenUsername_whenGetActiveByUsername_thenReturnGameList() {

		List<Game> gameListDB1 = gameRepo.getActiveByUsername(player1.getUsername());
		assertThat(gameListDB1).isNotNull();
		assertThat(gameListDB1.size()).isEqualTo(4);

		List<Game> gameListDB2 = gameRepo.getActiveByUsername(player2.getUsername());
		assertThat(gameListDB2).isNotNull();
		assertThat(gameListDB2.size()).isEqualTo(4);
		
		List<Game> gameListDB3 = gameRepo.getActiveByUsername(player3.getUsername());
		assertThat(gameListDB3).isEmpty();
	}
	
	@DisplayName("Test for getCompleteByUsername")
	@Test
	public void givenUsername_whenGetCompleteByUsername_thenReturnGameList() {
		List<Game> gameListDB1 = gameRepo.getCompleteByUsername(player1.getUsername());
		assertThat(gameListDB1).isNotNull();
		assertThat(gameListDB1.size()).isEqualTo(1);

		List<Game> gameListDB2 = gameRepo.getCompleteByUsername(player2.getUsername());
		assertThat(gameListDB2).isEmpty();
		
		List<Game> gameListDB3 = gameRepo.getCompleteByUsername(player3.getUsername());
		assertThat(gameListDB3).isNotNull();
		assertThat(gameListDB3.size()).isEqualTo(3);
	}
	
	@DisplayName("Test for getCompleteByUsernameOrderByStyle")
	@Test
	public void givenUsername_whenGetCompleteByUsernameOrderByStyle_thenReturnGameList() {
		List<Game> gameListDB1 = gameRepo.getCompleteByUsernameOrderByStyle(player1.getUsername());
		assertThat(gameListDB1).isNotNull();
		assertThat(gameListDB1.size()).isEqualTo(1);

		List<Game> gameListDB2 = gameRepo.getCompleteByUsernameOrderByStyle(player2.getUsername());
		assertThat(gameListDB2).isEmpty();
		
		List<Game> gameListDB3 = gameRepo.getCompleteByUsernameOrderByStyle(player3.getUsername());
		assertThat(gameListDB3).isNotNull();
		assertThat(gameListDB3.size()).isEqualTo(3);
		assertThat(gameListDB3.get(0).getStyle()).isEqualTo(GameStyle.AMBIGUOUS);
	}
	
	@DisplayName("Test for getCompleteByOpponentsOrderByStyle")
	@Test
	public void givenWhiteAndBlackUsername_whenGetCompleteByOpponentsOrderByStyle_thenReturnGameList() {
		List<Game> gameListDB1 = gameRepo.getCompleteByOpponentsOrderByStyle(player1.getUsername(), player2.getUsername());
		assertThat(gameListDB1).isEmpty();
		assertThat(gameListDB1.size()).isEqualTo(0);

		List<Game> gameListDB2 = gameRepo.getCompleteByOpponentsOrderByStyle(player3.getUsername(),player4.getUsername());
		assertThat(gameListDB2.size()).isEqualTo(2);
		assertThat(gameListDB2.get(0).getStyle()).isEqualTo(GameStyle.AMBIGUOUS);
	}
	
}