package com.github.hallbm.chesswithcats.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

import com.github.hallbm.chesswithcats.repository.PlayerRepository;

/**
 * Player logout event updates player 'isLogged' status to indicate to other players that
 * the user is no longer online 
 */

@Component
public class LogoutListener implements ApplicationListener<LogoutSuccessEvent> {
    
    @Autowired
    private PlayerRepository playerRepo;
    
    @Override
    public void onApplicationEvent(LogoutSuccessEvent event) {
    	playerRepo.logoutPlayerByUsername(event.getAuthentication().getName());
    }
    
}
