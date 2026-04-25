package com.rajashekar.familyleague.player.controller;

import com.rajashekar.familyleague.common.response.ApiResponse;
import com.rajashekar.familyleague.common.response.PagedResponse;
import com.rajashekar.familyleague.player.dto.CreatePlayerRequest;
import com.rajashekar.familyleague.player.dto.PlayerResponse;
import com.rajashekar.familyleague.player.dto.PlayerSummaryResponse;
import com.rajashekar.familyleague.player.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlayerResponse>> create(
            @Valid @RequestBody CreatePlayerRequest request,
            UriComponentsBuilder ucb) {
        PlayerResponse response = playerService.create(request);
        URI location = ucb.path("/api/v1/players/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlayerResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(playerService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PlayerSummaryResponse>>> list(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(playerService.list(teamId, search, pageable))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        playerService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
