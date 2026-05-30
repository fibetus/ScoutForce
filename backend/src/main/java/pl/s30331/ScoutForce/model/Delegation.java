package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.s30331.ScoutForce.model.enums.DelegationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Delegation – a scouting trip assigned to a Scout that includes one or more Matches.
 *
 * Wymaganie 20: każda delegacja jest organizowana dla dokładnie jednego skauta
 * i utworzona przez dokładnie jednego dyrektora klubu. Pamiętamy: nazwę,
 * datę rozpoczęcia, datę zakończenia oraz status.
 *
 * Class-level (static) attributes:
 *  DAILY_LIVING_COST = 250 PLN/day  (wymaganie 22)
 *  FLIGHT_COST       = 1500 PLN     (wymaganie 23)
 *
 * Derived attribute:
 *  /cost – calculated on-the-fly from trip duration and constants (not persisted).
 *
 * Associations:
 *  - Delegation *──1 Scout    (scout)
 *  - Delegation *──1 Director (createdBy)
 *  - Delegation 1──* Match    (matches)
 */
@Entity
@Table(name = "delegation")
@Getter
@Setter
@NoArgsConstructor
public class Delegation {

    // ── Class-level constants ─────────────────────────────────────────────────
    private static final BigDecimal DAILY_LIVING_COST = BigDecimal.valueOf(250);
    private static final BigDecimal FLIGHT_COST       = BigDecimal.valueOf(1500);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String destination;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DelegationStatus status = DelegationStatus.PLANNED;

    // ── Associations ──────────────────────────────────────────────────────────

    @ManyToOne(optional = false)
    @JoinColumn(name = "scout_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Scout scout;

    @ManyToOne(optional = false)
    @JoinColumn(name = "director_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Director createdBy;

    @OneToMany(mappedBy = "delegation", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Match> matches = new ArrayList<>();

    // ── Derived attribute ─────────────────────────────────────────────────────

    /**
     * /cost – not persisted.
     * Formula: FLIGHT_COST * 2 + DAILY_LIVING_COST * durationDays.
     */
    @Transient
    public BigDecimal getCost() {
        if (startDate == null || endDate == null) return BigDecimal.ZERO;
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        return FLIGHT_COST.multiply(BigDecimal.valueOf(2))
                .add(DAILY_LIVING_COST.multiply(BigDecimal.valueOf(days)));
    }
}
