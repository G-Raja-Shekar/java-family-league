package com.rajashekar.familyleague.result.repository;

import com.rajashekar.familyleague.result.entity.LeagueResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeagueResultRepository extends JpaRepository<LeagueResult, Long> {

    Optional<LeagueResult> findBySeasonId(Long seasonId);

    boolean existsBySeasonId(Long seasonId);
}
