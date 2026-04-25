package com.rajashekar.familyleague.prediction.league.service;

import com.rajashekar.familyleague.common.exception.PredictionLockedException;
import com.rajashekar.familyleague.common.exception.ResourceNotFoundException;
import com.rajashekar.familyleague.common.exception.SeasonClosedException;
import com.rajashekar.familyleague.prediction.config.PredictionProperties;
import com.rajashekar.familyleague.prediction.league.dto.LeagueHeadToHeadResponse;
import com.rajashekar.familyleague.prediction.league.dto.LeaguePredictionResponse;
import com.rajashekar.familyleague.prediction.league.dto.SubmitLeaguePredictionRequest;
import com.rajashekar.familyleague.prediction.league.entity.LeaguePrediction;
import com.rajashekar.familyleague.prediction.league.mapper.LeaguePredictionMapper;
import com.rajashekar.familyleague.prediction.league.repository.LeaguePredictionRepository;
import com.rajashekar.familyleague.season.entity.Season;
import com.rajashekar.familyleague.season.entity.SeasonStatus;
import com.rajashekar.familyleague.season.repository.SeasonRepository;
import com.rajashekar.familyleague.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaguePredictionServiceImpl implements LeaguePredictionService {

    private final LeaguePredictionRepository leaguePredictionRepository;
    private final SeasonRepository seasonRepository;
    private final LeaguePredictionMapper mapper;
    private final PredictionProperties predictionProperties;

    @Override
    @Transactional
    public LeaguePredictionResponse submit(Long seasonId, User user, SubmitLeaguePredictionRequest request) {
        Season season = findSeason(seasonId);

        if (season.getStatus() == SeasonStatus.CLOSED) {
            throw new SeasonClosedException("Season is closed — predictions are no longer accepted");
        }

        Instant lockTime = deriveLockTime(season);
        if (lockTime != null && !Instant.now().isBefore(lockTime)) {
            throw new PredictionLockedException(
                    "League prediction window is closed. Lock time was " + lockTime);
        }

        LeaguePrediction prediction = leaguePredictionRepository
                .findBySeasonIdAndUserId(seasonId, user.getId())
                .orElse(new LeaguePrediction());

        prediction.setSeason(season);
        prediction.setUser(user);
        prediction.setPredictedPositions(request.predictedPositions());

        LeaguePrediction saved = leaguePredictionRepository.save(prediction);
        log.info("League prediction upserted: season={} user={}", seasonId, user.getId());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaguePredictionResponse getMyPrediction(Long seasonId, User user) {
        findSeason(seasonId);
        return leaguePredictionRepository.findBySeasonIdAndUserId(seasonId, user.getId())
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("LeaguePrediction",
                        "season=" + seasonId + ",user=" + user.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public LeagueHeadToHeadResponse getHeadToHead(Long seasonId) {
        Season season = findSeason(seasonId);
        Instant lockTime = deriveLockTime(season);

        if (lockTime == null || Instant.now().isBefore(lockTime)) {
            throw new PredictionLockedException(
                    "League head-to-head predictions are only visible after the lock time");
        }

        List<LeaguePredictionResponse> predictions = leaguePredictionRepository.findBySeasonId(seasonId)
                .stream().map(mapper::toResponse).toList();
        return new LeagueHeadToHeadResponse(seasonId, lockTime, predictions);
    }

    private Instant deriveLockTime(Season season) {
        if (season.getFirstMatchStartTime() == null) return null;
        return season.getFirstMatchStartTime().minus(predictionProperties.leagueLockHours(), ChronoUnit.HOURS);
    }

    private Season findSeason(Long id) {
        return seasonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Season", id));
    }
}
