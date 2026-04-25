package com.rajashekar.familyleague.player.dto;

import com.rajashekar.familyleague.team.dto.TeamSummaryResponse;

import java.time.Instant;

public record PlayerResponse(
        Long id,
        String name,
        String displayName,
        TeamSummaryResponse team,
        Instant createdAt
) {}
