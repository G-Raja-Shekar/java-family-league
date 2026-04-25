package com.rajashekar.familyleague.match.repository;

import com.rajashekar.familyleague.match.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    Page<Match> findBySeasonId(Long seasonId, Pageable pageable);

    List<Match> findByStartTimeBetween(Instant from, Instant to);
}
