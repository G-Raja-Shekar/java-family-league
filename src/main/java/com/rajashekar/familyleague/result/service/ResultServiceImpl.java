package com.rajashekar.familyleague.result.service;

import com.rajashekar.familyleague.common.exception.ResourceNotFoundException;
import com.rajashekar.familyleague.common.exception.SeasonClosedException;
import com.rajashekar.familyleague.common.exception.ValidationException;
import com.rajashekar.familyleague.match.entity.Match;
import com.rajashekar.familyleague.match.repository.MatchRepository;
import com.rajashekar.familyleague.player.entity.Player;
import com.rajashekar.familyleague.player.repository.PlayerRepository;
import com.rajashekar.familyleague.result.dto.LeagueResultResponse;
import com.rajashekar.familyleague.result.dto.MatchResultResponse;
import com.rajashekar.familyleague.result.dto.PublishLeagueResultRequest;
import com.rajashekar.familyleague.result.dto.PublishMatchResultRequest;
import com.rajashekar.familyleague.result.entity.LeagueResult;
import com.rajashekar.familyleague.result.entity.MatchResult;
import com.rajashekar.familyleague.result.event.ResultPublishedEvent;
import com.rajashekar.familyleague.result.mapper.ResultMapper;
import com.rajashekar.familyleague.result.repository.LeagueResultRepository;
import com.rajashekar.familyleague.result.repository.MatchResultRepository;
import com.rajashekar.familyleague.season.entity.Season;
import com.rajashekar.familyleague.season.entity.SeasonStatus;
import com.rajashekar.familyleague.season.repository.SeasonRepository;
import com.rajashekar.familyleague.team.entity.Team;
import com.rajashekar.familyleague.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultServiceImpl implements ResultService {

    private final MatchResultRepository matchResultRepository;
    private final LeagueResultRepository leagueResultRepository;
    private final MatchRepository matchRepository;
    private final SeasonRepository seasonRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final ResultMapper resultMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public MatchResultResponse publishMatchResult(Long matchId, PublishMatchResultRequest request) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match", matchId));

        Season season = match.getSeason();
        if (season.getStatus() == SeasonStatus.CLOSED) {
            throw new SeasonClosedException("Season is closed — results cannot be published");
        }
        if (matchResultRepository.existsByMatchId(matchId)) {
            throw new ValidationException("Result already published for match " + matchId);
        }

        MatchResult result = new MatchResult();
        result.setMatch(match);
        result.setTie(request.tie());

        if (!request.tie() && request.winnerId() != null) {
            result.setWinner(findTeam(request.winnerId()));
        }
        if (request.tossWinnerId() != null) {
            result.setTossWinner(findTeam(request.tossWinnerId()));
        }
        if (request.playerOfMatchId() != null) {
            result.setPlayerOfMatch(findPlayer(request.playerOfMatchId()));
        }

        MatchResult saved = matchResultRepository.save(result);
        log.info("Match result published: matchId={}", matchId);

        eventPublisher.publishEvent(new ResultPublishedEvent(this, matchId, season.getId()));
        return resultMapper.toMatchResponse(saved);
    }

    @Override
    @Transactional
    public LeagueResultResponse publishLeagueResult(Long seasonId, PublishLeagueResultRequest request) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new ResourceNotFoundException("Season", seasonId));

        if (season.getStatus() == SeasonStatus.CLOSED) {
            throw new SeasonClosedException("Season is closed — results cannot be published");
        }
        if (leagueResultRepository.existsBySeasonId(seasonId)) {
            throw new ValidationException("Result already published for season " + seasonId);
        }

        LeagueResult result = new LeagueResult();
        result.setSeason(season);
        result.setFinalStandings(request.finalStandings());

        LeagueResult saved = leagueResultRepository.save(result);
        log.info("League result published: seasonId={}", seasonId);

        eventPublisher.publishEvent(new ResultPublishedEvent(this, null, seasonId));
        return resultMapper.toLeagueResponse(saved);
    }

    private Team findTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
    }

    private Player findPlayer(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", id));
    }
}
