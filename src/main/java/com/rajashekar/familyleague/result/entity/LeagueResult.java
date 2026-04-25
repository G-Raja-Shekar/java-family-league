package com.rajashekar.familyleague.result.entity;

import com.rajashekar.familyleague.common.audit.AuditableEntity;
import com.rajashekar.familyleague.result.dto.FinalStandingsEntry;
import com.rajashekar.familyleague.season.entity.Season;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "league_results")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class LeagueResult extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false, unique = true)
    private Season season;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "final_standings", columnDefinition = "jsonb")
    private List<FinalStandingsEntry> finalStandings;
}
