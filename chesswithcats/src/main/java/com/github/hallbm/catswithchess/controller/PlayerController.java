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
   	
		Player existingEmail = playerRepo.findByEmail(playerReg.getEmail());
		Player existingUsername = playerRepo.findByUsername(playerReg.getUsername());

		if (existingEmail != null){
	        //TODO suggest login instead   
			result.rejectValue("email", null, "There is already an account registered with that email");
	    }
		
		if (existingUsername != null) {
			result.rejectValue("username", playerReg.getUsername(), "Username already exists");
		}
		
		if (!playerReg.getPassword().equals(playerReg.getConfirmedPassword())) {
			//TODO currently handled by JS internal script
			result.rejectValue("password", null, "Passwords do not match");
			result.rejectValue("password-confirm", null, "Passwords do not match");
		} 
			
		if (result.hasErrors()){
	        model.addAttribute("playerReg", playerReg);
			return "register";
	    }
		
		System.out.println(playerReg.getUsername());
		
		playerServ.registerPlayer(playerReg);
		
		//TODO display successful registration
        return "redirect:/login";
	}

}
