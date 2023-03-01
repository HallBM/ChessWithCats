package com.github.hallbm.chesswithcats.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

import com.github.hallbm.chesswithcats.model.Player;
import com.github.hallbm.chesswithcats.repository.PlayerRepository;

@Component
public class LogoutListener implements ApplicationListener<LogoutSuccessEvent> {
    
    @Autowired
    private PlayerRepository playerRepo;
    
    @Override
    public void onApplicationEvent(LogoutSuccessEvent event) {
        String username = event.getAuthentication().getName();
        Player player = playerRepo.findByUsername(username);
        player.setLogged(false);
        player.setPlaying(false);
        playerRepo.save(player);
    }
}
