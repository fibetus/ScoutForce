package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.s30331.ScoutForce.model.enums.RecommendationType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Written evaluation of a {@link Player} by a {@link Scout}, grounded in observed {@link Match}es.
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

    @NotNull
    @Column(nullable = false)
    private LocalDate createdAt;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String note;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecommendationType recommendation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scout_id", nullable = false)
    private Scout createdBy;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @OneToMany(mappedBy = "scoutingReport",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    private List<DetailedRating> detailedRatings = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "report_match",
            joinColumns = @JoinColumn(name = "report_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "match_id", nullable = false)
    )
    private List<Match> basedOnMatches = new ArrayList<>();

    /**
     * Weighted sum of detailed ratings ({@code rating × weight}), scaled to two decimals.
     *
     * @return final score, or {@code 0} when there are no ratings
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
     * Ensures at least one rating exists and weights sum to exactly {@code 1.0}.
     *
     * @throws IllegalStateException when the invariant is violated
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
     * Ensures every match in {@link #basedOnMatches} appears in the scout's watched list.
     *
     * @throws IllegalStateException with E1 message when a match was not observed by the scout
     */
    public void validateMatchesObservedByScout() {
        if (createdBy == null) {
            throw new IllegalStateException("Scouting report must have an authoring scout.");
        }
        Set<Long> watchedMatchIds = createdBy.getWatchedMatches().stream()
                .map(Match::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Match m : basedOnMatches) {
            if (m.getId() == null || !watchedMatchIds.contains(m.getId())) {
                throw new IllegalStateException(
                        "Chosen player has no matches you've observed.");
            }
        }
    }

    /**
     * Ensures every match in {@link #basedOnMatches} is linked to the player via {@link MatchStats}.
     *
     * @throws IllegalStateException with E1 message when the player did not play in a listed match
     */
    public void validateMatchesPlayedByPlayer() {
        if (player == null) {
            throw new IllegalStateException("Scouting report must refer to a player.");
        }
        Set<Long> playerMatchIds = player.getMatchStats().stream()
                .map(MatchStats::getMatch)
                .filter(Objects::nonNull)
                .map(Match::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Match m : basedOnMatches) {
            if (m.getId() == null || !playerMatchIds.contains(m.getId())) {
                throw new IllegalStateException(
                        "Chosen player has no matches you've observed.");
            }
        }
    }
}
