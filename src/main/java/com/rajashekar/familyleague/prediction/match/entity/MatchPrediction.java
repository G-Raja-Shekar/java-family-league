package com.rajashekar.familyleague.prediction.match.entity;

import com.rajashekar.familyleague.common.audit.AuditableEntity;
import com.rajashekar.familyleague.match.entity.Match;
import com.rajashekar.familyleague.player.entity.Player;
import com.rajashekar.familyleague.team.entity.Team;
import com.rajashekar.familyleague.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "match_predictions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"match_id", "user_id"}))
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class MatchPrediction extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predicted_winner_id")
    private Team predictedWinner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predicted_toss_winner_id")
    private Team predictedTossWinner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predicted_player_of_match_id")
    private Player predictedPlayerOfMatch;

    @Column(name = "predicted_tie", nullable = false)
    private boolean predictedTie = false;
}
