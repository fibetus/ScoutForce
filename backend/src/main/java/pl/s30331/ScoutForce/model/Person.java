package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Abstract base class for the Person hierarchy.
 *
 * <p>Subclasses use {@link PrimaryKeyJoinColumn} (JOINED inheritance), not
 * {@link JoinColumn}, to link their table to {@link #id}.</p>
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

    @NotBlank
    @Column(nullable = false)
    private String firstName;

    @NotBlank
    @Column(nullable = false)
    private String lastName;

    @NotNull
    @Column(nullable = false)
    private LocalDate birthDate;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;
}
