package com.github.hallbm.chesswithcats.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NavigationController {

	@GetMapping("/about")
	public String getAboutSection(Model model) {
		return "underconstruction";
	}
	
	@GetMapping("/contact")
	public String getContactInfo(Model model) {
		return "underconstruction";
	}
	
	@GetMapping("/")
	public String getHomePage(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			    
		if (authentication != null && authentication.isAuthenticated()) {
		    model.addAttribute("authenticated", true);
		    model.addAttribute("username", authentication.getName());
		}
		return "index";
	}	
}
