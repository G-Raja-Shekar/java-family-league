package com.rajashekar.familyleague.league.service;

import com.rajashekar.familyleague.common.exception.ResourceNotFoundException;
import com.rajashekar.familyleague.common.exception.ValidationException;
import com.rajashekar.familyleague.league.dto.CreateLeagueRequest;
import com.rajashekar.familyleague.league.dto.LeagueResponse;
import com.rajashekar.familyleague.league.dto.LeagueSummaryResponse;
import com.rajashekar.familyleague.league.entity.League;
import com.rajashekar.familyleague.league.mapper.LeagueMapper;
import com.rajashekar.familyleague.league.repository.LeagueRepository;
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
public class LeagueServiceImpl implements LeagueService {

    private final LeagueRepository leagueRepository;
    private final LeagueMapper leagueMapper;

    @Override
    @Transactional
    public LeagueResponse create(CreateLeagueRequest request) {
        if (leagueRepository.existsByName(request.name())) {
            throw new ValidationException("League already exists: " + request.name());
        }
        League league = new League();
        league.setName(request.name());
        league.setDescription(request.description());
        league.setLogoUrl(request.logoUrl());
        League saved = leagueRepository.save(league);
        log.info("League created: {}", saved.getName());
        return leagueMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LeagueResponse getById(Long id) {
        return leagueMapper.toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeagueSummaryResponse> list(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return leagueRepository.findByNameContainingIgnoreCase(search, pageable)
                    .map(leagueMapper::toSummary);
        }
        return leagueRepository.findAll(pageable).map(leagueMapper::toSummary);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        League league = findById(id);
        league.setDeletedAt(Instant.now());
        leagueRepository.save(league);
        log.info("League soft-deleted: {}", id);
    }

    private League findById(Long id) {
        return leagueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("League", id));
    }
}
