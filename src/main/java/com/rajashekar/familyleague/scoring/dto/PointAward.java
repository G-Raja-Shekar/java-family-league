package com.rajashekar.familyleague.scoring.dto;

public record PointAward(
        Long userId,
        Long matchId,
        int points,
        String reason
) {}
