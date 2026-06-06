package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Box-score line for one {@link Player} in one {@link Match}.
 *
 * <p>Uniqueness constraint: at most one stats row per (player, match) pair.</p>
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

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @NotNull
    @Column(nullable = false)
    private Integer minutesPlayed;

    @NotNull
    @Column(nullable = false)
    private Integer points;

    @NotNull
    @Column(nullable = false)
    private Integer rebounds;

    @NotNull
    @Column(nullable = false)
    private Integer assists;

    @NotNull
    @Column(nullable = false)
    private Integer steals;

    @NotNull
    @Column(nullable = false)
    private Integer blocks;

    @NotNull
    @Column(nullable = false)
    private Integer turnovers;

    @NotNull
    @Column(nullable = false)
    private Integer fouls;

    @NotNull
    @Column(nullable = false)
    private Integer fieldGoalsMade;

    @NotNull
    @Column(nullable = false)
    private Integer fieldGoalsAttempted;

    @NotNull
    @Column(nullable = false)
    private Integer threePointersMade;

    @NotNull
    @Column(nullable = false)
    private Integer threePointersAttempted;

    @NotNull
    @Column(nullable = false)
    private Integer freeThrowsMade;

    @NotNull
    @Column(nullable = false)
    private Integer freeThrowsAttempted;

    @OneToMany(mappedBy = "basedOnMatchStats", fetch = FetchType.LAZY)
    private List<ShootingAnalysis> shootingAnalyses = new ArrayList<>();

    /** @return field-goal percentage (0–100), or {@code 0} when no attempts */
    @Transient
    public BigDecimal getFieldGoalPercentage() {
        return safePercentage(fieldGoalsMade, fieldGoalsAttempted);
    }

    /** @return three-point percentage (0–100), or {@code 0} when no attempts */
    @Transient
    public BigDecimal getThreePointPercentage() {
        return safePercentage(threePointersMade, threePointersAttempted);
    }

    /** @return free-throw percentage (0–100), or {@code 0} when no attempts */
    @Transient
    public BigDecimal getFreeThrowPercentage() {
        return safePercentage(freeThrowsMade, freeThrowsAttempted);
    }

    /**
     * Shared helper for shooting percentage columns.
     *
     * @param made      shots made
     * @param attempted shots attempted
     * @return percentage scaled to two decimals
     */
    private BigDecimal safePercentage(Integer made, Integer attempted) {
        if (made == null || attempted == null || attempted == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(made)
                .divide(BigDecimal.valueOf(attempted), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
