package com.rajashekar.familyleague.league.controller;

import com.rajashekar.familyleague.common.response.ApiResponse;
import com.rajashekar.familyleague.common.response.PagedResponse;
import com.rajashekar.familyleague.league.dto.CreateLeagueRequest;
import com.rajashekar.familyleague.league.dto.LeagueResponse;
import com.rajashekar.familyleague.league.dto.LeagueSummaryResponse;
import com.rajashekar.familyleague.league.service.LeagueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/leagues")
@RequiredArgsConstructor
public class LeagueController {

    private final LeagueService leagueService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LeagueResponse>> create(
            @Valid @RequestBody CreateLeagueRequest request,
            UriComponentsBuilder ucb) {
        LeagueResponse response = leagueService.create(request);
        URI location = ucb.path("/api/v1/leagues/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeagueResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(leagueService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<LeagueSummaryResponse>>> list(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(leagueService.list(search, pageable))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        leagueService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
