package com.github.hallbm.chesswithcats.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.github.hallbm.chesswithcats.model.Player;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class PlayerRepositoryTest {
	@Autowired
	private PlayerRepository playerRepo;

	private Player player1, player2, player3, player4;

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

		player1.setLastLogin(LocalDateTime.now());
		player2.setLastLogin(LocalDateTime.now());
		
		playerRepo.save(player1);
		playerRepo.save(player2);
		playerRepo.save(player3);
		playerRepo.save(player4);
	}
	
	@DisplayName("Test for findByUsername")
	@Test
	public void givenUsername_whenFindByUsername_thenReturnPlayer() {

		Player playerDB1 = playerRepo.findByUsername(player1.getUsername());
		assertThat(playerDB1).isNotNull();
		assertThat(playerDB1.getUsername()).isEqualTo(player1.getUsername());
		assertThat(playerDB1.getUsername()).isNotEqualTo(player2.getUsername());
	}
	
	@DisplayName("Test for findByEmail")
	@Test
	public void givenEmail_whenFindByEmail_thenReturnPlayer() {

		Player playerDB1 = playerRepo.findByEmail(player1.getEmail());
		assertThat(playerDB1).isNotNull();
		assertThat(playerDB1.getUsername()).isEqualTo(player1.getUsername());
		assertThat(playerDB1.getUsername()).isNotEqualTo(player2.getUsername());
	}
	
	@DisplayName("Test for existsByUsername")
	@Test
	public void givenUsername_whenExistsByUsername_thenReturnBoolean() {

		Boolean existsDB1 = playerRepo.existsByUsername("abc");
		assertThat(existsDB1).isEqualTo(false);

		Boolean existsDB2 = playerRepo.existsByUsername("player3_username");
		assertThat(existsDB2).isEqualTo(true);	
	}
	
	
	

}
