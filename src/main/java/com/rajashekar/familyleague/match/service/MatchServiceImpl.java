package com.rajashekar.familyleague.match.service;

import com.rajashekar.familyleague.common.exception.ResourceNotFoundException;
import com.rajashekar.familyleague.common.exception.SeasonClosedException;
import com.rajashekar.familyleague.common.exception.ValidationException;
import com.rajashekar.familyleague.match.dto.CreateMatchRequest;
import com.rajashekar.familyleague.match.dto.MatchResponse;
import com.rajashekar.familyleague.match.entity.Match;
import com.rajashekar.familyleague.match.mapper.MatchMapper;
import com.rajashekar.familyleague.match.repository.MatchRepository;
import com.rajashekar.familyleague.prediction.config.PredictionProperties;
import com.rajashekar.familyleague.season.entity.Season;
import com.rajashekar.familyleague.season.entity.SeasonStatus;
import com.rajashekar.familyleague.season.repository.SeasonRepository;
import com.rajashekar.familyleague.team.entity.Team;
import com.rajashekar.familyleague.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final SeasonRepository seasonRepository;
    private final TeamRepository teamRepository;
    private final MatchMapper matchMapper;
    private final PredictionProperties predictionProperties;

    @Override
    @Transactional
    public MatchResponse create(Long seasonId, CreateMatchRequest request) {
        Season season = findSeason(seasonId);
        if (season.getStatus() == SeasonStatus.CLOSED) {
            throw new SeasonClosedException("Season " + seasonId + " is closed");
        }
        if (request.homeTeamId().equals(request.awayTeamId())) {
            throw new ValidationException("Home team and away team must be different");
        }
        Team homeTeam = findTeam(request.homeTeamId());
        Team awayTeam = findTeam(request.awayTeamId());

        Match match = new Match();
        match.setSeason(season);
        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);
        match.setStartTime(request.startTime());
        Match saved = matchRepository.save(match);
        log.info("Match created: {} vs {} in season {}", homeTeam.getName(), awayTeam.getName(), seasonId);
        return matchMapper.toResponse(saved, deriveLockTime(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public MatchResponse getById(Long id) {
        Match match = findById(id);
        return matchMapper.toResponse(match, deriveLockTime(match));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MatchResponse> listBySeason(Long seasonId, Pageable pageable) {
        return matchRepository.findBySeasonId(seasonId, pageable)
                .map(m -> matchMapper.toResponse(m, deriveLockTime(m)));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Match match = findById(id);
        match.setDeletedAt(Instant.now());
        matchRepository.save(match);
        log.info("Match soft-deleted: {}", id);
    }

    public Instant deriveLockTime(Match match) {
        return match.getStartTime().minus(predictionProperties.matchLockHours(), ChronoUnit.HOURS);
    }

    Match findById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match", id));
    }

    private Season findSeason(Long id) {
        return seasonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Season", id));
    }

    private Team findTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
    }
}
