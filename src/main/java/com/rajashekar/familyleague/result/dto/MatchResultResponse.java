package com.rajashekar.familyleague.result.dto;

import java.time.Instant;

public record MatchResultResponse(
        Long id,
        Long matchId,
        Long winnerId,
        String winnerName,
        Long tossWinnerId,
        String tossWinnerName,
        Long playerOfMatchId,
        String playerOfMatchName,
        boolean tie,
        Instant createdAt
) {}
