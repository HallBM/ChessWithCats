package com.github.hallbm.chesswithcats.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.hallbm.chesswithcats.DTO.PlayerRegistrationDTO;
import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.PlayerRepository;

@Service
public class PlayerServices{

	@Autowired
	private PlayerRepository playerRepo;

	public Player registerPlayer(PlayerRegistrationDTO playerReg) {
		Player player = new Player();
		// PlayerId is auto generated, DateJoined is auto generated
		player.setUsername(playerReg.getUsername());
		player.setPassword(playerReg.getPassword());
		player.setFirstName(playerReg.getFirstName());
		player.setLastName(playerReg.getLastName());
		player.setIconFile(playerReg.getIconFile());
		player.setEmail(playerReg.getEmail());
		player.setLoggedIn(false);
		player.setPlaying(false);
		player.setDateJoined(new Date());
		return playerRepo.save(player);
	}
	


}
