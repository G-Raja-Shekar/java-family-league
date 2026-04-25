package com.rajashekar.familyleague.player.dto;

public record PlayerSummaryResponse(
        Long id,
        String name,
        String displayName,
        Long teamId,
        String teamName
) {}
