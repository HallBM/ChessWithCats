package com.github.hallbm.chesswithcats.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.github.hallbm.chesswithcats.model.Player;

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
	public String getHomePage(Model model, @AuthenticationPrincipal Player currentUser) {

		if (currentUser != null) {
			model.addAttribute("username", currentUser.getUsername());
		}

		return "index";
	}

	@GetMapping("/error")
	public String getErrorPage() {

		return "error";
	}
}
