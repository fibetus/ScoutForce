package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.s30331.ScoutForce.model.enums.ClassType;

/**
 * NCAA / university career branch of the {@link PlayerExperience} strategy.
 */
@Entity
@Table(name = "university_experience")
@Getter
@Setter
@NoArgsConstructor
public class UniversityExperience implements PlayerExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private Player player;

    @NotBlank
    @Column(nullable = false)
    private String university;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassType classType;

    /**
     * Creates a university experience row before the owning {@link Player} is linked.
     *
     * @param university institution name
     * @param classType    academic class year
     */
    public UniversityExperience(String university, ClassType classType) {
        this.university = university;
        this.classType = classType;
    }

    /** {@inheritDoc} */
    @Override
    public String getExperienceType() {
        return "UNIVERSITY";
    }
}
