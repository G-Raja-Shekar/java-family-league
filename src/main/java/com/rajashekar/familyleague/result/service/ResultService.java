package com.rajashekar.familyleague.result.service;

import com.rajashekar.familyleague.result.dto.LeagueResultResponse;
import com.rajashekar.familyleague.result.dto.MatchResultResponse;
import com.rajashekar.familyleague.result.dto.PublishLeagueResultRequest;
import com.rajashekar.familyleague.result.dto.PublishMatchResultRequest;

public interface ResultService {

    MatchResultResponse publishMatchResult(Long matchId, PublishMatchResultRequest request);

    LeagueResultResponse publishLeagueResult(Long seasonId, PublishLeagueResultRequest request);
}
