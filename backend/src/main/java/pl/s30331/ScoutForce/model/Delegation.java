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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scout_id", nullable = false)
    private Scout scout;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "director_id", nullable = false)
    private Director createdBy;

    @OneToMany(mappedBy = "delegation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Match> matches = new ArrayList<>();

    @Transient
    public BigDecimal getCost() {
        if (startDate == null || endDate == null) return BigDecimal.ZERO;
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        return FLIGHT_COST.multiply(BigDecimal.valueOf(2))
                .add(DAILY_LIVING_COST.multiply(BigDecimal.valueOf(days)));
    }
}
