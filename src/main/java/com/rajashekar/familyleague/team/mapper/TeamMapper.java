package com.rajashekar.familyleague.team.mapper;

import com.rajashekar.familyleague.team.dto.TeamResponse;
import com.rajashekar.familyleague.team.dto.TeamSummaryResponse;
import com.rajashekar.familyleague.team.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {

    public TeamResponse toResponse(Team team) {
        if (team == null) return null;
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getShortName(),
                team.getLogoUrl(),
                team.getCreatedAt()
        );
    }

    public TeamSummaryResponse toSummary(Team team) {
        if (team == null) return null;
        return new TeamSummaryResponse(
                team.getId(),
                team.getName(),
                team.getShortName(),
                team.getLogoUrl()
        );
    }
}
