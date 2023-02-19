package com.github.hallbm.catswithchess.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.hallbm.chesswithcats.dto.PlayerRegistrationDTO;
import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.PlayerRepository;
import com.github.hallbm.chesswithcats.service.PlayerServices;

import jakarta.validation.Valid;

@Controller
public class PlayerController {

	@Autowired
	private PlayerServices playerServ;
	
	@Autowired
	private PlayerRepository playerRepo;

	@GetMapping("/login")
	public String loginPlayer() {
		return "login";
	}
	
	@GetMapping("/successfulRegistration")
	public String loginAfterRegistration(Model model) {
		model.addAttribute("successReg", true);
		return "login";
	}
	
	@PostMapping("/validateLogin")
	public String validateLogin(Model model, Player player) {

		//TODO: Check login credentials and authorize 
		
		return "redirect:/";
	}
	
	@GetMapping("/profile")
	public String accessProfile(Model model) {
		return "profile";
	}
	
	//TODO post request to edit profile (not put)
	
	@GetMapping("/leaderboard")
	public String showLeaderboard(Model model) {
		//TODO: pull data from database for 3 models for top 5 players for each game type
		return "leaderboard";
	}
	
	@GetMapping("/register")
	public String playerRegistration(Model model) {
		model.addAttribute("playerReg", new PlayerRegistrationDTO());
		return "register";
	}
	
	@PostMapping("/registerPlayer")
    public String registerPlayer(@Valid @ModelAttribute("playerReg") PlayerRegistrationDTO playerReg, BindingResult result, Model model) {
   	
		String existingEmail = playerRepo.findEmail(playerReg.getEmail());
		String existingUsername = playerRepo.findUsername(playerReg.getUsername());

		if (existingEmail != null){
			result.rejectValue("email", null);
			return "register";
	    }
		
		if (existingUsername != null) {
			result.rejectValue("username", null);
			return "register";
		}
		
		if (!playerReg.getPassword().equals(playerReg.getConfirmedPassword())) {
			result.rejectValue("password", null);
			return "register";
		} 
		
	    if (result.hasErrors()){
	        return "register";
	    }
		
	    playerServ.registerPlayer(playerReg);
		
        return "redirect:/successfulRegistration";
	}

}
