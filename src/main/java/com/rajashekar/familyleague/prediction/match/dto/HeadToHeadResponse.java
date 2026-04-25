package com.rajashekar.familyleague.prediction.match.dto;

import java.time.Instant;
import java.util.List;

public record HeadToHeadResponse(
        Long matchId,
        Instant lockTime,
        List<MatchPredictionResponse> predictions
) {}
