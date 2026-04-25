package com.rajashekar.familyleague.prediction.league.dto;

import java.time.Instant;
import java.util.List;

public record LeaguePredictionResponse(
        Long id,
        Long seasonId,
        String seasonName,
        Long userId,
        String userDisplayName,
        List<TeamPositionEntry> predictedPositions,
        Instant createdAt,
        Instant updatedAt
) {}
