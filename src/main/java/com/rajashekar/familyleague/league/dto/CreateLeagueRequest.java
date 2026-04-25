package com.rajashekar.familyleague.league.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateLeagueRequest(
        @NotBlank String name,
        String description,
        String logoUrl
) {}
