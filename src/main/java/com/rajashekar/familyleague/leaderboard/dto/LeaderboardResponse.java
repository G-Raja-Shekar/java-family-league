package com.rajashekar.familyleague.leaderboard.dto;

public record LeaderboardResponse(
        Long userId,
        String userDisplayName,
        int totalPoints,
        int rank
) {}
