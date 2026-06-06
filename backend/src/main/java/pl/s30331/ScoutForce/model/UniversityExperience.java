package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.s30331.ScoutForce.model.enums.ClassType;

@Entity
@Table(name = "university_experience")
@Getter
@Setter
@NoArgsConstructor
public class UniversityExperience implements PlayerExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private Player player;

    @Column(nullable = false)
    private String university;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassType classType;

    public UniversityExperience(String university, ClassType classType) {
        this.university = university;
        this.classType = classType;
    }

    @Override
    public String getExperienceType() {
        return "UNIVERSITY";
    }
}
