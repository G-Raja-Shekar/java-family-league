package com.rajashekar.familyleague.season.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateSeasonRequest(
        @NotBlank String name,
        @NotNull Long leagueId,
        Instant firstMatchStartTime,
        Instant startDate,
        Instant endDate
) {}
