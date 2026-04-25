package com.rajashekar.familyleague.season.dto;

import com.rajashekar.familyleague.league.dto.LeagueSummaryResponse;
import com.rajashekar.familyleague.season.entity.SeasonStatus;

import java.time.Instant;

public record SeasonResponse(
        Long id,
        String name,
        LeagueSummaryResponse league,
        SeasonStatus status,
        Instant firstMatchStartTime,
        Instant startDate,
        Instant endDate,
        Instant createdAt
) {}
