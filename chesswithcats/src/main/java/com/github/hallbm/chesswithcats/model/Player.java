package com.github.hallbm.chesswithcats.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

import org.hibernate.annotations.NaturalId;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
	@Size(min = 3, max = 30)
	@NotBlank
	private String username;

	@Column(length = 30)
	@NotBlank
	private String iconFile;

	@NotBlank
	private String password;

	@Column(name = "first_name", length = 50)
	@NotBlank
	private String firstName;

	@Column(name = "last_name", length = 50)
	@NotBlank
	private String lastName;

	@Column(length = 50)
	@Email
	@NotBlank
	private String email;

	@Column(name="date_joined", updatable = false)
	@NotNull
	@Temporal(TemporalType.DATE)
	private LocalDate dateJoined = LocalDate.now();

	@Column(name = "last_login")
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime lastLogin;

	@Column
	@NotNull
	private boolean isLogged = false;

	@Column
	@NotNull
	private boolean isPlaying = false;
	
	@Column
	@NotNull
	private boolean isActive = true;
	
	
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable(
		    name = "players_authorities",
		    joinColumns = @JoinColumn(name = "player_id", referencedColumnName = "id"),
		    inverseJoinColumns = @JoinColumn(name = "authority_id", referencedColumnName = "id")
		)
	private Collection<Authority> authorities;

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
