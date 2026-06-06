package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "club_employee")
@PrimaryKeyJoinColumn(name = "person_id")
@Getter
@Setter
@NoArgsConstructor
public abstract class ClubEmployee extends Person {

    @Column(nullable = false)
    private LocalDate hireDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyRate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    private Club employer;
}
