package com.rajashekar.familyleague.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTeamRequest(
        @NotBlank String name,
        @Size(max = 10) String shortName,
        String logoUrl
) {}
