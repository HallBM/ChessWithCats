package com.github.hallbm.chesswithcats.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for transmitting login data from front end for user validation / authentication
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginCredentialsDTO {
		private String username;
		private String password;
}
