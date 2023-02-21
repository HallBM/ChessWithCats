package com.github.hallbm.chesswithcats.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.hallbm.chesswithcats.dto.LoginCredentialsDTO;
import com.github.hallbm.chesswithcats.dto.PlayerRegistrationDTO;
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
	public String loginPlayer(Model model) {
		model.addAttribute("userLogin", new LoginCredentialsDTO());
		return "login";
	}

	@GetMapping("/login-fail")
	public String failedLogin(Model model) {
		model.addAttribute("userLogin", new LoginCredentialsDTO());
		model.addAttribute("loginFail", true);
		return "login";
	}
	
	@GetMapping("/registration-success")
	public String loginAfterRegistration(Model model) {
		model.addAttribute("successReg", true);
		model.addAttribute("userLogin", new LoginCredentialsDTO());
		return "login";
	}
	
	@PostMapping("/login-player")
	public LoginCredentialsDTO validateLogin(@ModelAttribute("userLogin") LoginCredentialsDTO loginCred) {
		return loginCred;
	}

	//TODO only 1 log in per username
	//TODO update home page with changes after user login (username displayed, login disappears, switching users, logout option appears, etc). 
	
	@GetMapping("/profile")
	public String accessProfile(Model model) {
		return "profile";
	}
	
	//TODO patch request to edit profile 
	
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
	
	@PostMapping("/register")
    public String registerPlayer(@Valid @ModelAttribute("playerReg") PlayerRegistrationDTO playerReg, BindingResult result, Model model) {
   	
		String existingEmail = playerRepo.findEmail(playerReg.getEmail()).orElse(null);
		String existingUsername = playerRepo.findUsername(playerReg.getUsername()).orElse(null);

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
		
        return "redirect:/registration-success";
	}
	
	@GetMapping("/logout")
	public String logoutPlayer(Model model) {
		return "logout";
	}
	

}
