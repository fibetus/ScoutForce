package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Professional (non-NCAA) career branch of the {@link PlayerExperience} strategy.
 */
@Entity
@Table(name = "professional_experience")
@Getter
@Setter
@NoArgsConstructor
public class ProfessionalExperience implements PlayerExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private Player player;

    @NotBlank
    @Column(nullable = false)
    private String countryOfOrigin;

    /**
     * @param countryOfOrigin player's country of origin for pro scouting records
     */
    public ProfessionalExperience(String countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
    }

    /** {@inheritDoc} */
    @Override
    public String getExperienceType() {
        return "PROFESSIONAL";
    }
}
