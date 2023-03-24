package com.github.hallbm.chesswithcats.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.github.hallbm.chesswithcats.service.PlayerServices;

/**
 * Spring security configuration for login/logout, access permissions, and password encryption.
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Autowired
	public PlayerServices playerServ;
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests()
	    	.requestMatchers("/profile/**", 
	    				"/game/**", 
	    				"/games/**", 
						"/gameRequest/**", 
	    				 "/friends/**", 
	    				 "/friendrequest/**",
	    				 "/pendingrequest/**",
	    				 "/block/**",
	    				 "/unblock/**",
	    				 "/delete-account").authenticated()
	    	.anyRequest().permitAll();
		
		http.formLogin()
			.loginPage("/login")
			.loginProcessingUrl("/login-player")
	        .usernameParameter("username")
	        .passwordParameter("password")
			.defaultSuccessUrl("/login-success")
			.failureUrl("/login-fail")
			.permitAll();
			
		http.logout()
			.logoutUrl("/logout")
			.logoutSuccessUrl("/")
			.invalidateHttpSession(true)
			.clearAuthentication(true)
			.permitAll();
			
		return http.build();
	}

	@Bean
	static BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
