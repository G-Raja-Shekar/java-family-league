package com.rajashekar.familyleague.prediction.league.dto;

import java.time.Instant;
import java.util.List;

public record LeagueHeadToHeadResponse(
        Long seasonId,
        Instant lockTime,
        List<LeaguePredictionResponse> predictions
) {}
