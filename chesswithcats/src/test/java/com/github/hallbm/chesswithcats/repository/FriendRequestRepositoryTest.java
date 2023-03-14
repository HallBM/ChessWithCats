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

import com.github.hallbm.chesswithcats.domain.FriendEnums.BlockedBy;
import com.github.hallbm.chesswithcats.domain.FriendEnums.FriendRequestStatus;
import com.github.hallbm.chesswithcats.model.FriendRequest;
import com.github.hallbm.chesswithcats.model.Player;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class FriendRequestRepositoryTest {

	@Autowired
	private FriendRequestRepository friendReqRepo;

	@Autowired
	private PlayerRepository playerRepo;

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

		playerRepo.save(player1);
		playerRepo.save(player2);
		playerRepo.save(player3);
		playerRepo.save(player4);

		fr12 = new FriendRequest();
		fr12.setId(0);
		fr12.setSender(player1);
		fr12.setReceiver(player2);
		fr12.setRequestDate(LocalDate.now());
		fr12.setStatus(FriendRequestStatus.PENDING);

		fr13 = new FriendRequest();
		fr13.setId(1);
		fr13.setSender(player1);
		fr13.setReceiver(player3);
		fr13.setRequestDate(LocalDate.now());
		fr13.setStatus(FriendRequestStatus.ACCEPTED);

		fr14 = new FriendRequest();
		fr14.setId(2);
		fr14.setSender(player1);
		fr14.setReceiver(player4);
		fr14.setRequestDate(LocalDate.now());
		fr14.setStatus(FriendRequestStatus.BLOCKED);
		fr14.setBlockedBy(BlockedBy.SENDER);

		fr24 = new FriendRequest();
		fr24.setId(3);
		fr24.setSender(player2);
		fr24.setReceiver(player4);
		fr24.setRequestDate(LocalDate.now());
		fr24.setStatus(FriendRequestStatus.PENDING);

		fr34 = new FriendRequest();
		fr34.setId(4);
		fr34.setSender(player3);
		fr34.setReceiver(player4);
		fr34.setRequestDate(LocalDate.now());
		fr34.setStatus(FriendRequestStatus.ACCEPTED);

		friendReqRepo.save(fr12);
		friendReqRepo.save(fr13);
		friendReqRepo.save(fr14);
		friendReqRepo.save(fr24);
		friendReqRepo.save(fr34);
	}

	@DisplayName("Test for getByUsername")
	@Test
	public void givenReceiverUsername_whenFindByReceiverUsernameOrSenderUsername_thenReturnFriendRequestList() {

		List<FriendRequest> friendRequestListDB1 = friendReqRepo.getByUsername(player1.getUsername());
		assertThat(friendRequestListDB1).isNotNull();
		assertThat(friendRequestListDB1.size()).isEqualTo(3);

		List<FriendRequest> friendRequestListDB2 = friendReqRepo.getByUsername(player2.getUsername());
		assertThat(friendRequestListDB2).isNotNull();
		assertThat(friendRequestListDB2.size()).isEqualTo(2);
		assertThat(friendRequestListDB2.get(0).getSender()).isEqualTo(player1);

		List<FriendRequest> friendRequestListDB3 = friendReqRepo.getByUsername(player3.getUsername());
		assertThat(friendRequestListDB3).isNotNull();
		assertThat(friendRequestListDB3.size()).isEqualTo(2);
		assertThat(friendRequestListDB3.get(0).getSender()).isEqualTo(player1);

		List<FriendRequest> friendRequestListDB4 = friendReqRepo.getByUsername(player4.getUsername());
		assertThat(friendRequestListDB4).isNotNull();
		assertThat(friendRequestListDB4.size()).isEqualTo(3);
	}

	@DisplayName("Test for findByReceiverUsernameAndSenderUsername")
	@Test
	public void givenRecieverAndSenderUsername_whenFindByReceiverUsernameAndSenderUsername_thenReturnFriendRequest() {

		FriendRequest friendRequestListDB1 = friendReqRepo
				.findByReceiverUsernameAndSenderUsername(player1.getUsername(), player2.getUsername());
		assertThat(friendRequestListDB1).isNull();

		FriendRequest friendRequestDB2 = friendReqRepo.findByReceiverUsernameAndSenderUsername(player2.getUsername(),
				player1.getUsername());
		assertThat(friendRequestDB2).isNotNull();
		assertThat(friendRequestDB2.getSender()).isEqualTo(player1);
	}

	@DisplayName("Test for findBySenderUsernameAndStatus")
	@Test
	public void givenSenderUsername_whenFindBySenderUsernameAndStatus_thenReturnFriendRequestList() {

		List<FriendRequest> friendRequestListDB1 = friendReqRepo.findBySenderUsernameAndStatus(player1.getUsername(),
				FriendRequestStatus.ACCEPTED);
		assertThat(friendRequestListDB1).isNotNull();
		assertThat(friendRequestListDB1.size()).isEqualTo(1);
		assertThat(friendRequestListDB1.get(0).getReceiver()).isEqualTo(player3);

		List<FriendRequest> friendRequestListDB2 = friendReqRepo.findBySenderUsernameAndStatus(player1.getUsername(),
				FriendRequestStatus.BLOCKED);
		assertThat(friendRequestListDB2).isNotNull();
		assertThat(friendRequestListDB2.size()).isEqualTo(1);
		assertThat(friendRequestListDB2.get(0).getReceiver()).isEqualTo(player4);
	}

	@DisplayName("Test for findByReceiverUsernameAndStatus")
	@Test
	public void givenReceiverUsername_whenFindByReceiverUsernameAndStatus_thenReturnFriendRequestList() {

		List<FriendRequest> friendRequestListDB1 = friendReqRepo.findByReceiverUsernameAndStatus(player4.getUsername(),
				FriendRequestStatus.ACCEPTED);
		assertThat(friendRequestListDB1).isNotNull();
		assertThat(friendRequestListDB1.size()).isEqualTo(1);
		assertThat(friendRequestListDB1.get(0).getSender()).isEqualTo(player3);

		List<FriendRequest> friendRequestListDB2 = friendReqRepo.findByReceiverUsernameAndStatus(player4.getUsername(),
				FriendRequestStatus.BLOCKED);
		assertThat(friendRequestListDB2).isNotNull();
		assertThat(friendRequestListDB2.size()).isEqualTo(1);
		assertThat(friendRequestListDB2.get(0).getSender()).isEqualTo(player1);
	}

	@DisplayName("Test for getByUsernameAndStatus")
	@Test
	public void givenUsername_whenGetByUsernameAndStatus_thenReturnFriendRequestList() {

		List<FriendRequest> friendRequestListDB1 = friendReqRepo.getByUsernameAndStatus(player4.getUsername(),
				FriendRequestStatus.ACCEPTED.toString());
		assertThat(friendRequestListDB1).isNotNull();
		assertThat(friendRequestListDB1.size()).isEqualTo(1);
		assertThat(friendRequestListDB1.get(0).getSender()).isEqualTo(player3);

		List<FriendRequest> friendRequestListDB2 = friendReqRepo.getByUsernameAndStatus(player1.getUsername(),
				FriendRequestStatus.BLOCKED.toString());
		assertThat(friendRequestListDB2).isNotNull();
		assertThat(friendRequestListDB2.size()).isEqualTo(1);
		assertThat(friendRequestListDB2.get(0).getReceiver()).isEqualTo(player4);
	}

	@DisplayName("Test for getBlockedByUsername")
	@Test
	public void givenUsername_whenGetBlockedByUsername_thenReturnFriendRequestList() {

		List<FriendRequest> friendRequestListDB1 = friendReqRepo.getBlockedByUsername(player1.getUsername());
		assertThat(friendRequestListDB1).isNotNull();
		assertThat(friendRequestListDB1.size()).isEqualTo(1);
		assertThat(friendRequestListDB1.get(0).getReceiver()).isEqualTo(player4);

		List<FriendRequest> friendRequestListDB2 = friendReqRepo.getBlockedByUsername(player4.getUsername());
		assertThat(friendRequestListDB2).isEmpty();
	}

	@DisplayName("Test for getFriendshipByUsernames")
	@Test
	public void givenUsername_whenGetFriendshipByUsernames_thenReturnFriendRequest() {

		FriendRequest friendRequestDB1 = friendReqRepo.getFriendshipByUsernames(player1.getUsername(),
				player3.getUsername());
		assertThat(friendRequestDB1).isNotNull();
		assertThat(friendRequestDB1.getReceiver()).isEqualTo(player3);

		FriendRequest friendRequestDB2 = friendReqRepo.getFriendshipByUsernames(player3.getUsername(),
				player1.getUsername());
		assertThat(friendRequestDB2).isNotNull();
		assertThat(friendRequestDB2.getReceiver()).isEqualTo(player3);
	}

	@DisplayName("Test for deleteByReceiverUsernameAndSenderUsername")
	@Test
	public void givenReceiverandSenderUsernames_whendeleteByReceiverUsernameAndSenderUsername_thenReturnLong() {

		int isDeletedDB1 = friendReqRepo.deleteByUsernames(player1.getUsername(), player3.getUsername());
		assertThat(isDeletedDB1).isEqualTo(1);

		int isDeletedDB2 = friendReqRepo.deleteByUsernames(player1.getUsername(), player3.getUsername());
		assertThat(isDeletedDB2).isEqualTo(0);

	}
}
