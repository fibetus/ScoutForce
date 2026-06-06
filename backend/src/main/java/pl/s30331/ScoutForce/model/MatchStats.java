package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

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
