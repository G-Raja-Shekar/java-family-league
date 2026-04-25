package com.rajashekar.familyleague.season.mapper;

import com.rajashekar.familyleague.league.mapper.LeagueMapper;
import com.rajashekar.familyleague.season.dto.SeasonResponse;
import com.rajashekar.familyleague.season.dto.SeasonSummaryResponse;
import com.rajashekar.familyleague.season.entity.Season;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeasonMapper {

    private final LeagueMapper leagueMapper;

    public SeasonResponse toResponse(Season season) {
        if (season == null) return null;
        return new SeasonResponse(
                season.getId(),
                season.getName(),
                leagueMapper.toSummary(season.getLeague()),
                season.getStatus(),
                season.getFirstMatchStartTime(),
                season.getStartDate(),
                season.getEndDate(),
                season.getCreatedAt()
        );
    }

    public SeasonSummaryResponse toSummary(Season season) {
        if (season == null) return null;
        return new SeasonSummaryResponse(
                season.getId(),
                season.getName(),
                season.getLeague().getId(),
                season.getLeague().getName(),
                season.getStatus()
        );
    }
}
