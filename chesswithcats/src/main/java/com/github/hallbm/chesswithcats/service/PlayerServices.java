package com.github.hallbm.chesswithcats.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.hallbm.chesswithcats.dto.PlayerRegistrationDTO;
import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.PlayerRepository;

/**
 * Services for new player registration with security features.
 */

@Service
public class PlayerServices implements UserDetailsService{

	@Autowired
	private PlayerRepository playerRepo;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	public Player registerPlayer(PlayerRegistrationDTO playerReg) {
		Player player = new Player();
		player.setUsername(playerReg.getUsername());
		player.setPassword(passwordEncoder.encode(playerReg.getPassword()));
		player.setFirstName(playerReg.getFirstName());
		player.setLastName(playerReg.getLastName());
		player.setIconFile(playerReg.getIconFile());
		player.setEmail(playerReg.getEmail());

		return playerRepo.save(player);
	}
	
    @Override
    public Player loadUserByUsername(String username) throws UsernameNotFoundException {
        Player player = playerRepo.findByUsername(username);
        if (player == null) {
            throw new UsernameNotFoundException("user not found with the corresponding email");
        }
        return player;	
    }

}
