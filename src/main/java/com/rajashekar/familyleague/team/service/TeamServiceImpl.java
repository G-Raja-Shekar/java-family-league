package com.rajashekar.familyleague.team.service;

import com.rajashekar.familyleague.common.exception.ResourceNotFoundException;
import com.rajashekar.familyleague.common.exception.ValidationException;
import com.rajashekar.familyleague.team.dto.CreateTeamRequest;
import com.rajashekar.familyleague.team.dto.TeamResponse;
import com.rajashekar.familyleague.team.dto.TeamSummaryResponse;
import com.rajashekar.familyleague.team.entity.Team;
import com.rajashekar.familyleague.team.mapper.TeamMapper;
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
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;

    @Override
    @Transactional
    public TeamResponse create(CreateTeamRequest request) {
        if (teamRepository.existsByName(request.name())) {
            throw new ValidationException("Team already exists: " + request.name());
        }
        Team team = new Team();
        team.setName(request.name());
        team.setShortName(request.shortName());
        team.setLogoUrl(request.logoUrl());
        Team saved = teamRepository.save(team);
        log.info("Team created: {}", saved.getName());
        return teamMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getById(Long id) {
        return teamMapper.toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeamSummaryResponse> list(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return teamRepository.findByNameContainingIgnoreCase(search, pageable)
                    .map(teamMapper::toSummary);
        }
        return teamRepository.findAll(pageable).map(teamMapper::toSummary);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Team team = findById(id);
        team.setDeletedAt(Instant.now());
        teamRepository.save(team);
        log.info("Team soft-deleted: {}", id);
    }

    Team findById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
    }
}
