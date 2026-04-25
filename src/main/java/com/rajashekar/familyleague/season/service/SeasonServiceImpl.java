package com.rajashekar.familyleague.season.service;

import com.rajashekar.familyleague.common.exception.ResourceNotFoundException;
import com.rajashekar.familyleague.common.exception.SeasonClosedException;
import com.rajashekar.familyleague.league.entity.League;
import com.rajashekar.familyleague.league.repository.LeagueRepository;
import com.rajashekar.familyleague.season.dto.CreateSeasonRequest;
import com.rajashekar.familyleague.season.dto.SeasonResponse;
import com.rajashekar.familyleague.season.dto.SeasonSummaryResponse;
import com.rajashekar.familyleague.season.entity.Season;
import com.rajashekar.familyleague.season.entity.SeasonStatus;
import com.rajashekar.familyleague.season.mapper.SeasonMapper;
import com.rajashekar.familyleague.season.repository.SeasonRepository;
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
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository seasonRepository;
    private final LeagueRepository leagueRepository;
    private final SeasonMapper seasonMapper;

    @Override
    @Transactional
    public SeasonResponse create(CreateSeasonRequest request) {
        League league = leagueRepository.findById(request.leagueId())
                .orElseThrow(() -> new ResourceNotFoundException("League", request.leagueId()));
        Season season = new Season();
        season.setName(request.name());
        season.setLeague(league);
        season.setFirstMatchStartTime(request.firstMatchStartTime());
        season.setStartDate(request.startDate());
        season.setEndDate(request.endDate());
        Season saved = seasonRepository.save(season);
        log.info("Season created: {} for league {}", saved.getName(), league.getName());
        return seasonMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SeasonResponse getById(Long id) {
        return seasonMapper.toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SeasonSummaryResponse> list(Pageable pageable) {
        return seasonRepository.findAll(pageable).map(seasonMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SeasonSummaryResponse> listByLeague(Long leagueId, Pageable pageable) {
        return seasonRepository.findByLeagueId(leagueId, pageable).map(seasonMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SeasonSummaryResponse> listByStatus(SeasonStatus status, Pageable pageable) {
        return seasonRepository.findByStatus(status, pageable).map(seasonMapper::toSummary);
    }

    @Override
    @Transactional
    public SeasonResponse updateStatus(Long id, SeasonStatus newStatus) {
        Season season = findById(id);
        if (season.getStatus() == SeasonStatus.CLOSED) {
            throw new SeasonClosedException("Season " + id + " is closed and cannot be modified");
        }
        season.setStatus(newStatus);
        Season saved = seasonRepository.save(season);
        log.info("Season {} status updated to {}", id, newStatus);
        return seasonMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Season season = findById(id);
        if (season.getStatus() == SeasonStatus.CLOSED) {
            throw new SeasonClosedException("Season " + id + " is closed and cannot be deleted");
        }
        season.setDeletedAt(Instant.now());
        seasonRepository.save(season);
        log.info("Season soft-deleted: {}", id);
    }

    private Season findById(Long id) {
        return seasonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Season", id));
    }
}
