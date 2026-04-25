package com.rajashekar.familyleague.prediction.match.dto;

public record SubmitMatchPredictionRequest(
        Long predictedWinnerId,
        Long predictedTossWinnerId,
        Long predictedPlayerOfMatchId
) {}
