package com.github.hallbm.chesswithcats.model;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.annotations.NaturalId;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "players")
public class Player implements UserDetails, AuthenticatedPrincipal {

	private static final long serialVersionUID = -2076473928351138338L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

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

	@Column(name = "first_name", length = 30)
	@Size(min = 2, max = 30)
	@NotBlank
	private String firstName;

	@Column(name = "last_name", length = 30)
	@Size(min = 2, max = 30)
	@NotBlank
	private String lastName;

	@Column(length = 50)
	@Email
	@NotBlank
	private String email;

	@Column(updatable = false)
	@NotNull
	@Temporal(TemporalType.DATE)
	private Date dateJoined = new Date();

	@Column
	@NotNull
	private boolean isLoggedIn = false;

	@Column
	@NotNull
	private boolean isPlaying = false;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private Collection<Authority> authorities;

	@Column
	private boolean isAccountNonExpired = true;

	@Column
	private boolean isAccountNonLocked = true;

	@Column
	private boolean isCredentialsNonExpired = true;

	@Column
	private boolean isEnabled = true;

	@Override
	public String getName() {
		return username;
	}
	
}
