package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Professional basketball or college organisation that employs players and hosts matches.
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

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String league;

    @NotBlank
    @Column(nullable = false)
    private String city;

    /** Conference name (optional; used for NBA-style clubs). */
    private String conference;

    /** Division name (optional; used for NBA-style clubs). */
    private String division;

    @OneToMany(mappedBy = "club", fetch = FetchType.LAZY)
    private List<Player> players = new ArrayList<>();

    @OneToMany(mappedBy = "employer", fetch = FetchType.LAZY)
    private List<ClubEmployee> employees = new ArrayList<>();

    @OneToMany(mappedBy = "host", fetch = FetchType.LAZY)
    private List<Match> homeMatches = new ArrayList<>();

    @OneToMany(mappedBy = "guest", fetch = FetchType.LAZY)
    private List<Match> awayMatches = new ArrayList<>();
}
