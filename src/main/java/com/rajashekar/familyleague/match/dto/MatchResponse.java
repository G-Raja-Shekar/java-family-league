package com.rajashekar.familyleague.match.dto;

import com.rajashekar.familyleague.team.dto.TeamSummaryResponse;

import java.time.Instant;

public record MatchResponse(
        Long id,
        Long seasonId,
        String seasonName,
        TeamSummaryResponse homeTeam,
        TeamSummaryResponse awayTeam,
        Instant startTime,
        Instant lockTime,
        Instant createdAt
) {}
