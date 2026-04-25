package com.rajashekar.familyleague.prediction.match.mapper;

import com.rajashekar.familyleague.prediction.match.dto.MatchPredictionResponse;
import com.rajashekar.familyleague.prediction.match.entity.MatchPrediction;
import org.springframework.stereotype.Component;

@Component
public class MatchPredictionMapper {

    public MatchPredictionResponse toResponse(MatchPrediction p) {
        if (p == null) return null;
        return new MatchPredictionResponse(
                p.getId(),
                p.getMatch().getId(),
                p.getUser().getId(),
                p.getUser().getDisplayName(),
                p.isPredictedTie(),
                p.getPredictedWinner() != null ? p.getPredictedWinner().getId() : null,
                p.getPredictedWinner() != null ? p.getPredictedWinner().getName() : null,
                p.getPredictedTossWinner() != null ? p.getPredictedTossWinner().getId() : null,
                p.getPredictedTossWinner() != null ? p.getPredictedTossWinner().getName() : null,
                p.getPredictedPlayerOfMatch() != null ? p.getPredictedPlayerOfMatch().getId() : null,
                p.getPredictedPlayerOfMatch() != null ? p.getPredictedPlayerOfMatch().getName() : null,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
