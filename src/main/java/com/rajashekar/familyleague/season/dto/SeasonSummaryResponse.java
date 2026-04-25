package com.rajashekar.familyleague.season.dto;

import com.rajashekar.familyleague.season.entity.SeasonStatus;

public record SeasonSummaryResponse(
        Long id,
        String name,
        Long leagueId,
        String leagueName,
        SeasonStatus status
) {}
