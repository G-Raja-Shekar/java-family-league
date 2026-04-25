package com.rajashekar.familyleague.prediction.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.prediction")
public record PredictionProperties(
        long matchLockHours,
        long leagueLockHours
) {}
