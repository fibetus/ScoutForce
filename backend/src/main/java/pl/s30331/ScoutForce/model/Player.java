package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.s30331.ScoutForce.model.enums.ClassType;
import pl.s30331.ScoutForce.model.enums.PlayerStatus;
import pl.s30331.ScoutForce.model.enums.PositionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Basketball player observed and evaluated by scouts.
 *
 * <p>Uses the Strategy pattern for {@link PlayerExperience}: at most one of
 * {@link UniversityExperience} or {@link ProfessionalExperience} may be active.
 * Experience fields use hand-written setters ({@code @Setter(AccessLevel.NONE)} blocks Lombok).</p>
 */
@Entity
@Table(name = "player")
@PrimaryKeyJoinColumn(name = "person_id")
@Getter
@Setter
@NoArgsConstructor
public class Player extends Person {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PositionType position;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus playerStatus = PlayerStatus.NEW;

    @NotNull
    @Column(nullable = false)
    private Double weight;

    @NotNull
    @Column(nullable = false)
    private Double height;

    @NotNull
    @Column(nullable = false)
    private Double wingspan;

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Setter(AccessLevel.NONE)
    private UniversityExperience universityExperience;

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Setter(AccessLevel.NONE)
    private ProfessionalExperience professionalExperience;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MatchStats> matchStats = new ArrayList<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScoutingReport> scoutingReports = new ArrayList<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ShootingAnalysis> shootingAnalyses = new ArrayList<>();

    @ManyToMany(mappedBy = "observedPlayers", fetch = FetchType.LAZY)
    private List<Scout> observingScouts = new ArrayList<>();

    /**
     * Convenience constructor that assigns physical attributes and initial experience.
     *
     * @param position          playing position
     * @param status            initial scouting status
     * @param weight            weight in kilograms
     * @param height            height in centimetres
     * @param wingspan          wingspan in centimetres
     * @param initialExperience first (and only) experience strategy instance
     */
    public Player(PositionType position,
                  PlayerStatus status,
                  Double weight,
                  Double height,
                  Double wingspan,
                  PlayerExperience initialExperience) {
        if (initialExperience == null) {
            throw new IllegalArgumentException("Initial PlayerExperience must not be null.");
        }
        this.position     = position;
        this.playerStatus = status;
        this.weight       = weight;
        this.height       = height;
        this.wingspan     = wingspan;
        assignExperience(initialExperience);
    }

    /**
     * Replaces any professional experience with a university career.
     *
     * @param university name of the institution
     * @param classType  NCAA class year
     */
    public void becomeUniversityPlayer(String university, ClassType classType) {
        setProfessionalExperience(null);
        setUniversityExperience(new UniversityExperience(university, classType));
    }

    /**
     * Replaces any university experience with a professional career.
     *
     * @param countryOfOrigin player's country of origin for pro tracking
     */
    public void becomeProfessionalPlayer(String countryOfOrigin) {
        setUniversityExperience(null);
        setProfessionalExperience(new ProfessionalExperience(countryOfOrigin));
    }

    /**
     * Updates the scouting pipeline status.
     *
     * @param newStatus target status from the {@link pl.s30331.ScoutForce.model.enums.PlayerStatus} enum
     */
    public void changeStatus(PlayerStatus newStatus) {
        this.playerStatus = newStatus;
    }

    /**
     * Assigns university experience; enforces disjointness with professional experience.
     *
     * @param experience new university experience, or {@code null} to clear
     * @throws IllegalStateException when both experience types would be non-null
     */
    public void setUniversityExperience(UniversityExperience experience) {
        if (experience != null && professionalExperience != null) {
            throw new IllegalStateException(
                    "Player cannot have both University and Professional experience.");
        }
        this.universityExperience = experience;
        if (experience != null) {
            experience.setPlayer(this);
        }
    }

    /**
     * Assigns professional experience; enforces disjointness with university experience.
     *
     * @param experience new professional experience, or {@code null} to clear
     * @throws IllegalStateException when both experience types would be non-null
     */
    public void setProfessionalExperience(ProfessionalExperience experience) {
        if (experience != null && universityExperience != null) {
            throw new IllegalStateException(
                    "Player cannot have both University and Professional experience.");
        }
        this.professionalExperience = experience;
        if (experience != null) {
            experience.setPlayer(this);
        }
    }

    /**
     * Returns the active experience strategy, or {@code null} when none is set.
     *
     * @return {@link UniversityExperience}, {@link ProfessionalExperience}, or {@code null}
     */
    @Transient
    public PlayerExperience getExperience() {
        if (universityExperience != null)   return universityExperience;
        if (professionalExperience != null) return professionalExperience;
        return null;
    }

    /**
     * Computes the arithmetic mean of {@link ScoutingReport#getFinalRating()} values.
     *
     * @return average rating rounded to two decimals, or {@code 0} when there are no reports
     */
    @Transient
    public BigDecimal getAverageRating() {
        if (scoutingReports == null || scoutingReports.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = scoutingReports.stream()
                .map(ScoutingReport::getFinalRating)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(scoutingReports.size()),
                2, RoundingMode.HALF_UP);
    }

    /**
     * JPA lifecycle guard — rejects persist/update when both experience slots are filled.
     */
    @PrePersist
    @PreUpdate
    private void validateSingleExperience() {
        if (universityExperience != null && professionalExperience != null) {
            throw new IllegalStateException(
                    "Player cannot have both University and Professional experience.");
        }
    }

    /**
     * Routes a {@link PlayerExperience} implementation to the correct setter.
     *
     * @param experience concrete university or professional experience
     * @throws IllegalArgumentException for unknown implementations
     */
    private void assignExperience(PlayerExperience experience) {
        if (experience == null) {
            throw new IllegalArgumentException("Initial PlayerExperience must not be null.");
        }
        if (experience instanceof UniversityExperience ue) {
            setUniversityExperience(ue);
        } else if (experience instanceof ProfessionalExperience pe) {
            setProfessionalExperience(pe);
        } else {
            throw new IllegalArgumentException(
                    "Unknown PlayerExperience implementation: " + experience.getClass());
        }
    }
}
