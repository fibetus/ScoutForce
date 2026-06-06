package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Abstract employee of a {@link Club} — base for {@link Scout} and {@link Director}.
 */
@Entity
@Table(name = "club_employee")
@PrimaryKeyJoinColumn(name = "person_id")
@Getter
@Setter
@NoArgsConstructor
public abstract class ClubEmployee extends Person {

    @NotNull
    @Column(nullable = false)
    private LocalDate hireDate;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyRate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    private Club employer;
}
