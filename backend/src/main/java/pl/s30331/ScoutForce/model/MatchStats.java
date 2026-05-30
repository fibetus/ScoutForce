package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Association class between Player and Match.
 * Represents a single player's performance in a single match.
 *
 * Derived attributes (not persisted):
 *  /fieldGoalPercentage, /threePointPercentage, /freeThrowPercentage
 *
 * Associations:
 *  - MatchStats *──1 Player
 *  - MatchStats *──1 Match
 */
@Entity
@Table(name = "match_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "match_id"}))
@Getter
@Setter
@NoArgsConstructor
public class MatchStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Player player;

    @ManyToOne(optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Match match;

    // ── Box-score statistics ──────────────────────────────────────────────────
    @Column(nullable = false)
    private Integer minutesPlayed;

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false)
    private Integer rebounds;

    @Column(nullable = false)
    private Integer assists;

    @Column(nullable = false)
    private Integer steals;

    @Column(nullable = false)
    private Integer blocks;

    @Column(nullable = false)
    private Integer turnovers;

    @Column(nullable = false)
    private Integer fouls;

    // ── Shooting (used for derived percentages) ───────────────────────────────
    @Column(nullable = false)
    private Integer fieldGoalsMade;

    @Column(nullable = false)
    private Integer fieldGoalsAttempted;

    @Column(nullable = false)
    private Integer threePointersMade;

    @Column(nullable = false)
    private Integer threePointersAttempted;

    @Column(nullable = false)
    private Integer freeThrowsMade;

    @Column(nullable = false)
    private Integer freeThrowsAttempted;

    // ── Derived attributes ────────────────────────────────────────────────────

    @Transient
    public BigDecimal getFieldGoalPercentage() {
        return safePercentage(fieldGoalsMade, fieldGoalsAttempted);
    }

    @Transient
    public BigDecimal getThreePointPercentage() {
        return safePercentage(threePointersMade, threePointersAttempted);
    }

    @Transient
    public BigDecimal getFreeThrowPercentage() {
        return safePercentage(freeThrowsMade, freeThrowsAttempted);
    }

    private BigDecimal safePercentage(Integer made, Integer attempted) {
        if (made == null || attempted == null || attempted == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(made)
                .divide(BigDecimal.valueOf(attempted), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
