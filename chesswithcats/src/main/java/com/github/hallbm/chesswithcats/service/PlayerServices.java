package com.github.hallbm.chesswithcats.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.hallbm.chesswithcats.dto.PlayerRegistrationDTO;
import com.github.hallbm.chesswithcats.model.Authority;
import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.AuthorityRepository;
import com.github.hallbm.chesswithcats.repository.PlayerRepository;

import jakarta.transaction.Transactional;

/**
 * Services for new player registration with security features.
 */

@Service
public class PlayerServices implements UserDetailsService{

	@Autowired
	private PlayerRepository playerRepo;
	
	@Autowired
	private AuthorityRepository authRepo;
	
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
		player.setAuthorities(Collections.singletonList(authRepo.findByAuthority("ROLE_USER").orElse(createUserRole("ROLE_USER"))));

		return playerRepo.save(player);
	}
	
	@Transactional
	private Authority createUserRole(String role) {
		Authority auth = new Authority();
		auth.setAuthority(role);
		
		return auth;
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
