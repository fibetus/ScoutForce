package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Scout – pracownik terenowy obserwujący zawodników na meczach
 * i tworzący raporty skautingowe.
 *
 * Wymaganie 4: dla skauta dodatkowo: unikatowy numer licencji,
 * specjalizacja oraz region obserwacji.
 *
 * Associations:
 *  - Scout 1 ──* ScoutingReport   (createdReports)
 *  - Scout *──* Match             (watchedMatches) – via Delegation
 *  - Scout 1 ──* Delegation       (delegations)
 */
@Entity
@Table(name = "scout")
@PrimaryKeyJoinColumn(name = "person_id")
@Getter
@Setter
@NoArgsConstructor
public class Scout extends ClubEmployee {

    /** {unique} license number */
    @Column(unique = true, nullable = false)
    private String licenseNumber;

    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false)
    private String observationRegion;

    /** Reports authored by this scout */
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<ScoutingReport> createdReports = new ArrayList<>();

    /**
     * Matches this scout has observed (across all delegations).
     * The join table is owned by the Delegation → Match side;
     * here we navigate through delegations in the service layer.
     */
    @ManyToMany
    @JoinTable(
            name = "scout_watched_match",
            joinColumns = @JoinColumn(name = "scout_id"),
            inverseJoinColumns = @JoinColumn(name = "match_id")
    )
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Match> watchedMatches = new ArrayList<>();

    @OneToMany(mappedBy = "scout", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Delegation> delegations = new ArrayList<>();
}
