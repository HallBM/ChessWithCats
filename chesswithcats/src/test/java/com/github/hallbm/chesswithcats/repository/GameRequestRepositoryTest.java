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
import com.github.hallbm.chesswithcats.model.GameRequest;
import com.github.hallbm.chesswithcats.model.Player;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class GameRequestRepositoryTest {

	@Autowired
	private GameRequestRepository gameReqRepo;
	@Autowired
	private PlayerRepository playerRepo;

	private Player player1, player2, player3, player4;
	private GameRequest gr12, gr13, gr14, gr34, gr24;

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

		gr12 = new GameRequest();
		gr12.setId(0);
		gr12.setSender(player1);
		gr12.setReceiver(player2);
		gr12.setStyle(GameStyle.CLASSIC);

		gr13 = new GameRequest();
		gr13.setId(1);
		gr13.setSender(player1);
		gr13.setReceiver(player3);
		gr13.setStyle(GameStyle.AMBIGUOUS);


		gr14 = new GameRequest();
		gr14.setId(2);
		gr14.setSender(player1);
		gr14.setReceiver(player4);
		gr14.setStyle(GameStyle.CLASSIC);


		gr24 = new GameRequest();
		gr24.setId(3);
		gr24.setSender(player2);
		gr24.setReceiver(player4);
		gr24.setStyle(GameStyle.OBSTRUCTIVE);


		gr34 = new GameRequest();
		gr34.setId(4);
		gr34.setSender(player3);
		gr34.setReceiver(player4);
		gr34.setStyle(GameStyle.CLASSIC);


		gameReqRepo.save(gr12);
		gameReqRepo.save(gr13);
		gameReqRepo.save(gr14);
		gameReqRepo.save(gr24);
		gameReqRepo.save(gr34);
	}
	
	@DisplayName("Test for findByReceiverUsernameOrderByStyleAscCreatedAtDesc")
	@Test
	public void givenReceiverUsername_whenfindByReceiverUsernameOrderByStyleAscCreatedAtDesc_thenGameRequestList() {

		List<GameRequest> gameRequestListDB1 = gameReqRepo.findByReceiverUsernameOrderByStyleAscCreatedAtDesc(player1.getUsername());
		assertThat(gameRequestListDB1).isEmpty();
		
		List<GameRequest> gameRequestListDB2 = gameReqRepo.findByReceiverUsernameOrderByStyleAscCreatedAtDesc(player4.getUsername());
		assertThat(gameRequestListDB2).isNotNull();
		assertThat(gameRequestListDB2.size()).isEqualTo(3);
		assertThat(gameRequestListDB2.get(0).getStyle()).isEqualTo(gr34.getStyle());
	}
	
	@DisplayName("Test for findBySenderUsernameOrderByStyleAscCreatedAtDesc")
	@Test
	public void givenReceiverUsername_whenfindSenderUsernameOrderByStyleAscCreatedAtDesc_thenGameRequestList() {

		List<GameRequest> gameRequestListDB1 = gameReqRepo.findBySenderUsernameOrderByStyleAscCreatedAtDesc(player4.getUsername());
		assertThat(gameRequestListDB1).isEmpty();
		
		List<GameRequest> gameRequestListDB2 = gameReqRepo.findBySenderUsernameOrderByStyleAscCreatedAtDesc(player1.getUsername());
		assertThat(gameRequestListDB2).isNotNull();
		assertThat(gameRequestListDB2.size()).isEqualTo(3);
		assertThat(gameRequestListDB2.get(0).getStyle()).isEqualTo(gr13.getStyle());
	}
	
	@DisplayName("Test for deleteById")
	@Test
	public void givenId_whenDeleteById_thenGameRequestDeleted() {
		
		List<GameRequest> gameRequestListDB1 = gameReqRepo.findBySenderUsernameOrderByStyleAscCreatedAtDesc(player1.getUsername());
		assertThat(gameRequestListDB1.size()).isEqualTo(3);
		
		gameReqRepo.deleteById(gr12.getId());
		
		List<GameRequest> gameRequestListDB2 = gameReqRepo.findBySenderUsernameOrderByStyleAscCreatedAtDesc(player1.getUsername());
		assertThat(gameRequestListDB2.size()).isEqualTo(2);
	}
	
	@DisplayName("Test for existsBySenderUsernameAndReceiverUsernameAndStyle")
	@Test
	public void givenSenderAndReceiverUsernamesAndGameStyle_whenExistsBySenderUsernameAndReceiverUsernameAndStyle_thenReturnBoolean() {
		
		Boolean existsDB1 = gameReqRepo.existsBySenderUsernameAndReceiverUsernameAndStyle(player1.getUsername(), player2.getUsername(), GameStyle.CLASSIC);
		assertThat(existsDB1).isEqualTo(true);
		Boolean existsDB2 = gameReqRepo.existsBySenderUsernameAndReceiverUsernameAndStyle(player1.getUsername(), player3.getUsername(), GameStyle.CLASSIC);
		assertThat(existsDB2).isEqualTo(false);
		Boolean existsDB3 = gameReqRepo.existsBySenderUsernameAndReceiverUsernameAndStyle(player1.getUsername(), player2.getUsername(), GameStyle.OBSTRUCTIVE);
		assertThat(existsDB3).isEqualTo(false);
		Boolean existsDB4 = gameReqRepo.existsBySenderUsernameAndReceiverUsernameAndStyle(player2.getUsername(), player1.getUsername(), GameStyle.CLASSIC);
		assertThat(existsDB4).isEqualTo(false);
	}
	
	@DisplayName("Test for findBySenderAndStyle")
	@Test
	public void givenSenderAndGameStyle_whenFindBySenderAndStyle_thenReturnGameRequestList() {
		
		List<GameRequest> gameRequestListDB1 = gameReqRepo.findBySenderAndStyle(player1, GameStyle.CLASSIC);
		assertThat(gameRequestListDB1.size()).isEqualTo(2);
		
		
		List<GameRequest> gameRequestListDB2 = gameReqRepo.findBySenderAndStyle(player1, GameStyle.OBSTRUCTIVE);
		assertThat(gameRequestListDB2.size()).isEqualTo(0);
		
		
		List<GameRequest> gameRequestListDB3 = gameReqRepo.findBySenderAndStyle(player1, GameStyle.AMBIGUOUS);
		assertThat(gameRequestListDB3.size()).isEqualTo(1);
	}

}
