package com.github.hallbm.chesswithcats.model;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.annotations.NaturalId;
import org.springframework.security.core.userdetails.UserDetails;

import com.github.hallbm.chesswithcats.enums.GameStyles;

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
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
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
public class Player implements UserDetails {
	
	private static final long serialVersionUID = -2076473928351138338L;

	@Id
	@Column(name = "player_id", updatable = false, nullable = false)
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long playerId;
	
	@NaturalId
	@Column(unique=true, nullable=false, length=30)
	@Size(min=3, max=30)
	@NotBlank
	private String username;
	
	@Column(length=30)
	@NotBlank
	private String iconFile;
	
	@NotBlank
	private String password;
	
	@Column(length = 30)
	@Size(min=2, max=30)
	@NotBlank
	private String firstName;
	
	@Column(length = 30)
	@Size(min=2, max=30)
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
	
    @ElementCollection
    @CollectionTable(name = "scores", 
      joinColumns = {@JoinColumn(name = "player_id", referencedColumnName = "player_id")})
    @MapKeyColumn(name = "game_style")
    @Column(name = "points")
	@NotNull
	private Map<String, Integer> points; 
	
	@Column
	@NotNull
	private boolean isLoggedIn = false;
	
	@Column
	@NotNull
	private boolean isPlaying = false;
	
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<Player> friends = new HashSet<>();

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<Player> unfriended = new HashSet<>();
	
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<Player> blocked = new HashSet<>();
	
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
	
	
	//TODO: Fill out later when other classes are created
	
	//private Set<Game> playedGames; 
	//@OneToMany 
	//private Set<GameRequest> gameRequests;
	
	
	

	
	
	
	
}
