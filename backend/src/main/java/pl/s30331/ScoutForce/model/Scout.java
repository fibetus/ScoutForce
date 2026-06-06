package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Scout – a field employee who observes players at matches
 * and creates scouting reports.
 */
@Entity
@Table(name = "scout")
@PrimaryKeyJoinColumn(name = "person_id")
@Getter
@Setter
@NoArgsConstructor
public class Scout extends ClubEmployee {

    @NotBlank
    @Column(unique = true, nullable = false)
    private String licenseNumber;

    @NotBlank
    @Column(nullable = false)
    private String specialization;

    @NotBlank
    @Column(nullable = false)
    private String observationRegion;

    /** Director who sent this scout on delegations (owning side FK). */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sent_by_director_id", nullable = false)
    private Director sentByDirector;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScoutingReport> createdReports = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "scout_watched_match",
            joinColumns = @JoinColumn(name = "scout_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "match_id", nullable = false)
    )
    private List<Match> watchedMatches = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "scout_observed_player",
            joinColumns = @JoinColumn(name = "scout_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "player_id", nullable = false)
    )
    private List<Player> observedPlayers = new ArrayList<>();

    @OneToMany(mappedBy = "scout", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Delegation> delegations = new ArrayList<>();
}
