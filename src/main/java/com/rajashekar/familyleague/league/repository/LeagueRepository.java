package com.rajashekar.familyleague.league.repository;

import com.rajashekar.familyleague.league.entity.League;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeagueRepository extends JpaRepository<League, Long> {

    boolean existsByName(String name);

    Optional<League> findByName(String name);

    Page<League> findByNameContainingIgnoreCase(String search, Pageable pageable);
}
