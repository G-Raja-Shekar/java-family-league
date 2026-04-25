package com.rajashekar.familyleague.leaderboard.repository;

import com.rajashekar.familyleague.leaderboard.entity.LeaderboardEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaderboardEntryRepository extends JpaRepository<LeaderboardEntry, Long> {

    Optional<LeaderboardEntry> findBySeasonIdAndUserId(Long seasonId, Long userId);

    List<LeaderboardEntry> findBySeasonIdOrderByTotalPointsDesc(Long seasonId);

    Page<LeaderboardEntry> findBySeasonIdOrderByRankAsc(Long seasonId, Pageable pageable);
}
