package com.rajashekar.familyleague.season.repository;

import com.rajashekar.familyleague.season.entity.Season;
import com.rajashekar.familyleague.season.entity.SeasonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, Long> {

    Page<Season> findByLeagueId(Long leagueId, Pageable pageable);

    Page<Season> findByStatus(SeasonStatus status, Pageable pageable);

    Optional<Season> findFirstByLeagueIdAndStatusOrderByStartDateAsc(Long leagueId, SeasonStatus status);
}
