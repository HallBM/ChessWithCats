package com.github.hallbm.catswithchess.controller;

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
		return "index";
	}	
}
