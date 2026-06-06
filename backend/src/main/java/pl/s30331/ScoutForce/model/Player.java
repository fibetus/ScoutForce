package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
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
 * Player entity with dynamic University / Professional experience (Strategy pattern).
 */
@Entity
@Table(name = "player")
@PrimaryKeyJoinColumn(name = "person_id")
@Getter
@Setter
@NoArgsConstructor
public class Player extends Person {

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

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Setter(AccessLevel.NONE)
    private UniversityExperience universityExperience;

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Setter(AccessLevel.NONE)
    private ProfessionalExperience professionalExperience;

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

    public Player(PositionType position,
                  PlayerStatus status,
                  Double weight,
                  Double height,
                  Double wingspan,
                  PlayerExperience initialExperience) {
        this.position     = position;
        this.playerStatus = status;
        this.weight       = weight;
        this.height       = height;
        this.wingspan     = wingspan;
        assignExperience(initialExperience);
    }

    public void becomeUniversityPlayer(String university, ClassType classType) {
        this.professionalExperience = null;
        UniversityExperience exp = new UniversityExperience(university, classType);
        exp.setPlayer(this);
        this.universityExperience = exp;
    }

    public void becomeProfessionalPlayer(String countryOfOrigin) {
        this.universityExperience = null;
        ProfessionalExperience exp = new ProfessionalExperience(countryOfOrigin);
        exp.setPlayer(this);
        this.professionalExperience = exp;
    }

    public void changeStatus(PlayerStatus newStatus) {
        this.playerStatus = newStatus;
    }

    @Transient
    public PlayerExperience getExperience() {
        if (universityExperience != null)   return universityExperience;
        if (professionalExperience != null) return professionalExperience;
        return null;
    }

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

    @PrePersist
    @PreUpdate
    private void validateSingleExperience() {
        if (universityExperience != null && professionalExperience != null) {
            throw new IllegalStateException(
                    "Player cannot have both University and Professional experience.");
        }
    }

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
