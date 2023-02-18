package com.github.hallbm.chesswithcats.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.NaturalId;
import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class Player {
	
	@Id
	@Column(updatable = false, nullable = false)
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
	private Date dateJoined;
	
	@ColumnDefault("500")
	@NotNull
	private Integer points = 500;
	
	@Column
	@NotNull
	private boolean isLoggedIn;
	
	@Column
	@NotNull
	private boolean isPlaying;
	
	@ManyToMany(fetch = FetchType.EAGER)
	private Set<Player> friends = new HashSet<>();
	
	@ManyToMany(fetch = FetchType.EAGER)
	private Set<Player> blocked = new HashSet<>();
	
	//TODO: Fill out later when other classes are created
	
	//private Set<Game> playedGames; 
	//@OneToMany 
	//private Set<GameRequest> gameRequests;
	
	
	
}
