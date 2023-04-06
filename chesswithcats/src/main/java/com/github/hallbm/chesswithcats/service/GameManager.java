package com.github.hallbm.chesswithcats.service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.hallbm.chesswithcats.model.Game;
import com.github.hallbm.chesswithcats.repository.GameRepository;
import com.github.hallbm.chesswithcats.repository.PlayerRepository;

import jakarta.transaction.Transactional;

@Service
public class GameManager {
	
	@Autowired
	private GameRepository gameRepo;
	
	@Autowired
	private PlayerRepository playerRepo;
	
	
	private final static Set<String> onlinePlayers = ConcurrentHashMap.newKeySet();
	private final static ConcurrentHashMap<Long, Game> activeGames = new ConcurrentHashMap<>();
	private final static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	
	public void addPlayer(String username) {
		onlinePlayers.add(username);
	}
	
	public void removePlayer(String username) {
		onlinePlayers.remove(username);
	}

	public void start() {
        scheduler.scheduleAtFixedRate(this::removeInactiveGames, 0, 1, TimeUnit.MINUTES);
    }
	
    public void stop() {
        scheduler.shutdown();
    }
    
    private void removeInactiveGames() {
        for (Map.Entry<Long, Game> entry : activeGames.entrySet()) {
            Game game = entry.getValue();
            if (!onlinePlayers.contains(game.getWhite().getUsername()) && !onlinePlayers.contains(game.getBlack().getUsername())) {
                gameRepo.save(game);
                System.out.println("game removed: " + game.getId());
            	activeGames.remove(entry.getKey());
            }
        }
    }

    @Transactional
    public Game getGame(Long gameId) {
	    Game game = activeGames.get(gameId);
	    if (game == null) {
    		game = gameRepo.findById(gameId).get();
    		activeGames.put(gameId, game);
	    }
	    
	    return game;
	}
    
    public void removeGame(Long gameId) {
    	activeGames.remove(gameId);
	}
    
    public void addGame(Game game) {
  		activeGames.put(game.getId(), game);
	}
    
}
