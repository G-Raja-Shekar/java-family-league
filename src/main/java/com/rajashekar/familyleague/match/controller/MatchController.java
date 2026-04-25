package com.rajashekar.familyleague.match.controller;

import com.rajashekar.familyleague.common.response.ApiResponse;
import com.rajashekar.familyleague.common.response.PagedResponse;
import com.rajashekar.familyleague.match.dto.CreateMatchRequest;
import com.rajashekar.familyleague.match.dto.MatchResponse;
import com.rajashekar.familyleague.match.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/seasons/{seasonId}/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MatchResponse>> create(
            @PathVariable Long seasonId,
            @Valid @RequestBody CreateMatchRequest request,
            UriComponentsBuilder ucb) {
        MatchResponse response = matchService.create(seasonId, request);
        URI location = ucb.path("/api/v1/seasons/{seasonId}/matches/{id}")
                .buildAndExpand(seasonId, response.id()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MatchResponse>> getById(
            @PathVariable Long seasonId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(matchService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<MatchResponse>>> list(
            @PathVariable Long seasonId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(matchService.listBySeason(seasonId, pageable))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long seasonId,
            @PathVariable Long id) {
        matchService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
