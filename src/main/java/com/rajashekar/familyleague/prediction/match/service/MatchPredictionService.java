package com.rajashekar.familyleague.prediction.match.service;

import com.rajashekar.familyleague.prediction.match.dto.HeadToHeadResponse;
import com.rajashekar.familyleague.prediction.match.dto.MatchPredictionResponse;
import com.rajashekar.familyleague.prediction.match.dto.SubmitMatchPredictionRequest;
import com.rajashekar.familyleague.user.entity.User;

public interface MatchPredictionService {

    MatchPredictionResponse submit(Long matchId, User user, SubmitMatchPredictionRequest request);

    MatchPredictionResponse getMyPrediction(Long matchId, User user);

    HeadToHeadResponse getHeadToHead(Long matchId);
}
