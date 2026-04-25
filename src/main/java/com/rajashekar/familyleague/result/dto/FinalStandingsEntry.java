package com.rajashekar.familyleague.result.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FinalStandingsEntry(
        @NotNull Long teamId,
        @Min(1) int position
) {}
