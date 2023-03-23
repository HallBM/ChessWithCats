package com.github.hallbm.chesswithcats.controller;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
public class MoveUpdateSSEController {

	private final static long timeout_ms = 5 * 60 * 1000; // 5 min
	ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

	@GetMapping("/subscribe/{gameId}/{playerColor}")
	public SseEmitter subscribe(@PathVariable String gameId, @PathVariable String playerColor) {
		SseEmitter sseEmitter = new SseEmitter(timeout_ms);
		try {
			sseEmitter.send(SseEmitter.event().name("INIT"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		emitters.put(gameId + playerColor, sseEmitter);
		return sseEmitter;
	}

	public void sendMove(String move, String gameId, String playerColor) {		
		
		if (emitters.containsKey(gameId + playerColor)) {
			try {
				emitters.get(gameId + playerColor).send(SseEmitter.event().name("move").data(move));
			} catch (IOException e) {
				emitters.remove(gameId + playerColor);
			}
		}

	}

}
