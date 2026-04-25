package com.rajashekar.familyleague.team.controller;

import com.rajashekar.familyleague.common.response.ApiResponse;
import com.rajashekar.familyleague.common.response.PagedResponse;
import com.rajashekar.familyleague.team.dto.CreateTeamRequest;
import com.rajashekar.familyleague.team.dto.TeamResponse;
import com.rajashekar.familyleague.team.dto.TeamSummaryResponse;
import com.rajashekar.familyleague.team.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TeamResponse>> create(
            @Valid @RequestBody CreateTeamRequest request,
            UriComponentsBuilder ucb) {
        TeamResponse response = teamService.create(request);
        URI location = ucb.path("/api/v1/teams/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(teamService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<TeamSummaryResponse>>> list(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(teamService.list(search, pageable))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        teamService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
