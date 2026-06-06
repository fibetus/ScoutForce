package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.s30331.ScoutForce.model.enums.DelegationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Trip on which a {@link Director} sends a {@link Scout} to observe {@link Match}es.
 *
 * <p>Every delegation requires both {@link #createdBy} (director) and {@link #scout}.</p>
 */
@Entity
@Table(name = "delegation")
@Getter
@Setter
@NoArgsConstructor
public class Delegation {

    private static final BigDecimal DAILY_LIVING_COST = BigDecimal.valueOf(250);
    private static final BigDecimal FLIGHT_COST       = BigDecimal.valueOf(1500);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate endDate;

    @NotBlank
    @Column(nullable = false)
    private String destination;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DelegationStatus status = DelegationStatus.PLANNED;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scout_id", nullable = false)
    private Scout scout;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "director_id", nullable = false)
    private Director createdBy;

    @OneToMany(mappedBy = "delegation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Match> matches = new ArrayList<>();

    /**
     * Estimates total delegation cost (return flights plus daily living allowance).
     *
     * @return cost in arbitrary currency units, or {@code 0} when dates are unset
     */
    @Transient
    public BigDecimal getCost() {
        if (startDate == null || endDate == null) return BigDecimal.ZERO;
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return FLIGHT_COST.multiply(BigDecimal.valueOf(2))
                .add(DAILY_LIVING_COST.multiply(BigDecimal.valueOf(days)));
    }
}
