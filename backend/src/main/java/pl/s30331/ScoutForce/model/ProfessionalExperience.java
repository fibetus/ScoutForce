package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "professional_experience")
@Getter
@Setter
@NoArgsConstructor
public class ProfessionalExperience implements PlayerExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private Player player;

    @Column(nullable = false)
    private String countryOfOrigin;

    public ProfessionalExperience(String countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
    }

    @Override
    public String getExperienceType() {
        return "PROFESSIONAL";
    }
}
