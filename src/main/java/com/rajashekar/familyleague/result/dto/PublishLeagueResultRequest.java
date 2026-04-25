package com.rajashekar.familyleague.result.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PublishLeagueResultRequest(
        @NotNull @NotEmpty @Valid List<FinalStandingsEntry> finalStandings
) {}
