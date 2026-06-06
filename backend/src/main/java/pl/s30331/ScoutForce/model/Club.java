package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

    private String conference;

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
