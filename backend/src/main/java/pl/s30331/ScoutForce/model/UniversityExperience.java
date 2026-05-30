package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.s30331.ScoutForce.model.enums.ClassType;

/**
 * University experience – implements PlayerExperience.
 * Composition: does not exist without the owning Player.
 *
 * Constructor: UniversityExperience(university: String, class: ClassType)
 */
@Entity
@Table(name = "university_experience")
@Getter
@Setter
@NoArgsConstructor  // required by JPA
public class UniversityExperience implements PlayerExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Player player;

    @Column(nullable = false)
    private String university;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassType classType;

    /** Domain constructor as defined in the class diagram. */
    public UniversityExperience(String university, ClassType classType) {
        this.university = university;
        this.classType = classType;
    }

    @Override
    public String getExperienceType() {
        return "UNIVERSITY";
    }
}
