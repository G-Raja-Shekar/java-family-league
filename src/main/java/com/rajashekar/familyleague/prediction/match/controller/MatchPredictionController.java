package com.rajashekar.familyleague.prediction.match.controller;

import com.rajashekar.familyleague.common.response.ApiResponse;
import com.rajashekar.familyleague.common.security.CurrentUser;
import com.rajashekar.familyleague.prediction.match.dto.HeadToHeadResponse;
import com.rajashekar.familyleague.prediction.match.dto.MatchPredictionResponse;
import com.rajashekar.familyleague.prediction.match.dto.SubmitMatchPredictionRequest;
import com.rajashekar.familyleague.prediction.match.service.MatchPredictionService;
import com.rajashekar.familyleague.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/matches/{matchId}/predictions")
@RequiredArgsConstructor
public class MatchPredictionController {

    private final MatchPredictionService matchPredictionService;

    @PostMapping
    public ResponseEntity<ApiResponse<MatchPredictionResponse>> submit(
            @PathVariable Long matchId,
            @Valid @RequestBody SubmitMatchPredictionRequest request,
            @CurrentUser User currentUser,
            UriComponentsBuilder ucb) {
        MatchPredictionResponse response = matchPredictionService.submit(matchId, currentUser, request);
        URI location = ucb.path("/api/v1/matches/{matchId}/predictions/me")
                .buildAndExpand(matchId).toUri();
        return ResponseEntity.created(location).body(ApiResponse.ok(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MatchPredictionResponse>> getMyPrediction(
            @PathVariable Long matchId,
            @CurrentUser User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(
                matchPredictionService.getMyPrediction(matchId, currentUser)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<HeadToHeadResponse>> getHeadToHead(
            @PathVariable Long matchId) {
        return ResponseEntity.ok(ApiResponse.ok(matchPredictionService.getHeadToHead(matchId)));
    }
}
