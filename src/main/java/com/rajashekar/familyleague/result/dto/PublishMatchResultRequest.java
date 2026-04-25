package com.rajashekar.familyleague.result.dto;

public record PublishMatchResultRequest(
        Long winnerId,
        Long tossWinnerId,
        Long playerOfMatchId,
        boolean tie
) {}
