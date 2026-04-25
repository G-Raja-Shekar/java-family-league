package com.rajashekar.familyleague.result.dto;

import java.time.Instant;
import java.util.List;

public record LeagueResultResponse(
        Long id,
        Long seasonId,
        String seasonName,
        List<FinalStandingsEntry> finalStandings,
        Instant createdAt
) {}
