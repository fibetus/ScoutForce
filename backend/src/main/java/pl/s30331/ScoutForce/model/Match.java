package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Single game between a {@link #host} club and a {@link #guest} club.
 *
 * <p>May occur during one or more {@link Delegation}s ({@link #delegations}).</p>
 * <p>Constraint: host and guest must be different clubs ({@link #validateBothTeams()}).</p>
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

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    @NotBlank
    @Column(nullable = false)
    private String place;

    @NotNull
    @Column(nullable = false)
    private Integer hostScore;

    @NotNull
    @Column(nullable = false)
    private Integer guestScore;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_club_id", nullable = false)
    private Club host;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_club_id", nullable = false)
    private Club guest;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MatchStats> matchStats = new ArrayList<>();

    @ManyToMany(mappedBy = "watchedMatches", fetch = FetchType.LAZY)
    private List<Scout> observedBy = new ArrayList<>();

    @ManyToMany(mappedBy = "matches", fetch = FetchType.LAZY)
    private List<Delegation> delegations = new ArrayList<>();

    @ManyToMany(mappedBy = "basedOnMatches", fetch = FetchType.LAZY)
    private List<ScoutingReport> basedOnReports = new ArrayList<>();

    /**
     * Validates that host and guest are present and refer to different clubs.
     *
     * @throws IllegalArgumentException when teams are missing or identical
     */
    public void validateBothTeams() {
        if (host == null || guest == null) {
            throw new IllegalArgumentException("Match must have both a host and a guest club.");
        }
        if (host == guest) {
            throw new IllegalArgumentException("Host and guest clubs must be different.");
        }
        if (host.getId() != null && guest.getId() != null && host.getId().equals(guest.getId())) {
            throw new IllegalArgumentException("Host and guest clubs must be different.");
        }
    }

    /**
     * Identity equality by persisted primary key (JPA best practice).
     *
     * @param o other object
     * @return {@code true} when both are {@link Match} instances with the same non-null id
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    /**
     * Constant hash code so managed/detached instances behave consistently in collections.
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
