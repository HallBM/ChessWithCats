package com.github.hallbm.chesswithcats.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests()
			.requestMatchers("/profile").authenticated()
			.anyRequest().permitAll();
		
		http.formLogin()
			.loginPage("/login")
			.loginProcessingUrl("/login-player")
	        .usernameParameter("username")
	        .passwordParameter("password")
			.successForwardUrl("/").defaultSuccessUrl("/")
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
