package com.rajashekar.familyleague.scoring.service;

import com.rajashekar.familyleague.prediction.match.entity.MatchPrediction;
import com.rajashekar.familyleague.result.entity.MatchResult;
import com.rajashekar.familyleague.scoring.dto.PointAward;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ScoringServiceImpl implements ScoringService {

    @Override
    public List<PointAward> calculate(MatchResult result, List<MatchPrediction> predictions) {
        List<PointAward> awards = new ArrayList<>();
        for (MatchPrediction prediction : predictions) {
            int points = 0;
            List<String> reasons = new ArrayList<>();

            points += scoreWinner(result, prediction, reasons);
            points += scoreTossWinner(result, prediction, reasons);
            points += scorePlayerOfMatch(result, prediction, reasons);

            awards.add(new PointAward(
                    prediction.getUser().getId(),
                    result.getMatch().getId(),
                    points,
                    String.join(", ", reasons)
            ));
        }
        log.debug("Scoring complete for match {}: {} awards", result.getMatch().getId(), awards.size());
        return awards;
    }

    private int scoreWinner(MatchResult result, MatchPrediction prediction, List<String> reasons) {
        if (result.isTie()) {
            if (prediction.isPredictedTie()) {
                reasons.add("Correct tie prediction");
                return 1;
            }
            return 0;
        }
        if (!prediction.isPredictedTie()
                && prediction.getPredictedWinner() != null
                && result.getWinner() != null
                && prediction.getPredictedWinner().getId().equals(result.getWinner().getId())) {
            reasons.add("Correct winner");
            return 1;
        }
        return 0;
    }

    private int scoreTossWinner(MatchResult result, MatchPrediction prediction, List<String> reasons) {
        if (prediction.getPredictedTossWinner() != null
                && result.getTossWinner() != null
                && prediction.getPredictedTossWinner().getId().equals(result.getTossWinner().getId())) {
            reasons.add("Correct toss winner");
            return 1;
        }
        return 0;
    }

    private int scorePlayerOfMatch(MatchResult result, MatchPrediction prediction, List<String> reasons) {
        if (prediction.getPredictedPlayerOfMatch() != null
                && result.getPlayerOfMatch() != null
                && prediction.getPredictedPlayerOfMatch().getId().equals(result.getPlayerOfMatch().getId())) {
            reasons.add("Correct player of match");
            return 1;
        }
        return 0;
    }
}
