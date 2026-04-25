package com.rajashekar.familyleague.leaderboard.event;

import org.springframework.context.ApplicationEvent;

public class LeaderboardRecalculatedEvent extends ApplicationEvent {

    private final Long seasonId;

    public LeaderboardRecalculatedEvent(Object source, Long seasonId) {
        super(source);
        this.seasonId = seasonId;
    }

    public Long getSeasonId() {
        return seasonId;
    }
}
