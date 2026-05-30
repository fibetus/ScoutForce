package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Match between two clubs.
 *
 * Constraint: host != guest (validated in constructor / service).
 *
 * Associations:
 *  - Match *──1 Club (host)
 *  - Match *──1 Club (guest)
 *  - Match 1──* MatchStats      (matchStats)
 *  - Match *──* Scout           (observedBy) – join table scout_watched_match
 *  - Match *──1 Delegation      (delegation)
 *  - Match *──* ScoutingReport  (basedOnReports) – inverse side
 */
@Entity
@Table(name = "match")
@Getter
@Setter
@NoArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    /** Venue / location */
    @Column(nullable = false)
    private String place;

    @Column(nullable = false)
    private Integer hostScore;

    @Column(nullable = false)
    private Integer guestScore;

    // ── Two-club constraint (host / guest) ────────────────────────────────────
    @ManyToOne(optional = false)
    @JoinColumn(name = "host_club_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"players", "employees", "homeMatches", "awayMatches"})
    private Club host;

    @ManyToOne(optional = false)
    @JoinColumn(name = "guest_club_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"players", "employees", "homeMatches", "awayMatches"})
    private Club guest;

    // ── Statistics for individual players in this match ───────────────────────
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<MatchStats> matchStats = new ArrayList<>();

    // ── Scouts that observed this match ───────────────────────────────────────
    @ManyToMany(mappedBy = "watchedMatches")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Scout> observedBy = new ArrayList<>();

    // ── Delegation that included this match ───────────────────────────────────
    @ManyToOne
    @JoinColumn(name = "delegation_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Delegation delegation;

    // ── Scouting reports based (at least in part) on this match ──────────────
    @ManyToMany(mappedBy = "basedOnMatches")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<ScoutingReport> basedOnReports = new ArrayList<>();

    /**
     * Validates that host and guest are set and are different clubs.
     * Called in service before persisting a Match.
     */
    public void validateBothTeams() {
        if (host == null || guest == null) {
            throw new IllegalArgumentException("Match must have both a host and a guest club.");
        }
        if (host.getId() != null && host.getId().equals(guest.getId())) {
            throw new IllegalArgumentException("Host and guest clubs must be different.");
        }
    }
}
