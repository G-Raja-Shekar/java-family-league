package com.rajashekar.familyleague.player.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePlayerRequest(
        @NotBlank String name,
        String displayName,
        @NotNull Long teamId
) {}
