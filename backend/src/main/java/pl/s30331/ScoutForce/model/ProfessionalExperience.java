package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Professional experience – implements PlayerExperience.
 * Composition: does not exist without the owning Player.
 *
 * Constructor: ProfessionalExperience(country: String)
 */
@Entity
@Table(name = "professional_experience")
@Getter
@Setter
@NoArgsConstructor  // required by JPA
public class ProfessionalExperience implements PlayerExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Player player;

    @Column(nullable = false)
    private String countryOfOrigin;

    /** Domain constructor as defined in the class diagram. */
    public ProfessionalExperience(String countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
    }

    @Override
    public String getExperienceType() {
        return "PROFESSIONAL";
    }
}
