package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Abstract base class for the Person hierarchy.
 *
 * Subtree:
 *  Person
 *   ├── ClubEmployee (abstract)
 *   │     ├── Scout
 *   │     └── Director
 *   └── Player
 *
 * Mapping: JOINED inheritance – each subclass has its own table linked by
 * {@code person_id} FK to {@link #id}.
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "person")
@Getter
@Setter
@NoArgsConstructor
public abstract class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(unique = true, nullable = false)
    private String email;
}
