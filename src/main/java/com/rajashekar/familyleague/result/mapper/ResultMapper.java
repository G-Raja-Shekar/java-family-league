package com.rajashekar.familyleague.result.mapper;

import com.rajashekar.familyleague.result.dto.LeagueResultResponse;
import com.rajashekar.familyleague.result.dto.MatchResultResponse;
import com.rajashekar.familyleague.result.entity.LeagueResult;
import com.rajashekar.familyleague.result.entity.MatchResult;
import org.springframework.stereotype.Component;

@Component
public class ResultMapper {

    public MatchResultResponse toMatchResponse(MatchResult r) {
        if (r == null) return null;
        return new MatchResultResponse(
                r.getId(),
                r.getMatch().getId(),
                r.getWinner() != null ? r.getWinner().getId() : null,
                r.getWinner() != null ? r.getWinner().getName() : null,
                r.getTossWinner() != null ? r.getTossWinner().getId() : null,
                r.getTossWinner() != null ? r.getTossWinner().getName() : null,
                r.getPlayerOfMatch() != null ? r.getPlayerOfMatch().getId() : null,
                r.getPlayerOfMatch() != null ? r.getPlayerOfMatch().getName() : null,
                r.isTie(),
                r.getCreatedAt()
        );
    }

    public LeagueResultResponse toLeagueResponse(LeagueResult r) {
        if (r == null) return null;
        return new LeagueResultResponse(
                r.getId(),
                r.getSeason().getId(),
                r.getSeason().getName(),
                r.getFinalStandings(),
                r.getCreatedAt()
        );
    }
}
