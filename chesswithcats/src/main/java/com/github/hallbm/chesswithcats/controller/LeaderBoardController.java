package com.github.hallbm.chesswithcats.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.github.hallbm.chesswithcats.domain.GameEnums.GameStyle;
import com.github.hallbm.chesswithcats.service.GameServices;

/**
 * Controller related to display of top 5 users for each style of game
 */


@Controller
public class LeaderBoardController {

	@Autowired
	GameServices gameServ;
	
	@GetMapping("/leaderboard")
	public String showLeaders(Model model) {
		for (GameStyle gs : GameStyle.values()) {
			List<Object []> ranking = gameServ.getTopPlayers(gs);
			if (ranking == null) {
				continue;
			}
			model.addAttribute(gs.toString(), gameServ.getTopPlayers(gs));
		}
		return "leaderboard";
	}
}
