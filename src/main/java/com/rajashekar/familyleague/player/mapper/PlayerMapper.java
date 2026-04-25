package com.rajashekar.familyleague.player.mapper;

import com.rajashekar.familyleague.player.dto.PlayerResponse;
import com.rajashekar.familyleague.player.dto.PlayerSummaryResponse;
import com.rajashekar.familyleague.player.entity.Player;
import com.rajashekar.familyleague.team.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayerMapper {

    private final TeamMapper teamMapper;

    public PlayerResponse toResponse(Player player) {
        if (player == null) return null;
        return new PlayerResponse(
                player.getId(),
                player.getName(),
                player.getDisplayName(),
                teamMapper.toSummary(player.getTeam()),
                player.getCreatedAt()
        );
    }

    public PlayerSummaryResponse toSummary(Player player) {
        if (player == null) return null;
        return new PlayerSummaryResponse(
                player.getId(),
                player.getName(),
                player.getDisplayName(),
                player.getTeam().getId(),
                player.getTeam().getName()
        );
    }
}
