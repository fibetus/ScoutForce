package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
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
 * Player entity.
 *
 * Wymaganie 2: osoby w systemie dzielą się na pracowników klubu i zawodników
 * (kompletnie i rozłącznie). Player jest podklasą {@link Person}.
 *
 * Dynamic inheritance (University / Professional experience) is modelled
 * using the Strategy pattern: Player "has assigned" exactly one
 * PlayerExperience implementation at a time (UniversityExperience or
 * ProfessionalExperience). The two @OneToOne fields map each concrete
 * type; only one is non-null at any given moment.
 *
 * Domain constructor (from class diagram):
 *   Player(position, status, weight, height, wingspan, initialExperience)
 *
 * Derived attribute:
 *   /averageRating – calculated on-the-fly from scoutingReports, NOT persisted.
 *
 * Associations:
 *   - Player 1 ──* MatchStats        (matchStats)
 *   - Player 1 ──* ScoutingReport    (scoutingReports)
 *   - Player 1 ──* ShootingAnalysis  (shootingAnalyses)
 *   - Player *──1 Club               (club)
 */
@Entity
@Table(name = "player")
@PrimaryKeyJoinColumn(name = "person_id")
@Getter
@Setter
@NoArgsConstructor  // required by JPA
public class Player extends Person {

    // ── Attributes from class diagram ─────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PositionType position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus playerStatus = PlayerStatus.NEW;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Double height;

    @Column(nullable = false)
    private Double wingspan;

    // ── Dynamic inheritance via Strategy (PlayerExperience interface) ─────────
    // At most one of these is non-null at a time.
    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private UniversityExperience universityExperience;

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProfessionalExperience professionalExperience;

    // ── Associations ──────────────────────────────────────────────────────────
    @ManyToOne(optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"players", "employees", "homeMatches", "awayMatches"})
    private Club club;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<MatchStats> matchStats = new ArrayList<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<ScoutingReport> scoutingReports = new ArrayList<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<ShootingAnalysis> shootingAnalyses = new ArrayList<>();

    // ── Domain constructor (from class diagram) ───────────────────────────────

    /**
     * Creates a Player with a given initial experience type.
     *
     * @param position          playing position
     * @param status            initial scouting status
     * @param weight            weight in kg
     * @param height            height in cm
     * @param wingspan          wingspan in cm
     * @param initialExperience either a new UniversityExperience or ProfessionalExperience
     */
    public Player(PositionType position,
                  PlayerStatus status,
                  Double weight,
                  Double height,
                  Double wingspan,
                  PlayerExperience initialExperience) {
        this.position      = position;
        this.playerStatus  = status;
        this.weight        = weight;
        this.height        = height;
        this.wingspan      = wingspan;
        assignExperience(initialExperience);
    }

    // ── Domain methods ────────────────────────────────────────────────────────

    /**
     * Switches the player to University experience (dynamic inheritance).
     * Creates a new UniversityExperience using its domain constructor and
     * removes any existing ProfessionalExperience (orphanRemoval handles DB).
     */
    public void becomeUniversityPlayer(String university, ClassType classType) {
        this.professionalExperience = null;
        UniversityExperience exp = new UniversityExperience(university, classType);
        exp.setPlayer(this);
        this.universityExperience = exp;
    }

    /**
     * Switches the player to Professional experience (dynamic inheritance).
     * Creates a new ProfessionalExperience using its domain constructor and
     * removes any existing UniversityExperience (orphanRemoval handles DB).
     */
    public void becomeProfessionalPlayer(String countryOfOrigin) {
        this.universityExperience = null;
        ProfessionalExperience exp = new ProfessionalExperience(countryOfOrigin);
        exp.setPlayer(this);
        this.professionalExperience = exp;
    }

    /** Updates the player's scouting status. */
    public void changeStatus(PlayerStatus newStatus) {
        this.playerStatus = newStatus;
    }

    /**
     * Returns the currently active PlayerExperience, or null if none is set.
     * Convenience method – the caller can use getExperienceType() on the result.
     */
    @Transient
    public PlayerExperience getExperience() {
        if (universityExperience != null)   return universityExperience;
        if (professionalExperience != null) return professionalExperience;
        return null;
    }

    /**
     * /averageRating – derived attribute (not persisted).
     * Average finalRating across all scouting reports; returns 0 if none.
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

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Sets the correct @OneToOne field based on the runtime type of the
     * provided PlayerExperience implementation.
     */
    private void assignExperience(PlayerExperience experience) {
        if (experience instanceof UniversityExperience ue) {
            ue.setPlayer(this);
            this.universityExperience   = ue;
            this.professionalExperience = null;
        } else if (experience instanceof ProfessionalExperience pe) {
            pe.setPlayer(this);
            this.professionalExperience = pe;
            this.universityExperience   = null;
        } else {
            throw new IllegalArgumentException(
                    "Unknown PlayerExperience implementation: " + experience.getClass());
        }
    }
}
