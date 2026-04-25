package com.rajashekar.familyleague.league.dto;

import java.time.Instant;

public record LeagueResponse(
        Long id,
        String name,
        String description,
        String logoUrl,
        Instant createdAt
) {}
