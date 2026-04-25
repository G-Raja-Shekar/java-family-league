package com.rajashekar.familyleague.result.event;

import org.springframework.context.ApplicationEvent;

public class ResultPublishedEvent extends ApplicationEvent {

    private final Long matchId;
    private final Long seasonId;

    public ResultPublishedEvent(Object source, Long matchId, Long seasonId) {
        super(source);
        this.matchId = matchId;
        this.seasonId = seasonId;
    }

    public Long getMatchId() {
        return matchId;
    }

    public Long getSeasonId() {
        return seasonId;
    }
}
