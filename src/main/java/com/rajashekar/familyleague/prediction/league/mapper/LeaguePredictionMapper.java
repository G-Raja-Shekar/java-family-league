package com.rajashekar.familyleague.prediction.league.mapper;

import com.rajashekar.familyleague.prediction.league.dto.LeaguePredictionResponse;
import com.rajashekar.familyleague.prediction.league.entity.LeaguePrediction;
import org.springframework.stereotype.Component;

@Component
public class LeaguePredictionMapper {

    public LeaguePredictionResponse toResponse(LeaguePrediction p) {
        if (p == null) return null;
        return new LeaguePredictionResponse(
                p.getId(),
                p.getSeason().getId(),
                p.getSeason().getName(),
                p.getUser().getId(),
                p.getUser().getDisplayName(),
                p.getPredictedPositions(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
