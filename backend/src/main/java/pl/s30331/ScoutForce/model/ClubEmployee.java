package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Abstract club employee – concrete subclasses are {@link Scout} and {@link Director}.
 *
 * Wymaganie 3: pracownicy klubu dzielą się m.in. na dyrektorów i skautów,
 * podział kompletny i rozłączny, ale otwarty na przyszłe typy.
 *
 * Wymaganie 7: każdy pracownik klubu jest zatrudniony w dokładnie jednym klubie.
 *
 * Attributes:
 *  – hireDate
 *  – dailyRate (dzienna stawka wynagrodzenia)
 *
 * Associations:
 *  – ClubEmployee *──1 Club (employer)
 */
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Club employer;
}
