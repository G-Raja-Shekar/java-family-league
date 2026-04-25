package com.rajashekar.familyleague.team.dto;

import java.time.Instant;

public record TeamResponse(
        Long id,
        String name,
        String shortName,
        String logoUrl,
        Instant createdAt
) {}
