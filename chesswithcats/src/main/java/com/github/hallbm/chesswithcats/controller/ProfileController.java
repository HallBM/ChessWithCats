package com.github.hallbm.chesswithcats.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;
import com.github.hallbm.chesswithcats.dto.PlayerRegistrationDTO;
import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.PlayerRepository;
import com.github.hallbm.chesswithcats.service.GameServices;
import com.github.hallbm.chesswithcats.service.PlayerServices;

import jakarta.transaction.Transactional;

@RestController
@Component("ProfilePageController")
public class ProfileController {

	@Autowired
	PlayerRepository playerRepo;
	
	@Autowired
	PlayerServices playerServ;
	
	@Autowired
	GameServices gameServ;
	
	@GetMapping("/profile")
	public ModelAndView accessProfile(Model model, @AuthenticationPrincipal Player currentUser) {
		
		String fullname = currentUser.getFirstName() + " " + currentUser.getLastName();
		int calcAcctDays = (int) ChronoUnit.DAYS.between(currentUser.getDateJoined(), LocalDate.now());
		String dayDisplay = String.format(" (%d %s)", calcAcctDays, calcAcctDays == 1 ? "day" : "days");

		model.addAttribute("iconFile", currentUser.getIconFile());
		model.addAttribute("username", currentUser.getUsername());
		model.addAttribute("email", currentUser.getEmail());
		model.addAttribute("password", "************");
		model.addAttribute("name", fullname);
		model.addAttribute("dateJoined", currentUser.getDateJoined());
		model.addAttribute("accountDays", dayDisplay);
		model.addAttribute("isEditable", true);
		
		Map<GameStyle,Integer[]> dbResults = gameServ.getWinDrawLosePercentageByPlayer(currentUser.getUsername());
		model.addAttribute("gameResults", dbResults);
		
		return new ModelAndView("profile", model.asMap());
	}


	@GetMapping("/profile/{username}")
	public ModelAndView accessOtherProfile(Model model, @PathVariable String username, @AuthenticationPrincipal Player currentUser) {

		if (username.equals(currentUser.getUsername())) {
			return new ModelAndView("redirect:/profile");
		}

		Player other = playerRepo.findByUsername(username);
		int calcAcctDays = (int) ChronoUnit.DAYS.between(other.getDateJoined(), LocalDate.now());
		String dayDisplay = String.format(" (%d %s)", calcAcctDays, calcAcctDays == 1 ? "day" : "days");

		model.addAttribute("iconFile", other.getIconFile());
		model.addAttribute("username", other.getUsername());
		model.addAttribute("dateJoined", other.getDateJoined());
		model.addAttribute("accountDays", dayDisplay);
		model.addAttribute("isEditable", false);
		model.addAttribute("accountStatus", other.isActive()); 

		Map<GameStyle,Integer[]> dbResults = gameServ.getWinDrawLosePercentageByPlayer(username);
		model.addAttribute("gameResults", dbResults);
		
		Map<GameStyle,Integer[]> matchResults = gameServ.getWinDrawLosePercentageByOpponents(currentUser.getUsername(), username);
		model.addAttribute("matchResults", matchResults);

		model.addAttribute("online", other.isLogged()); 
		
		return new ModelAndView("profile");
	}
	
	@Modifying
	@Transactional
	@PostMapping("/delete-account")
	public ModelAndView deleteAccount(Model model, @AuthenticationPrincipal Player currentUser) {
		currentUser.setActive(false);
		playerRepo.save(currentUser);
		return new ModelAndView("redirect:/logout");
	}
	
	
}
