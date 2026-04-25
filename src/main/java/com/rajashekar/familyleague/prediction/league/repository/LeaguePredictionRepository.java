package com.rajashekar.familyleague.prediction.league.repository;

import com.rajashekar.familyleague.prediction.league.entity.LeaguePrediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaguePredictionRepository extends JpaRepository<LeaguePrediction, Long> {

    Optional<LeaguePrediction> findBySeasonIdAndUserId(Long seasonId, Long userId);

    List<LeaguePrediction> findBySeasonId(Long seasonId);
}
