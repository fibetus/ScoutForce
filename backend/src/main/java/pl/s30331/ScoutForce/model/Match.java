package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Match between two clubs.
 *
 * Constraint: host != guest (validated before persist).
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

    @Column(nullable = false)
    private String place;

    @Column(nullable = false)
    private Integer hostScore;

    @Column(nullable = false)
    private Integer guestScore;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_club_id", nullable = false)
    private Club host;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_club_id", nullable = false)
    private Club guest;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MatchStats> matchStats = new ArrayList<>();

    @ManyToMany(mappedBy = "watchedMatches", fetch = FetchType.LAZY)
    private List<Scout> observedBy = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delegation_id", nullable = false)
    private Delegation delegation;

    @ManyToMany(mappedBy = "basedOnMatches", fetch = FetchType.LAZY)
    private List<ScoutingReport> basedOnReports = new ArrayList<>();

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
