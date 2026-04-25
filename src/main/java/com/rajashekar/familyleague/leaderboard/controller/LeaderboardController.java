package com.rajashekar.familyleague.leaderboard.controller;

import com.rajashekar.familyleague.common.response.ApiResponse;
import com.rajashekar.familyleague.common.response.PagedResponse;
import com.rajashekar.familyleague.leaderboard.dto.LeaderboardResponse;
import com.rajashekar.familyleague.leaderboard.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seasons/{seasonId}/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<LeaderboardResponse>>> getLeaderboard(
            @PathVariable Long seasonId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                PagedResponse.from(leaderboardService.getSeasonLeaderboard(seasonId, pageable))));
    }
}
