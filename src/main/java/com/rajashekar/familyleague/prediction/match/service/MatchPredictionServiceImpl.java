package com.rajashekar.familyleague.prediction.match.service;

import com.rajashekar.familyleague.common.exception.PredictionLockedException;
import com.rajashekar.familyleague.common.exception.ResourceNotFoundException;
import com.rajashekar.familyleague.common.exception.SeasonClosedException;
import com.rajashekar.familyleague.common.exception.ValidationException;
import com.rajashekar.familyleague.match.entity.Match;
import com.rajashekar.familyleague.match.repository.MatchRepository;
import com.rajashekar.familyleague.player.entity.Player;
import com.rajashekar.familyleague.player.repository.PlayerRepository;
import com.rajashekar.familyleague.prediction.config.PredictionProperties;
import com.rajashekar.familyleague.prediction.match.dto.HeadToHeadResponse;
import com.rajashekar.familyleague.prediction.match.dto.MatchPredictionResponse;
import com.rajashekar.familyleague.prediction.match.dto.SubmitMatchPredictionRequest;
import com.rajashekar.familyleague.prediction.match.entity.MatchPrediction;
import com.rajashekar.familyleague.prediction.match.mapper.MatchPredictionMapper;
import com.rajashekar.familyleague.prediction.match.repository.MatchPredictionRepository;
import com.rajashekar.familyleague.season.entity.SeasonStatus;
import com.rajashekar.familyleague.team.entity.Team;
import com.rajashekar.familyleague.team.repository.TeamRepository;
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
public class MatchPredictionServiceImpl implements MatchPredictionService {

    private final MatchPredictionRepository matchPredictionRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final MatchPredictionMapper mapper;
    private final PredictionProperties predictionProperties;

    @Override
    @Transactional
    public MatchPredictionResponse submit(Long matchId, User user, SubmitMatchPredictionRequest request) {
        Match match = findMatch(matchId);

        if (match.getSeason().getStatus() == SeasonStatus.CLOSED) {
            throw new SeasonClosedException("Season is closed — predictions are no longer accepted");
        }

        Instant lockTime = deriveLockTime(match);
        if (!Instant.now().isBefore(lockTime)) {
            throw new PredictionLockedException(
                    "Prediction window is closed. Lock time was " + lockTime);
        }

        MatchPrediction prediction = matchPredictionRepository
                .findByMatchIdAndUserId(matchId, user.getId())
                .orElse(new MatchPrediction());

        prediction.setMatch(match);
        prediction.setUser(user);
        prediction.setPredictedWinner(resolveTeam(request.predictedWinnerId(), match));
        prediction.setPredictedTossWinner(resolveTeam(request.predictedTossWinnerId(), match));
        prediction.setPredictedPlayerOfMatch(resolvePlayer(request.predictedPlayerOfMatchId()));

        MatchPrediction saved = matchPredictionRepository.save(prediction);
        log.info("Match prediction upserted: match={} user={}", matchId, user.getId());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MatchPredictionResponse getMyPrediction(Long matchId, User user) {
        findMatch(matchId);
        return matchPredictionRepository.findByMatchIdAndUserId(matchId, user.getId())
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("MatchPrediction",
                        "match=" + matchId + ",user=" + user.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public HeadToHeadResponse getHeadToHead(Long matchId) {
        Match match = findMatch(matchId);
        Instant lockTime = deriveLockTime(match);

        if (Instant.now().isBefore(lockTime)) {
            throw new PredictionLockedException(
                    "Head-to-head predictions are only visible after the lock time: " + lockTime);
        }

        List<MatchPredictionResponse> predictions = matchPredictionRepository.findByMatchId(matchId)
                .stream().map(mapper::toResponse).toList();
        return new HeadToHeadResponse(matchId, lockTime, predictions);
    }

    private Instant deriveLockTime(Match match) {
        return match.getStartTime().minus(predictionProperties.matchLockHours(), ChronoUnit.HOURS);
    }

    private Match findMatch(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match", matchId));
    }

    private Team resolveTeam(Long teamId, Match match) {
        if (teamId == null) return null;
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));
        boolean isMatchTeam = team.getId().equals(match.getHomeTeam().getId())
                || team.getId().equals(match.getAwayTeam().getId());
        if (!isMatchTeam) {
            throw new ValidationException("Team " + teamId + " is not part of this match");
        }
        return team;
    }

    private Player resolvePlayer(Long playerId) {
        if (playerId == null) return null;
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", playerId));
    }
}
