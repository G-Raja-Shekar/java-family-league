package com.rajashekar.familyleague.leaderboard.mapper;

import com.rajashekar.familyleague.leaderboard.dto.LeaderboardResponse;
import com.rajashekar.familyleague.leaderboard.entity.LeaderboardEntry;
import org.springframework.stereotype.Component;

@Component
public class LeaderboardMapper {

    public LeaderboardResponse toResponse(LeaderboardEntry entry) {
        if (entry == null) return null;
        return new LeaderboardResponse(
                entry.getUser().getId(),
                entry.getUser().getDisplayName(),
                entry.getTotalPoints(),
                entry.getRank()
        );
    }
}
