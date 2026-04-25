package com.rajashekar.familyleague.result.entity;

import com.rajashekar.familyleague.common.audit.AuditableEntity;
import com.rajashekar.familyleague.match.entity.Match;
import com.rajashekar.familyleague.player.entity.Player;
import com.rajashekar.familyleague.team.entity.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "match_results")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class MatchResult extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private Team winner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toss_winner_id")
    private Team tossWinner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_of_match_id")
    private Player playerOfMatch;

    @Column(name = "is_tie", nullable = false)
    private boolean tie = false;
}
