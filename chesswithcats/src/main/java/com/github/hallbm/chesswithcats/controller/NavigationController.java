package com.github.hallbm.chesswithcats.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.github.hallbm.chesswithcats.domain.FriendEnums.FriendRequestStatus;
import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.FriendRequestRepository;
import com.github.hallbm.chesswithcats.repository.GameRequestRepository;

/**
 * Controller related to general navigation through website that act as simple endpoints
 */

@Controller
public class NavigationController {

	@Autowired
	GameRequestRepository gameReqRepo;
	
	@Autowired
	FriendRequestRepository friendReqRepo;
	
	@GetMapping("/about")
	public String getAboutSection(Model model) {
		return "redirect:/error";
	}

	@GetMapping("/contact")
	public String getContactInfo(Model model) {
		return "redirect:/error";
	}

	@GetMapping("/")
	public String getHomePage(Model model, @AuthenticationPrincipal Player currentUser) {
		if (currentUser != null) {
			model.addAttribute("username", currentUser.getUsername());
			model.addAttribute("hasFriendRequests", friendReqRepo.existsByReceiverUsernameAndStatus(currentUser.getUsername(), FriendRequestStatus.PENDING));
			model.addAttribute("hasGameRequests", gameReqRepo.existsByReceiverUsername(currentUser.getUsername()));
		}
		
		return "index";
	}

	@GetMapping("/error")
	public String getErrorPage() {
		return "error";
	}
}
