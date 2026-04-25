package com.rajashekar.familyleague.match.mapper;

import com.rajashekar.familyleague.match.dto.MatchResponse;
import com.rajashekar.familyleague.match.entity.Match;
import com.rajashekar.familyleague.team.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class MatchMapper {

    private final TeamMapper teamMapper;

    public MatchResponse toResponse(Match match, Instant lockTime) {
        if (match == null) return null;
        return new MatchResponse(
                match.getId(),
                match.getSeason().getId(),
                match.getSeason().getName(),
                teamMapper.toSummary(match.getHomeTeam()),
                teamMapper.toSummary(match.getAwayTeam()),
                match.getStartTime(),
                lockTime,
                match.getCreatedAt()
        );
    }
}
