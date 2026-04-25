package com.rajashekar.familyleague.prediction.league.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TeamPositionEntry(
        @NotNull Long teamId,
        @Min(1) int position
) {}
