package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Shooting Analysis – per-zone, per-season shooting record for a {@link Player}.
 *
 *
 * Constraints:
 *  – {@code (player_id, season, range)} is unique – each (range, season) pair
 *    has exactly one ShootingAnalysis per player. The same {@code range}
 *    label may reappear in another season for the same player.
 *
 * Range is a free-text label (e.g. "5m", "10m", "from the logo") so scouts
 * can define their own zones; uniqueness is scoped to player + season.
 *
 * Derived attribute:
 *  /percentage – shotsMade / shotsAttempted, not persisted.
 *
 * Associations:
 *  - ShootingAnalysis *──1 Player
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Player player;

    @Column(nullable = false)
    private String season;

    /** Free-text shooting zone label (e.g. "5m", "10m", "from the logo"). */
    @Column(name = "range_label", nullable = false)
    private String range;

    @Column(nullable = false)
    private Integer shotsAttempted;

    @Column(nullable = false)
    private Integer shotsMade;

    /**
     * /percentage – derived attribute, not persisted.
     * Implementation intentionally left as a stub – not part of the
     * Create Scouting Report use case.
     */
    @Transient
    public BigDecimal getPercentage() {
        // TODO: implement when needed
        throw new UnsupportedOperationException("Not yet implemented.");
    }
}
