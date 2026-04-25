package com.rajashekar.familyleague.prediction.league.controller;

import com.rajashekar.familyleague.common.response.ApiResponse;
import com.rajashekar.familyleague.common.security.CurrentUser;
import com.rajashekar.familyleague.prediction.league.dto.LeagueHeadToHeadResponse;
import com.rajashekar.familyleague.prediction.league.dto.LeaguePredictionResponse;
import com.rajashekar.familyleague.prediction.league.dto.SubmitLeaguePredictionRequest;
import com.rajashekar.familyleague.prediction.league.service.LeaguePredictionService;
import com.rajashekar.familyleague.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/seasons/{seasonId}/predictions/league")
@RequiredArgsConstructor
public class LeaguePredictionController {

    private final LeaguePredictionService leaguePredictionService;

    @PostMapping
    public ResponseEntity<ApiResponse<LeaguePredictionResponse>> submit(
            @PathVariable Long seasonId,
            @Valid @RequestBody SubmitLeaguePredictionRequest request,
            @CurrentUser User currentUser,
            UriComponentsBuilder ucb) {
        LeaguePredictionResponse response = leaguePredictionService.submit(seasonId, currentUser, request);
        URI location = ucb.path("/api/v1/seasons/{seasonId}/predictions/league/me")
                .buildAndExpand(seasonId).toUri();
        return ResponseEntity.created(location).body(ApiResponse.ok(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LeaguePredictionResponse>> getMyPrediction(
            @PathVariable Long seasonId,
            @CurrentUser User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(
                leaguePredictionService.getMyPrediction(seasonId, currentUser)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<LeagueHeadToHeadResponse>> getHeadToHead(
            @PathVariable Long seasonId) {
        return ResponseEntity.ok(ApiResponse.ok(leaguePredictionService.getHeadToHead(seasonId)));
    }
}
