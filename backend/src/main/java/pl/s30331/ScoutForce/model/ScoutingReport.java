package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.s30331.ScoutForce.model.enums.RecommendationType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Scouting Report created by a Scout for a specific Player.
 *
 * Business rules (enforced before persist in ScoutingReportService):
 *  1. detailedRatings must be non-empty (1..* constraint).
 *  2. Sum of weights of all DetailedRatings must equal exactly 1.0.
 *  3. All basedOnMatches must be in scout.watchedMatches ({subset} constraint).
 *
 * Derived attribute:
 *  /finalRating – weighted average of DetailedRating.rating × weight (not persisted).
 *
 * Associations:
 *  - ScoutingReport *──1 Scout           (createdBy)
 *  - ScoutingReport *──1 Player          (player)
 *  - ScoutingReport 1──* DetailedRating  (detailedRatings) – COMPOSITION
 *  - ScoutingReport *──* Match           (basedOnMatches)
 */
@Entity
@Table(name = "scouting_report")
@Getter
@Setter
@NoArgsConstructor
public class ScoutingReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate createdAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecommendationType recommendation;

    // ── Associations ──────────────────────────────────────────────────────────

    @ManyToOne(optional = false)
    @JoinColumn(name = "scout_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Scout createdBy;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Player player;

    /**
     * COMPOSITION – DetailedRating does not exist without this ScoutingReport.
     * cascade ALL + orphanRemoval ensure child lifecycle is fully managed here.
     */
    @OneToMany(mappedBy = "scoutingReport",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<DetailedRating> detailedRatings = new ArrayList<>();

    /**
     * Matches this report is based on.
     * Must be a {subset} of createdBy.watchedMatches.
     */
    @ManyToMany
    @JoinTable(
            name = "report_match",
            joinColumns = @JoinColumn(name = "report_id"),
            inverseJoinColumns = @JoinColumn(name = "match_id")
    )
    private List<Match> basedOnMatches = new ArrayList<>();

    // ── Domain methods ────────────────────────────────────────────────────────

    /**
     * /finalRating – derived, not persisted.
     * Weighted average: sum(rating_i × weight_i).
     */
    @Transient
    public BigDecimal getFinalRating() {
        if (detailedRatings == null || detailedRatings.isEmpty()) return BigDecimal.ZERO;
        return detailedRatings.stream()
                .map(dr -> dr.getRating().multiply(dr.getWeight()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Validates that the sum of all weights equals exactly 1.0.
     * Called by ScoutingReportService before repository.save().
     *
     * @throws IllegalStateException if sum ≠ 1.0
     */
    public void validateDetailedRatings() {
        if (detailedRatings == null || detailedRatings.isEmpty()) {
            throw new IllegalStateException("Report needs at least one Detailed Rating.");
        }
        BigDecimal weightSum = detailedRatings.stream()
                .map(DetailedRating::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (weightSum.compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalStateException(
                    "Sum of weights need to be exactly 1.0. Current sum: " + weightSum + ".");
        }
    }

    /**
     * Validates the {subset} constraint: every match in basedOnMatches must
     * appear in createdBy.watchedMatches.
     * Called by ScoutingReportService before repository.save().
     *
     * @throws IllegalStateException if any match is not watched by the scout
     */
    public void validateMatchesObservedByScout() {
        List<Match> watched = createdBy.getWatchedMatches();
        for (Match m : basedOnMatches) {
            if (!watched.contains(m)) {
                throw new IllegalStateException(
                        "Chosen player has no matches you've observed.");
            }
        }
    }
}
