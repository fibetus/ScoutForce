package pl.s30331.ScoutForce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Scouting Report created by a Scout for a specific Player.
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scout_id", nullable = false)
    @JsonIgnore
    private Scout createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    @JsonIgnore
    private Player player;

    @OneToMany(mappedBy = "scoutingReport",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    private List<DetailedRating> detailedRatings = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "report_match",
            joinColumns = @JoinColumn(name = "report_id"),
            inverseJoinColumns = @JoinColumn(name = "match_id")
    )
    private List<Match> basedOnMatches = new ArrayList<>();

    @Transient
    public BigDecimal getFinalRating() {
        if (detailedRatings == null || detailedRatings.isEmpty()) return BigDecimal.ZERO;
        return detailedRatings.stream()
                .map(dr -> dr.getRating().multiply(dr.getWeight()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

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

    public void validateMatchesObservedByScout() {
        if (createdBy == null) {
            throw new IllegalStateException("Scouting report must have an authoring scout.");
        }
        List<Match> watched = createdBy.getWatchedMatches();
        for (Match m : basedOnMatches) {
            if (!watched.contains(m)) {
                throw new IllegalStateException(
                        "Chosen player has no matches you've observed.");
            }
        }
    }

    /**
     * Validates that every match in {@code basedOnMatches} has a {@link MatchStats}
     * entry for this report's {@link #player}.
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
