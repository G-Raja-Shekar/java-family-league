package com.rajashekar.familyleague.prediction.match.repository;

import com.rajashekar.familyleague.prediction.match.entity.MatchPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchPredictionRepository extends JpaRepository<MatchPrediction, Long> {

    Optional<MatchPrediction> findByMatchIdAndUserId(Long matchId, Long userId);

    List<MatchPrediction> findByMatchId(Long matchId);
}
