package com.github.hallbm.chesswithcats.DTO;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
