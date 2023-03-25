package com.github.hallbm.chesswithcats.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.hallbm.chesswithcats.dto.LoginCredentialsDTO;
import com.github.hallbm.chesswithcats.dto.PlayerRegistrationDTO;
import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.PlayerRepository;
import com.github.hallbm.chesswithcats.service.PlayerServices;

import jakarta.validation.Valid;

/**
 * Controller related to player login, registration and logout.
 */
@Controller
public class UserController {

	@Autowired
	private PlayerServices playerServ;

	@Autowired
	private PlayerRepository playerRepo;

	@GetMapping("/login")
	public String loginPlayer(Model model, @AuthenticationPrincipal Player currentUser) {
		if (currentUser != null) {
			return "redirect:/";
		}

		model.addAttribute("userLogin", new LoginCredentialsDTO());
		return "login";
	}

	@GetMapping("/login-success")
	public String successfulLogin(@AuthenticationPrincipal Player currentUser) {
		currentUser.setOnline(true);
		currentUser.setLastLogin(LocalDateTime.now());
		playerRepo.save(currentUser);
		return "redirect:/";
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

	@GetMapping("/register")
	public String playerRegistration(Model model) {
		model.addAttribute("playerReg", new PlayerRegistrationDTO());
		return "register";
	}

	@ResponseBody
	@PostMapping("/username")
	public ResponseEntity<Boolean> getUserByUserName(@RequestBody Map<String, String> userInput) {
		Boolean isTaken = playerRepo.existsByUsername(userInput.get("userInput"));
		return new ResponseEntity<Boolean>(isTaken, HttpStatus.OK);
	}

	@PostMapping("/register")
    public String registerPlayer(@Valid @ModelAttribute("playerReg") PlayerRegistrationDTO playerReg, BindingResult result, Model model) {

		Player playerWithEmail = playerRepo.findByEmail(playerReg.getEmail());
		Player playerWithUsername = playerRepo.findByUsername(playerReg.getUsername());

		if (playerWithEmail != null){
			result.rejectValue("email", null);
			return "register";
	    }

		if (playerWithUsername != null) {
			result.rejectValue("username", null);
			return "register";
		}
		
		if (!playerReg.getPassword().equals(playerReg.getConfirmedPassword())) {
			result.rejectValue("password", null);
			return "register";
		}

		if (playerReg.getIconFile() == null) {
			result.rejectValue("iconFile", null);
			return "register";
		}
		
	    if (result.hasErrors()){
	        return "register";
	    }

	    playerServ.registerPlayer(playerReg);

        return "redirect:/registration-success";
	}

	@GetMapping("/logout")
	public String showLogout(Model model) {
		return "logout";
	}

}
