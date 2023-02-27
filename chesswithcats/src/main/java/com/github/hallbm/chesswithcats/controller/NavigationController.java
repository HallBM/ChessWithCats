package com.github.hallbm.chesswithcats.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NavigationController {

	@GetMapping("/about")
	public String getAboutSection(Model model) {
		return "mockup";
	}
	
	@GetMapping("/contact")
	public String getContactInfo(Model model) {
		return "underconstruction";
	}
	
	@GetMapping("/")
	public String getHomePage(Model model, Principal user) {
		
		if (user != null) {
			model.addAttribute("username", user.getName()); 
		}
		
		return "index";
	}
	

	
	@GetMapping("/leaderboard")
	public String showLeaders(Model model, Principal user) {
		
		if (user != null) {
			model.addAttribute("authenticated", true);
			model.addAttribute("username", user.getName()); 
		}
		
		return "leaderboard";
	}
	

	
	@GetMapping("/games")
	public String showGames(Model model, Principal user) {
		
		if (user != null) {
			model.addAttribute("authenticated", true);
			model.addAttribute("username", user.getName()); 
		}
		
		return "games";
	}
}
