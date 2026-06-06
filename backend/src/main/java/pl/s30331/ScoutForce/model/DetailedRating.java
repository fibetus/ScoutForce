package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Single weighted criterion within a {@link ScoutingReport} (e.g. offense, defense).
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

    @NotBlank
    @Column(nullable = false)
    private String type;

    @NotNull
    @Min(1) @Max(10)
    @Column(nullable = false)
    private BigDecimal rating;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @NotNull
    @DecimalMin("0.0") @DecimalMax("1.0")
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal weight;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scouting_report_id", nullable = false)
    private ScoutingReport scoutingReport;

    /**
     * Validates textual fields and numeric ranges before a report is persisted.
     *
     * @throws IllegalStateException when type/comment are blank or rating/weight are out of range
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
