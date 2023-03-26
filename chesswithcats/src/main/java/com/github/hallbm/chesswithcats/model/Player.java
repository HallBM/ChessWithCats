package com.github.hallbm.chesswithcats.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.hibernate.annotations.NaturalId;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Player/User data persistence; extends UserDetails and AuthenticatedPrincipal for Spring Security authorizations
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "players")
public class Player implements UserDetails, AuthenticatedPrincipal {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@NaturalId
	@Column(unique = true, nullable = false, length = 30)
	@Size(max = 30, message = "Username is too long (limit to 30 characters)")
	@NotBlank (message = "Username cannot be blank")
	private String username;

	@Column(length = 30)
	@NotBlank (message = "Icon is required")
	private String iconFile;

	private String password;

	@Column(length = 50)
	@NotBlank (message = "First name cannot be blank")
	private String firstName;

	@Column(length = 50)
	@NotBlank (message = "Last name cannot be blank")
	private String lastName;

	@Column(length = 50)
	@Email
	private String email;

	@Column(updatable = false)
	@Temporal(TemporalType.DATE)
	private LocalDate dateJoined = LocalDate.now();

	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime lastLogin;

	private boolean isOnline = false;
	
	private boolean isEnabled = true;
	
	@Override
	  public Collection<? extends GrantedAuthority> getAuthorities() {
	    return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
	  }
	
	@Override
	public String getName() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	
	@Override
	public boolean equals(Object o) {
	    if (this == o) return true;
	    if (o == null || this.getClass() != o.getClass()) return false;
	    Player other = (Player) o;
	    return Objects.equals(id, other.getId()) &&
	            Objects.equals(username, other.getUsername()) &&
	            Objects.equals(iconFile, other.getIconFile()) &&
	            Objects.equals(password, other.getPassword()) &&
	            Objects.equals(firstName, other.getFirstName()) &&
	            Objects.equals(lastName, other.getLastName()) &&
	            Objects.equals(email, other.getEmail()) &&
	            Objects.equals(dateJoined, other.getDateJoined());
	}

	@Override
	public int hashCode() {
	    return Objects.hash(id, username, iconFile, password, firstName, lastName, email, dateJoined);
	}
	
}
