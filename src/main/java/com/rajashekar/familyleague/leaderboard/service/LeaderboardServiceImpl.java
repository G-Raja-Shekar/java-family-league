package com.rajashekar.familyleague.leaderboard.service;

import com.rajashekar.familyleague.common.exception.ResourceNotFoundException;
import com.rajashekar.familyleague.leaderboard.dto.LeaderboardResponse;
import com.rajashekar.familyleague.leaderboard.entity.LeaderboardEntry;
import com.rajashekar.familyleague.leaderboard.event.LeaderboardRecalculatedEvent;
import com.rajashekar.familyleague.leaderboard.mapper.LeaderboardMapper;
import com.rajashekar.familyleague.leaderboard.repository.LeaderboardEntryRepository;
import com.rajashekar.familyleague.prediction.league.entity.LeaguePrediction;
import com.rajashekar.familyleague.prediction.league.repository.LeaguePredictionRepository;
import com.rajashekar.familyleague.prediction.match.entity.MatchPrediction;
import com.rajashekar.familyleague.prediction.match.repository.MatchPredictionRepository;
import com.rajashekar.familyleague.result.entity.LeagueResult;
import com.rajashekar.familyleague.result.entity.MatchResult;
import com.rajashekar.familyleague.result.event.ResultPublishedEvent;
import com.rajashekar.familyleague.result.repository.LeagueResultRepository;
import com.rajashekar.familyleague.result.repository.MatchResultRepository;
import com.rajashekar.familyleague.scoring.dto.PointAward;
import com.rajashekar.familyleague.scoring.service.ScoringService;
import com.rajashekar.familyleague.season.entity.Season;
import com.rajashekar.familyleague.season.repository.SeasonRepository;
import com.rajashekar.familyleague.user.entity.User;
import com.rajashekar.familyleague.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardEntryRepository leaderboardEntryRepository;
    private final MatchResultRepository matchResultRepository;
    private final LeagueResultRepository leagueResultRepository;
    private final MatchPredictionRepository matchPredictionRepository;
    private final LeaguePredictionRepository leaguePredictionRepository;
    private final SeasonRepository seasonRepository;
    private final UserRepository userRepository;
    private final ScoringService scoringService;
    private final LeaderboardMapper leaderboardMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Async("leaderboard-executor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void handleResultPublished(ResultPublishedEvent event) {
        try {
            if (event.getMatchId() != null) {
                processMatchResult(event.getMatchId(), event.getSeasonId());
            } else {
                processLeagueResult(event.getSeasonId());
            }
            recalculateRanks(event.getSeasonId());
            eventPublisher.publishEvent(new LeaderboardRecalculatedEvent(this, event.getSeasonId()));
            log.info("Leaderboard recalculated for season {}", event.getSeasonId());
        } catch (Exception ex) {
            log.error("Leaderboard recalculation failed for season {}", event.getSeasonId(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaderboardResponse> getSeasonLeaderboard(Long seasonId, Pageable pageable) {
        return leaderboardEntryRepository.findBySeasonIdOrderByRankAsc(seasonId, pageable)
                .map(leaderboardMapper::toResponse);
    }

    private void processMatchResult(Long matchId, Long seasonId) {
        MatchResult result = matchResultRepository.findByMatchId(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("MatchResult", matchId));
        List<MatchPrediction> predictions = matchPredictionRepository.findByMatchId(matchId);
        List<PointAward> awards = scoringService.calculate(result, predictions);
        Season season = findSeason(seasonId);
        upsertEntries(awards, season);
        log.debug("Match scoring applied: matchId={}, {} predictions scored", matchId, awards.size());
    }

    private void processLeagueResult(Long seasonId) {
        LeagueResult result = leagueResultRepository.findBySeasonId(seasonId)
                .orElseThrow(() -> new ResourceNotFoundException("LeagueResult", seasonId));
        List<LeaguePrediction> predictions = leaguePredictionRepository.findBySeasonId(seasonId);
        List<PointAward> awards = scoringService.calculateLeague(result, predictions);
        Season season = findSeason(seasonId);
        upsertEntries(awards, season);
        log.debug("League scoring applied: seasonId={}, {} predictions scored", seasonId, awards.size());
    }

    private void upsertEntries(List<PointAward> awards, Season season) {
        for (PointAward award : awards) {
            LeaderboardEntry entry = leaderboardEntryRepository
                    .findBySeasonIdAndUserId(season.getId(), award.userId())
                    .orElseGet(() -> {
                        LeaderboardEntry e = new LeaderboardEntry();
                        e.setSeason(season);
                        User user = userRepository.findById(award.userId())
                                .orElseThrow(() -> new ResourceNotFoundException("User", award.userId()));
                        e.setUser(user);
                        return e;
                    });
            entry.setTotalPoints(entry.getTotalPoints() + award.points());
            leaderboardEntryRepository.save(entry);
        }
    }

    private void recalculateRanks(Long seasonId) {
        List<LeaderboardEntry> entries =
                leaderboardEntryRepository.findBySeasonIdOrderByTotalPointsDesc(seasonId);

        int rank = 1;
        int prevPoints = -1;
        int prevRank = 1;

        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry entry = entries.get(i);
            if (entry.getTotalPoints() == prevPoints) {
                entry.setRank(prevRank);
            } else {
                prevRank = rank;
                entry.setRank(rank);
                prevPoints = entry.getTotalPoints();
            }
            rank++;
        }
        leaderboardEntryRepository.saveAll(entries);
    }

    private Season findSeason(Long id) {
        return seasonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Season", id));
    }
}
