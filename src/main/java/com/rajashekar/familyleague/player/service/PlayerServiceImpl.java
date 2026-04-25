package com.rajashekar.familyleague.player.service;

import com.rajashekar.familyleague.common.exception.ResourceNotFoundException;
import com.rajashekar.familyleague.player.dto.CreatePlayerRequest;
import com.rajashekar.familyleague.player.dto.PlayerResponse;
import com.rajashekar.familyleague.player.dto.PlayerSummaryResponse;
import com.rajashekar.familyleague.player.entity.Player;
import com.rajashekar.familyleague.player.mapper.PlayerMapper;
import com.rajashekar.familyleague.player.repository.PlayerRepository;
import com.rajashekar.familyleague.team.entity.Team;
import com.rajashekar.familyleague.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final TeamRepository teamRepository;

    @Override
    @Transactional
    public PlayerResponse create(CreatePlayerRequest request) {
        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team", request.teamId()));
        Player player = new Player();
        player.setName(request.name());
        player.setDisplayName(request.displayName());
        player.setTeam(team);
        Player saved = playerRepository.save(player);
        log.info("Player created: {} for team {}", saved.getName(), team.getName());
        return playerMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PlayerResponse getById(Long id) {
        return playerMapper.toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlayerSummaryResponse> list(Long teamId, String search, Pageable pageable) {
        boolean hasTeam = teamId != null;
        boolean hasSearch = search != null && !search.isBlank();

        if (hasTeam && hasSearch) {
            return playerRepository.findByTeamIdAndNameContainingIgnoreCase(teamId, search, pageable)
                    .map(playerMapper::toSummary);
        }
        if (hasTeam) {
            return playerRepository.findByTeamId(teamId, pageable).map(playerMapper::toSummary);
        }
        if (hasSearch) {
            return playerRepository.findByNameContainingIgnoreCase(search, pageable)
                    .map(playerMapper::toSummary);
        }
        return playerRepository.findAll(pageable).map(playerMapper::toSummary);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Player player = findById(id);
        player.setDeletedAt(Instant.now());
        playerRepository.save(player);
        log.info("Player soft-deleted: {}", id);
    }

    private Player findById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", id));
    }
}
