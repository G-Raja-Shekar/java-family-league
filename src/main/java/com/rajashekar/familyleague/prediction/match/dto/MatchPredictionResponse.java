package com.rajashekar.familyleague.prediction.match.dto;

import java.time.Instant;

public record MatchPredictionResponse(
        Long id,
        Long matchId,
        Long userId,
        String userDisplayName,
        Long predictedWinnerId,
        String predictedWinnerName,
        Long predictedTossWinnerId,
        String predictedTossWinnerName,
        Long predictedPlayerOfMatchId,
        String predictedPlayerOfMatchName,
        Instant createdAt,
        Instant updatedAt
) {}
