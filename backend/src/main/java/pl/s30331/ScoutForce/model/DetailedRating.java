package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Detailed Rating – a single category score within a Scouting Report.
 *
 * COMPOSITION: does not exist without the owning ScoutingReport.
 * Constraints:
 *  - rating  ∈ [1, 10]
 *  - weight  ∈ [0.0, 1.0]
 *  - comment must not be blank
 *
 * Associations:
 *  - DetailedRating *──1 ScoutingReport (scoutingReport)
 */
@Entity
@Table(name = "detailed_rating")
@Getter
@Setter
@NoArgsConstructor
public class DetailedRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Free-text category, e.g. "offense", "defense", "athleticism" */
    @Column(nullable = false)
    private String type;

    @Min(1) @Max(10)
    @Column(nullable = false)
    private BigDecimal rating;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @DecimalMin("0.0") @DecimalMax("1.0")
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal weight;

    @ManyToOne(optional = false)
    @JoinColumn(name = "scouting_report_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private ScoutingReport scoutingReport;

    // ── Domain methods ────────────────────────────────────────────────────────

    /**
     * Validates the input ranges of this Detailed Rating (use case exception E4).
     * Called by the service BEFORE building the aggregate, so we fail fast
     * without touching the database.
     *
     * @throws IllegalStateException if any of:
     *   – {@code type} or {@code comment} is blank,
     *   – {@code rating} is null or outside [1, 10],
     *   – {@code weight} is null or outside [0.0, 1.0].
     */
    public void validateRanges() {
        if (type == null || type.isBlank() || comment == null || comment.isBlank()) {
            throw new IllegalStateException(
                    "Rating must be in 1-10 range. Weight must be in 0.0-1.0 range.");
        }
        if (rating == null
                || rating.compareTo(BigDecimal.ONE) < 0
                || rating.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalStateException(
                    "Rating must be in 1-10 range. Weight must be in 0.0-1.0 range.");
        }
        if (weight == null
                || weight.compareTo(BigDecimal.ZERO) < 0
                || weight.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalStateException(
                    "Rating must be in 1-10 range. Weight must be in 0.0-1.0 range.");
        }
    }
}
