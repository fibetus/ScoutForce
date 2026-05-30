package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Director – dyrektor sportowy klubu.
 *
 * Wymaganie 5 / 20: tworzenie nowych delegacji jest realizowane wyłącznie
 * przez dyrektora klubu.
 *
 * Associations:
 *  - Director 1 ──* Delegation (createdDelegations)
 */
@Entity
@Table(name = "director")
@PrimaryKeyJoinColumn(name = "person_id")
@Getter
@Setter
@NoArgsConstructor
public class Director extends ClubEmployee {

    /** Delegations created (issued) by this director. */
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Delegation> createdDelegations = new ArrayList<>();
}
