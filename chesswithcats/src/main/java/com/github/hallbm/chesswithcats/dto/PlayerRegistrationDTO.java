package com.github.hallbm.chesswithcats.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for transmitting player registration data from the front end for validation and persistence
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRegistrationDTO {
		private String username;
		private String iconFile;
		private String password;
		private String confirmedPassword;
		private String firstName;
		private String lastName;
		private String email;
}
