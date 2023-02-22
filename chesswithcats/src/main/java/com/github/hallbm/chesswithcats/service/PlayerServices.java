package com.github.hallbm.chesswithcats.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.hallbm.chesswithcats.dto.PlayerRegistrationDTO;
import com.github.hallbm.chesswithcats.enums.GameStyles;
import com.github.hallbm.chesswithcats.model.Authority;
import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.AuthorityRepository;
import com.github.hallbm.chesswithcats.repository.PlayerRepository;

import jakarta.transaction.Transactional;

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

		Map<String, Integer> points = new HashMap<> ();
		for(GameStyles gs : GameStyles.values()) {
			points.put(gs.toString(), 500);
		}
		
		player.setPoints(points);
		
		return playerRepo.save(player);
	}
	
	@Transactional
	private Authority createUserRole(String role) {
		Authority auth = new Authority();
		auth.setAuthority(role);
		
		return auth;
	}

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails player = playerRepo.findByUsername(username);
        if (player == null) {
            throw new UsernameNotFoundException("user not found with the corresponding email");
        }
        return new User(player.getUsername(), player.getPassword(), player.getAuthorities().stream()
        				.map((authorities) -> new SimpleGrantedAuthority(authorities.getAuthority())).collect(Collectors.toList()));
	
    }

}
