package com.rajashekar.familyleague.league.mapper;

import com.rajashekar.familyleague.league.dto.LeagueResponse;
import com.rajashekar.familyleague.league.dto.LeagueSummaryResponse;
import com.rajashekar.familyleague.league.entity.League;
import org.springframework.stereotype.Component;

@Component
public class LeagueMapper {

    public LeagueResponse toResponse(League league) {
        if (league == null) return null;
        return new LeagueResponse(
                league.getId(),
                league.getName(),
                league.getDescription(),
                league.getLogoUrl(),
                league.getCreatedAt()
        );
    }

    public LeagueSummaryResponse toSummary(League league) {
        if (league == null) return null;
        return new LeagueSummaryResponse(league.getId(), league.getName(), league.getLogoUrl());
    }
}
