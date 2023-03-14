package com.github.hallbm.chesswithcats.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import com.github.hallbm.chesswithcats.domain.FriendEnums.BlockedBy;
import com.github.hallbm.chesswithcats.domain.FriendEnums.FriendRequestStatus;
import com.github.hallbm.chesswithcats.model.FriendRequest;
import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.FriendRequestRepository;

@SpringJUnitConfig
@SpringBootTest
public class FriendServicesTest {

	@Mock
	private FriendRequestRepository friendReqRepo;

	@InjectMocks
	private FriendServices friendServ;

	private Player player1, player2, player3, player4;
	private FriendRequest fr12, fr13, fr14, fr34, fr24;

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

		fr12 = new FriendRequest();
		fr12.setId(0);
		fr12.setSender(player1);
		fr12.setReceiver(player2);
		fr12.setRequestDate(LocalDate.now());
		fr12.setStatus(FriendRequestStatus.PENDING);

		fr13 = new FriendRequest();
		fr13.setId(0);
		fr13.setSender(player1);
		fr13.setReceiver(player3);
		fr13.setRequestDate(LocalDate.now());
		fr13.setStatus(FriendRequestStatus.ACCEPTED);

		fr14 = new FriendRequest();
		fr14.setId(0);
		fr14.setSender(player1);
		fr14.setReceiver(player4);
		fr14.setRequestDate(LocalDate.now());
		fr14.setStatus(FriendRequestStatus.BLOCKED);
		fr14.setBlockedBy(BlockedBy.SENDER);

		fr24 = new FriendRequest();
		fr24.setId(0);
		fr24.setSender(player2);
		fr24.setReceiver(player4);
		fr24.setRequestDate(LocalDate.now());
		fr24.setStatus(FriendRequestStatus.PENDING);

		fr34 = new FriendRequest();
		fr34.setId(0);
		fr34.setSender(player3);
		fr34.setReceiver(player4);
		fr34.setRequestDate(LocalDate.now());
		fr34.setStatus(FriendRequestStatus.ACCEPTED);
	}

	@Test
	@Transactional
	public void testGetReceivedFriendRequestUsernames1() {

		List<FriendRequest> requests1 = new ArrayList<>();
		Set<String> expectedUsernames1 = new TreeSet<>();

		when(friendReqRepo.findByReceiverUsernameAndStatus(player1.getUsername(), FriendRequestStatus.PENDING))
				.thenReturn(requests1);
		Set<String> actualUsernames1 = friendServ.getReceivedFriendRequestUsernames(player1.getUsername());
		assertEquals(expectedUsernames1, actualUsernames1);
	}
	
	@Test
	@Transactional
	public void testGetReceivedFriendRequestUsernames2() {
	
		List<FriendRequest> requests2 = new ArrayList<>();
		requests2.add(fr12);
		Set<String> expectedUsernames2 = new TreeSet<>();
		expectedUsernames2.add(player1.getUsername());

		when(friendReqRepo.findByReceiverUsernameAndStatus(player2.getUsername(), FriendRequestStatus.PENDING))
				.thenReturn(requests2);
		Set<String> actualUsernames2 = friendServ.getReceivedFriendRequestUsernames(player2.getUsername());
		assertEquals(expectedUsernames2, actualUsernames2);
	}
	
	@Test
	@Transactional
	public void testGetReceivedFriendRequestUsernames3() {
	
		List<FriendRequest> requests3= new ArrayList<>();
		Set<String> expectedUsernames3 = new TreeSet<>();

		when(friendReqRepo.findByReceiverUsernameAndStatus(player3.getUsername(), FriendRequestStatus.PENDING))
				.thenReturn(requests3);
		Set<String> actualUsernames3 = friendServ.getReceivedFriendRequestUsernames(player3.getUsername());
		assertEquals(expectedUsernames3, actualUsernames3);
	}
	
	@Test
	@Transactional
	public void testGetReceivedFriendRequestUsernames4() {
	
		List<FriendRequest> requests4 = new ArrayList<>();
		requests4.add(fr24);
		Set<String> expectedUsernames4 = new TreeSet<>();
		expectedUsernames4.add(player2.getUsername());

		when(friendReqRepo.findByReceiverUsernameAndStatus(player4.getUsername(), FriendRequestStatus.PENDING))
				.thenReturn(requests4);
		Set<String> actualUsernames4 = friendServ.getReceivedFriendRequestUsernames(player4.getUsername());
		assertEquals(expectedUsernames4, actualUsernames4);
	}
}
