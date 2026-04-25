package com.rajashekar.familyleague.season.controller;

import com.rajashekar.familyleague.common.response.ApiResponse;
import com.rajashekar.familyleague.common.response.PagedResponse;
import com.rajashekar.familyleague.season.dto.CreateSeasonRequest;
import com.rajashekar.familyleague.season.dto.SeasonResponse;
import com.rajashekar.familyleague.season.dto.SeasonSummaryResponse;
import com.rajashekar.familyleague.season.entity.SeasonStatus;
import com.rajashekar.familyleague.season.service.SeasonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/seasons")
@RequiredArgsConstructor
public class SeasonController {

    private final SeasonService seasonService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SeasonResponse>> create(
            @Valid @RequestBody CreateSeasonRequest request,
            UriComponentsBuilder ucb) {
        SeasonResponse response = seasonService.create(request);
        URI location = ucb.path("/api/v1/seasons/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SeasonResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(seasonService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<SeasonSummaryResponse>>> listByLeague(
            @RequestParam(required = false) Long leagueId,
            @RequestParam(required = false) SeasonStatus status,
            Pageable pageable) {
        if (leagueId != null) {
            return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(seasonService.listByLeague(leagueId, pageable))));
        }
        if (status != null) {
            return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(seasonService.listByStatus(status, pageable))));
        }
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(seasonService.list(pageable))));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SeasonResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam SeasonStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(seasonService.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        seasonService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
