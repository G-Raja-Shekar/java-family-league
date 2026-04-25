package com.rajashekar.familyleague.prediction.league.service;

import com.rajashekar.familyleague.prediction.league.dto.LeagueHeadToHeadResponse;
import com.rajashekar.familyleague.prediction.league.dto.LeaguePredictionResponse;
import com.rajashekar.familyleague.prediction.league.dto.SubmitLeaguePredictionRequest;
import com.rajashekar.familyleague.user.entity.User;

public interface LeaguePredictionService {

    LeaguePredictionResponse submit(Long seasonId, User user, SubmitLeaguePredictionRequest request);

    LeaguePredictionResponse getMyPrediction(Long seasonId, User user);

    LeagueHeadToHeadResponse getHeadToHead(Long seasonId);
}
