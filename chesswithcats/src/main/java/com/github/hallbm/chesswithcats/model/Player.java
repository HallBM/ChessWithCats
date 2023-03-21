package com.github.hallbm.chesswithcats.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.annotations.NaturalId;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Player/User data persistence; extends UserDetails and AuthenticatedPrincipal for Spring Security authorizations
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "players")
public class Player implements UserDetails, AuthenticatedPrincipal {

	@Transient
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

	@NaturalId
	@Column(unique = true, nullable = false, length = 30)
	@Size(max = 30)
	@NotBlank
	private String username;

	@Column(length = 30)
	@NotBlank
	private String iconFile;

	private String password;

	@Column(name = "first_name", length = 50)
	@NotBlank
	private String firstName;

	@Column(name = "last_name", length = 50)
	@NotBlank
	private String lastName;

	@Column(length = 50)
	@Email
	private String email;

	@Column(updatable = false)
	@Temporal(TemporalType.DATE)
	private LocalDate dateJoined = LocalDate.now();

	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime lastLogin;

	private boolean isLogged = false;

	private boolean isPlaying = false;
	
	private boolean isActive = true;
	
	@OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
	private List<Authority> authorities;
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		  return authorities.stream()
		    .map(a -> new SimpleGrantedAuthority(a.getAuthority()))
		    .collect(Collectors.toList());
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
	public boolean isEnabled() {
		return isActive;
	}



}
