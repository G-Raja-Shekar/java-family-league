package com.rajashekar.familyleague.team.dto;

public record TeamSummaryResponse(
        Long id,
        String name,
        String shortName,
        String logoUrl
) {}
