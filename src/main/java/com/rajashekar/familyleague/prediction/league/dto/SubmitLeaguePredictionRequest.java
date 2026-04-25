package com.rajashekar.familyleague.prediction.league.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SubmitLeaguePredictionRequest(
        @NotNull @NotEmpty @Valid List<TeamPositionEntry> predictedPositions
) {}
