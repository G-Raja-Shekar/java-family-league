package com.rajashekar.familyleague.result.controller;

import com.rajashekar.familyleague.common.response.ApiResponse;
import com.rajashekar.familyleague.result.dto.LeagueResultResponse;
import com.rajashekar.familyleague.result.dto.MatchResultResponse;
import com.rajashekar.familyleague.result.dto.PublishLeagueResultRequest;
import com.rajashekar.familyleague.result.dto.PublishMatchResultRequest;
import com.rajashekar.familyleague.result.service.ResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    @PostMapping("/matches/{matchId}/result")
    public ResponseEntity<ApiResponse<MatchResultResponse>> publishMatchResult(
            @PathVariable Long matchId,
            @Valid @RequestBody PublishMatchResultRequest request,
            UriComponentsBuilder ucb) {
        MatchResultResponse response = resultService.publishMatchResult(matchId, request);
        URI location = ucb.path("/api/v1/admin/matches/{matchId}/result")
                .buildAndExpand(matchId).toUri();
        return ResponseEntity.created(location).body(ApiResponse.ok(response));
    }

    @PostMapping("/seasons/{seasonId}/result")
    public ResponseEntity<ApiResponse<LeagueResultResponse>> publishLeagueResult(
            @PathVariable Long seasonId,
            @Valid @RequestBody PublishLeagueResultRequest request,
            UriComponentsBuilder ucb) {
        LeagueResultResponse response = resultService.publishLeagueResult(seasonId, request);
        URI location = ucb.path("/api/v1/admin/seasons/{seasonId}/result")
                .buildAndExpand(seasonId).toUri();
        return ResponseEntity.created(location).body(ApiResponse.ok(response));
    }
}
