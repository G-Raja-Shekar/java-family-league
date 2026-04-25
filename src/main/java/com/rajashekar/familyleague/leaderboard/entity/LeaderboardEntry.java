package com.rajashekar.familyleague.leaderboard.entity;

import com.rajashekar.familyleague.common.audit.AuditableEntity;
import com.rajashekar.familyleague.season.entity.Season;
import com.rajashekar.familyleague.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "leaderboard_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"season_id", "user_id"}))
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class LeaderboardEntry extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_points", nullable = false)
    private int totalPoints = 0;

    @Column(name = "rank", nullable = false)
    private int rank = 0;
}
