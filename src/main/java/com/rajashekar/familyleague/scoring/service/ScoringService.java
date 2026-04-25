package com.rajashekar.familyleague.scoring.service;

import com.rajashekar.familyleague.prediction.match.entity.MatchPrediction;
import com.rajashekar.familyleague.result.entity.MatchResult;
import com.rajashekar.familyleague.scoring.dto.PointAward;

import java.util.List;

public interface ScoringService {

    List<PointAward> calculate(MatchResult result, List<MatchPrediction> predictions);
}
