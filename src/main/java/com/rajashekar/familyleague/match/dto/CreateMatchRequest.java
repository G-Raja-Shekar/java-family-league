package com.rajashekar.familyleague.match.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateMatchRequest(
        @NotNull Long homeTeamId,
        @NotNull Long awayTeamId,
        @NotNull Instant startTime
) {}
