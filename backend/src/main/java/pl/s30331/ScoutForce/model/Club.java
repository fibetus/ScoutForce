package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Club (basketball team / organisation).
 *
 * Wymaganie 6: dla każdego klubu pamiętamy nazwę, miasto oraz ligę;
 * dla niektórych klubów (NBA) dodatkowo konferencję i dywizję.
 *
 * Associations:
 *  - Club 1 ──* Player        (players)
 *  - Club 1 ──* ClubEmployee  (employees) – Scouts and Directors
 *  - Club 1 ──* Match         (homeMatches – as host)
 *  - Club 1 ──* Match         (awayMatches – as guest)
 */
@Entity
@Table(name = "club")
@Getter
@Setter
@NoArgsConstructor
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String league;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    /** Optional – populated only for NBA clubs. */
    private String conference;

    /** Optional – populated only for NBA clubs. */
    private String division;

    @OneToMany(mappedBy = "club")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Player> players = new ArrayList<>();

    @OneToMany(mappedBy = "employer")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<ClubEmployee> employees = new ArrayList<>();

    @OneToMany(mappedBy = "host")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Match> homeMatches = new ArrayList<>();

    @OneToMany(mappedBy = "guest")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Match> awayMatches = new ArrayList<>();
}
