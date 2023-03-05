package com.github.hallbm.chesswithcats.controller;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.web.exchanges.HttpExchange.Principal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.PlayerRepository;

@RestController
@Component("ProfilePageController")
public class ProfileController {

	@Autowired
	PlayerRepository playerRepo;

	@GetMapping("/profile")
	public ModelAndView accessProfile(Model model, Principal user) {
		Player player = playerRepo.findByUsername(user.getName());
		String fullname = player.getFirstName() + " " + player.getLastName();
		int calcAcctDays = (int) Duration.between(player.getDateJoined(), LocalDate.now()).toDays();
		String dayDisplay = String.format(" (%d %s)", calcAcctDays, calcAcctDays == 1 ? "day" : "days");

		model.addAttribute("iconFile", player.getIconFile());
		model.addAttribute("username", player.getUsername());
		model.addAttribute("email", player.getEmail());
		model.addAttribute("password", "************");
		model.addAttribute("name", fullname);
		model.addAttribute("dateJoined", player.getDateJoined());
		model.addAttribute("accountDays", dayDisplay);
		model.addAttribute("isEditable", true);

		//TODO games, scores, etc

		return new ModelAndView("profile", model.asMap());
	}


	@GetMapping("/profile/{username}")
	public ModelAndView accessOtherProfile(Model model, @PathVariable String username, @AuthenticationPrincipal Player currentUser) {

		if (username.equals(currentUser.getUsername())) {
			return new ModelAndView("redirect:/profile");
		}

		Player other = playerRepo.findByUsername(username);
		int calcAcctDays = (int) Duration.between(other.getDateJoined(), LocalDate.now()).toDays();
		String dayDisplay = String.format(" (%d %s)", calcAcctDays, calcAcctDays == 1 ? "day" : "days");

		model.addAttribute("iconFile", other.getIconFile());
		model.addAttribute("username", other.getUsername());
		model.addAttribute("dateJoined", other.getDateJoined());
		model.addAttribute("accountDays", dayDisplay);
		model.addAttribute("isEditable", false);

		//TODO games, scores, etc

		return new ModelAndView("profile");
	}

	//TODO PROFILE EDITS
}
