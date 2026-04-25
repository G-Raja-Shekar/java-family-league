package com.rajashekar.familyleague.leaderboard.service;

import com.rajashekar.familyleague.leaderboard.dto.LeaderboardResponse;
import com.rajashekar.familyleague.result.event.ResultPublishedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeaderboardService {

    void handleResultPublished(ResultPublishedEvent event);

    Page<LeaderboardResponse> getSeasonLeaderboard(Long seasonId, Pageable pageable);
}
