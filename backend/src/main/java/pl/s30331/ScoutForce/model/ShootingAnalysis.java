package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Shooting Analysis – per-zone, per-season shooting record for a {@link Player}.
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private String season;

    @Column(name = "range_label", nullable = false)
    private String range;

    @Column(nullable = false)
    private Integer shotsAttempted;

    @Column(nullable = false)
    private Integer shotsMade;

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
}
