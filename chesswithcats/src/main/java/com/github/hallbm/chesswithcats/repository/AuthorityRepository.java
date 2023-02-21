package com.github.hallbm.chesswithcats.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.hallbm.chesswithcats.model.Authority;

//@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Optional<Authority> findByAuthority(String authority);
}