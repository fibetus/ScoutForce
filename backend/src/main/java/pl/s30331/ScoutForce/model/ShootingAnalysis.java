package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Per-zone, per-season shooting breakdown for a {@link Player},
 * derived from a single {@link MatchStats} row.
 */
@Entity
@Table(name = "shooting_analysis",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_player_season_range",
                columnNames = {"player_id", "season", "range_label"}))
@Getter
@Setter
@NoArgsConstructor
public class ShootingAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_stats_id", nullable = false)
    private MatchStats basedOnMatchStats;

    @NotBlank
    @Column(nullable = false)
    private String season;

    @NotBlank
    @Column(name = "range_label", nullable = false)
    private String range;

    @NotNull
    @Column(nullable = false)
    private Integer shotsAttempted;

    @NotNull
    @Column(nullable = false)
    private Integer shotsMade;

    /**
     * Computes make percentage for this zone/season slice.
     *
     * @return percentage 0–100 with two decimal places, or {@code 0} when no attempts
     */
    @Transient
    public BigDecimal getPercentage() {
        if (shotsMade == null || shotsAttempted == null || shotsAttempted == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(shotsMade)
                .divide(BigDecimal.valueOf(shotsAttempted), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Ensures {@link #basedOnMatchStats} belongs to the same {@link #player}.
     *
     * @throws IllegalStateException when the match-stats player differs from the analysis player
     */
    @PrePersist
    @PreUpdate
    private void validateBasedOnMatchStats() {
        if (player == null || basedOnMatchStats == null) {
            return;
        }
        Player statsPlayer = basedOnMatchStats.getPlayer();
        if (statsPlayer == null) {
            throw new IllegalStateException(
                    "Shooting analysis must be based on match stats that refer to a player.");
        }
        Long playerId = player.getId();
        Long statsPlayerId = statsPlayer.getId();
        if (playerId == null || statsPlayerId == null || !Objects.equals(playerId, statsPlayerId)) {
            throw new IllegalStateException(
                    "Shooting analysis must be based on match stats of the same player.");
        }
    }
}
